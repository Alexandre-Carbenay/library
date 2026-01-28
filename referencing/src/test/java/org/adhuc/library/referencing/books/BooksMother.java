package org.adhuc.library.referencing.books;

import net.datafaker.Faker;
import org.adhuc.library.referencing.authors.AuthorsMother;
import org.adhuc.library.referencing.books.Book.LocalizedDetail;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.adhuc.library.referencing.authors.AuthorsMother.Real.*;
import static org.adhuc.library.referencing.books.BooksMother.Books.*;

public class BooksMother {

    public static List<Book> books(int size) {
        return IntStream.range(0, size)
                .mapToObj(_ -> book())
                .toList();
    }

    public static Book book() {
        var originalLanguage = language();
        return new Book(id(), authors(), originalLanguage, Set.copyOf(detailsLists(originalLanguage)));
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static LocalizedDetailsBuilder detailsBuilder() {
        return new LocalizedDetailsBuilder();
    }

    public static final class Books {
        private static final Faker FAKER = new Faker();
        private static final List<String> LANGUAGES = List.of("fr", "en", "de", "it");

        public static UUID id() {
            return UUID.randomUUID();
        }

        public static UUID author() {
            return AuthorsMother.author().id();
        }

        public static Set<UUID> authors() {
            var numberOfAuthors = FAKER.random().nextInt(1, 3);
            return authors(numberOfAuthors);
        }

        public static Set<UUID> authors(int numberOfAuthors) {
            return IntStream.range(0, numberOfAuthors)
                    .mapToObj(_ -> author())
                    .collect(toSet());
        }

        public static String language() {
            return LANGUAGES.get(FAKER.random().nextInt(4));
        }

        public static List<String> otherLanguages(String language) {
            var otherLanguages = LANGUAGES.stream().filter(other -> !other.equals(language));
            var maximumOtherLanguages = LANGUAGES.size() - 1;
            var numberOfLanguages = FAKER.random().nextInt(1, maximumOtherLanguages);
            return otherLanguages.limit(numberOfLanguages).toList();
        }

        public static List<LocalizedDetail> detailsLists(String originalLanguage) {
            return detailsLists(originalLanguage, Set.copyOf(otherLanguages(originalLanguage)));
        }

        public static List<LocalizedDetail> detailsLists(Collection<String> languages) {
            return languages.stream()
                    .map(Books::details)
                    .toList();
        }

        public static List<LocalizedDetail> detailsLists(String originalLanguage, Set<String> otherLanguages) {
            var originalDetails = details(originalLanguage);
            var otherDetails = detailsLists(otherLanguages);
            var details = new ArrayList<>(otherDetails);
            details.add(originalDetails);
            return details;
        }

        public static LocalizedDetail details(String language) {
            return new LocalizedDetail(language, title(), description());
        }

        public static String title() {
            return FAKER.book().title();
        }

        public static String description() {
            return FAKER.text().text(30, 1000);
        }
    }

    public static class BookBuilder {
        private Book book = book();

        public BookBuilder id(UUID id) {
            book = new Book(id, book.authors(), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder authors(Set<UUID> authors) {
            book = new Book(book.id(), authors, book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder oneAuthor() {
            book = new Book(book.id(), Set.of(author()), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder multipleAuthors() {
            book = new Book(book.id(), Set.of(author(), author()), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder originalLanguage(String originalLanguage) {
            book = new Book(book.id(), book.authors(), originalLanguage, Set.copyOf(Books.detailsLists(originalLanguage)));
            return this;
        }

        public BookBuilder detailInOriginalLanguage() {
            return this.details(Books.details(book.originalLanguage()));
        }

        public BookBuilder detailInOtherLanguage() {
            var language = Books.otherLanguages(book.originalLanguage()).getFirst();
            return this.details(Books.details(language));
        }

        public BookBuilder plusDetailInOtherLanguage() {
            var language = Books.otherLanguages(book.originalLanguage()).getFirst();
            var details = new HashSet<>(book.details());
            details.add(Books.details(language));
            return this.details(details);
        }

        public BookBuilder details(LocalizedDetail localizedDetail) {
            book = new Book(book.id(), book.authors(), book.originalLanguage(), Set.of(localizedDetail));
            return this;
        }

        public BookBuilder details(Set<LocalizedDetail> localizedDetails) {
            book = new Book(book.id(), book.authors(), book.originalLanguage(), localizedDetails);
            return this;
        }

        public Book build() {
            return book;
        }
    }

    public static class LocalizedDetailsBuilder {
        private String language;
        private String title;
        private String description;

        private LocalizedDetailsBuilder() {
            language = Books.language();
            title = Books.title();
            description = Books.description();
        }

        public LocalizedDetailsBuilder language(String language) {
            this.language = language;
            return this;
        }

        public LocalizedDetailsBuilder title(String title) {
            this.title = title;
            return this;
        }

        public LocalizedDetailsBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LocalizedDetail build() {
            return new LocalizedDetail(language, title, description);
        }
    }

    public static final class Real {

        public static final Book L_ETRANGER = new Book(
                UUID.fromString("a24564d8-8ab9-474d-9593-8a43c45bbbad"),
                Set.of(ALBERT_CAMUS.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "L'Étranger",
                                "L'Étranger est le premier roman publié d'Albert Camus, paru en 1942. Les premières esquisses datent de 1938, mais le roman ne prend vraiment forme que dans les premiers mois de 1940 et sera travaillé par Camus jusqu’en 1941. Il prend place dans la tétralogie que Camus nommera « cycle de l'absurde » qui décrit les fondements de la philosophie camusienne : l'absurde. Cette tétralogie comprend également l'essai Le Mythe de Sisyphe ainsi que les pièces de théâtre Caligula et Le Malentendu."
                        ),
                        new LocalizedDetail(
                                "en",
                                "The Stranger",
                                "The Stranger, also published in English as The Outsider, is a 1942 novella written by French author Albert Camus. The first of Camus's novels to be published, the story follows Meursault, an indifferent settler in French Algeria, who, weeks after his mother's funeral, kills an unnamed Arab man in Algiers. The story is divided into two parts, presenting Meursault's first-person narrative before and after the killing."
                        ),
                        new LocalizedDetail(
                                "de",
                                "Der Fremde",
                                "Der Fremde ist ein Roman des französischen Schriftstellers und Philosophen Albert Camus. Er erschien 1942 im Pariser Verlagshaus Gallimard und wurde einer der meistgedruckten französischen Romane des 20. Jahrhunderts. Er gilt als eines der Hauptwerke der Philosophie des Existentialismus und Absurdismus."
                        )
                )
        );
        public static final Book LA_PESTE = new Book(
                UUID.fromString("e8fc9ca8-913a-40fd-9622-162a27b8cf82"),
                Set.of(ALBERT_CAMUS.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "La Peste",
                                "La Peste est un roman d'Albert Camus, prix Nobel de littérature en 1957.\n\nInspiré du thème de l'absurde et publié en 1947, le roman a reçu le prix des Critiques la même année. Il appartient au « cycle de la révolte » regroupant deux autres œuvres de Camus : L'Homme révolté et Les Justes."
                        ),
                        new LocalizedDetail(
                                "en",
                                "The Plague",
                                "The Plague is a 1947 absurdist novel by Albert Camus. The plot centers around the French Algerian city of Oran as it combats a plague outbreak and is put under a city-wide quarantine. The novel presents a snapshot into life in Oran as seen through Camus's absurdist lens."
                        )
                )
        );
        public static final Book LA_CHUTE = new Book(
                UUID.fromString("1dc7b5ff-93fe-4d32-9b3e-0c94d5a48dd9"),
                Set.of(ALBERT_CAMUS.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "La Chute",
                                "La Chute est un court roman d'Albert Camus publié à Paris chez Gallimard en 1956, découpé en six parties non numérotées. Camus y écrit la confession d'un homme à un autre, rencontré dans un bar d'Amsterdam. Le roman devait primitivement être intégré au recueil L'Exil et le Royaume qui sera publié en 1957 et qui constitue la dernière œuvre « littéraire » publiée par Camus."
                        ),
                        new LocalizedDetail(
                                "de",
                                "Der Fall",
                                "Der Fall ist ein Roman von Albert Camus. Er sollte eigentlich in Camus’ Novellen des Exils (Das Exil und das Reich) veröffentlicht werden, wurde dann aber zu umfangreich, und erschien bereits im Jahr 1956 als vorgeschobenes Einzelwerk. Er ist Camus’ letztes vollendetes Prosawerk."
                        )
                )
        );
        public static final Book ASTERIX_LE_GAULOIS = new Book(
                UUID.fromString("b03d3316-76f9-4031-9bfd-34538fbb9fa8"),
                Set.of(RENE_GOSCINNY.id(), ALBERT_UDERZO.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "Astérix le Gaulois",
                                "Astérix le Gaulois est le premier album de la bande dessinée Astérix, publié en octobre 1961, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le magazine Pilote du no 1 (29 octobre 1959) au no 38 (14 juillet 1960)."
                        )
                )
        );
        public static final Book LA_SERPE_D_OR = new Book(
                UUID.fromString("2d1a7b82-e636-4e94-b698-f91203755a05"),
                Set.of(RENE_GOSCINNY.id(), ALBERT_UDERZO.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "La Serpe d'or",
                                "La Serpe d'or est le deuxième album de la bande dessinée Astérix, publié en 1962, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le journal Pilote du no 42 (11 août 1960) au no 74 (23 mars 1961)."
                        )
                )
        );
        public static final Book ASTERIX_ET_CLEOPATRE = new Book(
                UUID.fromString("71ab800c-8940-4237-87d7-89afdcb92929"),
                Set.of(RENE_GOSCINNY.id(), ALBERT_UDERZO.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "Astérix et Cléopâtre",
                                "Astérix et Cléopâtre est le sixième album de la bande dessinée Astérix, publié en 1965, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le journal Pilote du no 215 (5 décembre 1963) au no 257 (24 septembre 1964). "
                        )
                )
        );
        public static final Book LE_PETIT_NICOLAS = new Book(
                UUID.fromString("7e58d0d2-92ca-4964-a010-dc174752e01d"),
                Set.of(RENE_GOSCINNY.id(), JEAN_JACQUES_SEMPE.id()),
                "fr",
                Set.of(
                        new LocalizedDetail(
                                "fr",
                                "Le Petit Nicolas",
                                "Le Petit Nicolas est une œuvre de littérature d'enfance et de jeunesse écrite de 1956 à 1965 par René Goscinny, et illustrée par Jean-Jacques Sempé. Écrites sous forme de courts récits dans lesquels se mêlent l'humour et la tendresse de l'enfance, les aventures du Petit Nicolas mettent en scène un petit garçon dans un environnement urbain pendant les années 1960. Le personnage y livre ses pensées intimes grâce à un langage enfantin créé par Goscinny et les thèmes sont avant tout ceux de l'enfance (la camaraderie, les disputes, les rapports avec la maîtresse d'école, les premières amourettes...) mais Goscinny y décrypte également le monde complexe des adultes : l'éducation, les disputes familiales, les rapports entre voisins, la relation du père avec son patron, etc."
                        )
                )
        );

    }

}
