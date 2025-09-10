package org.adhuc.library.catalog.books;

import net.datafaker.Faker;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsMother;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.Books.*;

public class BooksMother {

    public static Book book() {
        var originalLanguage = language();
        return new Book(id(), authors(), originalLanguage, detailsSets(originalLanguage));
    }

    public static List<Book> notableBooksOf(UUID authorId) {
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);

        return notableBooksOf(authorId, originalLanguage, otherLanguages);
    }

    public static List<Book> notableBooksOf(UUID authorId, String originalLanguage, Collection<String> otherLanguages) {
        var numberOfBooks = Books.FAKER.random().nextInt(1, 10);
        return IntStream.range(0, numberOfBooks)
                .mapToObj(_ -> notableBookOf(authorId, originalLanguage, otherLanguages))
                .toList();
    }

    public static Book notableBookOf(UUID authorId, String originalLanguage, Collection<String> otherLanguages) {
        return new Book(
                Books.id(),
                Books.authoredWith(authorId),
                originalLanguage,
                Books.detailsSets(originalLanguage, Set.copyOf(otherLanguages))
        );
    }

    public static List<Book> books(int numberOfBooks, String originalLanguage, Collection<String> languages) {
        return IntStream.range(0, numberOfBooks)
                .mapToObj(_ -> builder()
                        .originalLanguage(originalLanguage)
                        .details(detailsSets(originalLanguage, Set.copyOf(languages)))
                        .build()
                )
                .toList();
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

        public static Set<Author> authors() {
            var numberOfAuthors = FAKER.random().nextInt(1, 3);
            return IntStream.range(0, numberOfAuthors)
                    .mapToObj(_ -> AuthorsMother.author())
                    .collect(toSet());
        }

        public static Set<Author> authoredWith(UUID authorId) {
            var author = AuthorsMother.builder().id(authorId).build();
            var numberOfOtherAuthors = FAKER.random().nextInt(3);
            var others = AuthorsMother.authors(numberOfOtherAuthors);
            var authors = new HashSet<>(others);
            authors.add(author);
            return authors;
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

        public static Set<LocalizedDetails> detailsSets(String originalLanguage) {
            return detailsSets(originalLanguage, Set.copyOf(otherLanguages(originalLanguage)));
        }

        public static Set<LocalizedDetails> detailsSets(String originalLanguage, Set<String> otherLanguages) {
            var originalDetails = details(originalLanguage);
            var otherDetails = otherLanguages.stream()
                    .map(Books::details)
                    .toList();
            var details = new HashSet<>(otherDetails);
            details.add(originalDetails);
            return details;
        }

        public static LocalizedDetails details(String language) {
            return new LocalizedDetails(language, title(), description(), externalLinks());
        }

        public static String title() {
            return FAKER.book().title();
        }

        public static String description() {
            return FAKER.text().text(30, 1000);
        }

        public static Set<ExternalLink> externalLinks() {
            var links = new HashSet<ExternalLink>();
            if (FAKER.bool().bool()) {
                links.add(wikipediaLink());
            }
            if (FAKER.bool().bool()) {
                links.add(otherLink());
            }
            return links;
        }

        public static ExternalLink wikipediaLink() {
            return links("wikipedia");
        }

        public static ExternalLink otherLink() {
            return links("other");
        }

        private static ExternalLink links(String source) {
            return new ExternalLink(source, FAKER.internet().url());
        }
    }

    public static class BookBuilder {
        private Book book = book();

        public BookBuilder id(UUID id) {
            book = new Book(id, book.authors(), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder authors(Set<Author> authors) {
            book = new Book(book.id(), authors, book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder originalLanguage(String originalLanguage) {
            book = new Book(book.id(), book.authors(), originalLanguage, Books.detailsSets(originalLanguage));
            return this;
        }

        public BookBuilder details(LocalizedDetails localizedDetail) {
            book = new Book(book.id(), book.authors(), book.originalLanguage(), Set.of(localizedDetail));
            return this;
        }

        public BookBuilder details(Set<LocalizedDetails> localizedDetails) {
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
        private Map<String, ExternalLink> links;

        private LocalizedDetailsBuilder() {
            language = Books.language();
            title = Books.title();
            description = Books.description();
            links = prepareLinks(externalLinks());
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

        public LocalizedDetailsBuilder links(Set<ExternalLink> links) {
            this.links = prepareLinks(links);
            return this;
        }

        private Map<String, ExternalLink> prepareLinks(Set<ExternalLink> links) {
            return links.stream().collect(toMap(ExternalLink::source, Function.identity()));
        }

        public LocalizedDetailsBuilder withWikipediaLink() {
            return additionalLink(wikipediaLink());
        }

        public LocalizedDetailsBuilder withoutWikipediaLink() {
            links.remove("wikipedia");
            return this;
        }

        public LocalizedDetailsBuilder additionalLink(ExternalLink link) {
            this.links.put(link.source(), link);
            return this;
        }

        public LocalizedDetails build() {
            return new LocalizedDetails(language, title, description, Set.copyOf(links.values()));
        }
    }

    public static final class Real {

        public static final Book L_ETRANGER = new Book(
                UUID.fromString("a24564d8-8ab9-474d-9593-8a43c45bbbad"),
                Set.of(ALBERT_CAMUS),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "L'Étranger",
                                "L'Étranger est le premier roman publié d'Albert Camus, paru en 1942. Les premières esquisses datent de 1938, mais le roman ne prend vraiment forme que dans les premiers mois de 1940 et sera travaillé par Camus jusqu’en 1941. Il prend place dans la tétralogie que Camus nommera « cycle de l'absurde » qui décrit les fondements de la philosophie camusienne : l'absurde. Cette tétralogie comprend également l'essai Le Mythe de Sisyphe ainsi que les pièces de théâtre Caligula et Le Malentendu.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/L'Étranger"))
                        ),
                        new LocalizedDetails(
                                "en",
                                "The Stranger",
                                "The Stranger, also published in English as The Outsider, is a 1942 novella written by French author Albert Camus. The first of Camus's novels to be published, the story follows Meursault, an indifferent settler in French Algeria, who, weeks after his mother's funeral, kills an unnamed Arab man in Algiers. The story is divided into two parts, presenting Meursault's first-person narrative before and after the killing.",
                                Set.of(new ExternalLink("wikipedia", "https://en.wikipedia.org/wiki/The_Stranger_(Camus_novel)"))
                        ),
                        new LocalizedDetails(
                                "de",
                                "Der Fremde",
                                "Der Fremde ist ein Roman des französischen Schriftstellers und Philosophen Albert Camus. Er erschien 1942 im Pariser Verlagshaus Gallimard und wurde einer der meistgedruckten französischen Romane des 20. Jahrhunderts. Er gilt als eines der Hauptwerke der Philosophie des Existentialismus und Absurdismus.",
                                Set.of(new ExternalLink("wikipedia", "https://de.wikipedia.org/wiki/Der_Fremde"))
                        )
                )
        );
        public static final Book LA_PESTE = new Book(
                UUID.fromString("e8fc9ca8-913a-40fd-9622-162a27b8cf82"),
                Set.of(ALBERT_CAMUS),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "La Peste",
                                "La Peste est un roman d'Albert Camus, prix Nobel de littérature en 1957.\n\nInspiré du thème de l'absurde et publié en 1947, le roman a reçu le prix des Critiques la même année. Il appartient au « cycle de la révolte » regroupant deux autres œuvres de Camus : L'Homme révolté et Les Justes.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/La_Peste"))
                        ),
                        new LocalizedDetails(
                                "en",
                                "The Plague",
                                "The Plague is a 1947 absurdist novel by Albert Camus. The plot centers around the French Algerian city of Oran as it combats a plague outbreak and is put under a city-wide quarantine. The novel presents a snapshot into life in Oran as seen through Camus's absurdist lens.",
                                Set.of(new ExternalLink("wikipedia", "https://en.wikipedia.org/wiki/The_Plague_(novel)"))
                        )
                )
        );
        public static final Book LA_CHUTE = new Book(
                UUID.fromString("1dc7b5ff-93fe-4d32-9b3e-0c94d5a48dd9"),
                Set.of(ALBERT_CAMUS),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "La Chute",
                                "La Chute est un court roman d'Albert Camus publié à Paris chez Gallimard en 1956, découpé en six parties non numérotées. Camus y écrit la confession d'un homme à un autre, rencontré dans un bar d'Amsterdam. Le roman devait primitivement être intégré au recueil L'Exil et le Royaume qui sera publié en 1957 et qui constitue la dernière œuvre « littéraire » publiée par Camus.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/La_Chute_(roman)"))
                        ),
                        new LocalizedDetails(
                                "de",
                                "Der Fall",
                                "Der Fall ist ein Roman von Albert Camus. Er sollte eigentlich in Camus’ Novellen des Exils (Das Exil und das Reich) veröffentlicht werden, wurde dann aber zu umfangreich, und erschien bereits im Jahr 1956 als vorgeschobenes Einzelwerk. Er ist Camus’ letztes vollendetes Prosawerk.",
                                Set.of(new ExternalLink("wikipedia", "https://de.wikipedia.org/wiki/Der_Fall_(Roman)"))
                        )
                )
        );
        public static final Book MADAME_BOVARY = new Book(
                UUID.fromString("be722746-41ed-4641-9098-4c33bc535cf3"),
                Set.of(GUSTAVE_FLAUBERT),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Madame Bovary",
                                "Madame Bovary. Mœurs de province, couramment abrégé en Madame Bovary, est un roman de Gustave Flaubert paru en 1857 chez Michel Lévy frères, après une préparution en 1856 dans la Revue de Paris. Il s'agit d'une œuvre majeure de la littérature française. L'histoire est celle de l'épouse d'un médecin de province, Emma Bovary, qui lie des relations adultères et vit au-dessus de ses moyens, essayant ainsi d'éviter l’ennui, la banalité et la médiocrité de la vie provinciale.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Madame_Bovary"))
                        )
                )
        );
        public static final Book SALAMMBO = new Book(
                UUID.fromString("14bef203-4295-49b4-a880-08988ee908d0"),
                Set.of(GUSTAVE_FLAUBERT),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Salammbô",
                                "Salammbô est un roman historique de Gustave Flaubert, paru le 24 novembre 1862 chez Michel Lévy frères.\n\nIl a pour sujet la guerre des Mercenaires, au IIIe siècle av. J.-C., qui opposa la ville de Carthage aux mercenaires barbares qu'elle avait employés pendant la première guerre punique, et qui se révoltèrent, furieux de ne pas avoir reçu la solde convenue. Flaubert chercha à respecter l'histoire connue, mais profita du peu d'informations disponibles pour décrire un Orient à l'exotisme sensuel et violent.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Salammbô"))
                        )
                )
        );
        public static final Book ANNA_KARENINE = new Book(
                UUID.fromString("e3a6704f-4bc7-4120-9ef6-7b0f43f6ba8f"),
                Set.of(LEON_TOLSTOI),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Anna Karénine",
                                "Anna Karénine est un roman de Léon Tolstoï paru en 1877 en feuilleton dans Le Messager russe. Il est considéré comme un chef-d'œuvre de la littérature. L'auteur y oppose le calme bonheur d'un ménage honnête formé par Lévine et Kitty Stcherbatskï aux humiliations et aux déboires qui accompagnent la passion coupable d'Alexis Vronski et d'Anna Karénine ; les premiers brouillons étaient d'ailleurs intitulés Deux mariages, deux couples.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Anna_Karénine"))
                        )
                )
        );
        public static final Book GUERRE_ET_PAIX = new Book(
                UUID.fromString("693f77eb-4fe3-45df-850e-e8ead31fc5c8"),
                Set.of(LEON_TOLSTOI),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Guerre et Paix",
                                "Guerre et Paix ou La Guerre et la Paix est un roman de l'écrivain russe Léon Tolstoï.\n\nPublié en feuilleton entre 1865 et 1869 dans Le Messager russe, ce livre narre l'histoire de la Russie à l'époque de Napoléon Ier, notamment la campagne de Russie en 1812. Léon Tolstoï y développe une théorie fataliste de l'histoire, où le libre arbitre n'a qu'une importance mineure et où tous les événements n'obéissent qu'à un déterminisme historique inéluctable. ",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Guerre_et_Paix"))
                        )
                )
        );
        public static final Book LES_COSAQUES = new Book(
                UUID.fromString("d1545e48-e39b-4156-905e-0f02c15c78a2"),
                Set.of(LEON_TOLSTOI),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Les Cosaques",
                                "Les Cosaques est un roman de Léon Tolstoï écrit en grande partie dans son domaine de Nikolskoïe-Viazemskoïe et publié en 1863 dans le Messager russe. Il a été acclamé par Ivan Bounine comme l'un des plus beaux de la langue russe.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Les_Cosaques"))
                        )
                )
        );
        public static final Book CHER_CONNARD = new Book(
                UUID.fromString("852ee559-0231-4515-ab71-a421b316921a"),
                Set.of(VIRGINIE_DESPENTES),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Cher connard",
                                "Au tournant des années 2020, dans un contexte post metoo une correspondance débute entre deux personnes, Rebecca, une actrice en fin de carrière, et Oscar, un écrivain accusé de harcèlement sexuel par Zoé, son ancienne attachée de presse. Le roman est uniquement épistolaire. Alors que les premiers échanges sont agressifs et que tout semble les opposer, une amitié nait entre les deux. Une troisième voix s'impose également, celle de Zoé. Leurs échanges balaient nombre de questionnements de société contemporains : le féminisme, les addictions dont ils cherchent à décrocher en participant aux Narcotiques anonymes, les réseaux sociaux, la résilience, l'amitié. L'humour, souvent acide, est également très présent.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Cher_Connard"))
                        )
                )
        );
        public static final Book BAISE_MOI = new Book(
                UUID.fromString("86bd5b9f-30a7-456d-8628-20469126d2fe"),
                Set.of(VIRGINIE_DESPENTES),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Baise-moi",
                                "Baise-moi est le premier roman de Virginie Despentes, paru en 1994 chez Florent Massot.\n\nCe livre fait partie d'une nouvelle littérature (tel le roman La Vie sexuelle de Catherine M. de Catherine Millet) qui affirme un « néo-féminisme revendicatif » en montrant une sexualité agressive et la désacralisation du corps féminin.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Baise-moi_(roman)"))
                        )
                )
        );
        public static final Book APOCALYPSE_BEBE = new Book(
                UUID.fromString("510d0dd6-ade3-480a-a76b-cfe6acf6a1b6"),
                Set.of(VIRGINIE_DESPENTES),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Apocalypse bébé",
                                "Apocalypse Bébé est un roman de Virginie Despentes paru le 10 août 2010 aux éditions Grasset.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Apocalypse_Bébé"))
                        )
                )
        );
        public static final Book ASTERIX_LE_GAULOIS = new Book(
                UUID.fromString("b03d3316-76f9-4031-9bfd-34538fbb9fa8"),
                Set.of(RENE_GOSCINNY, ALBERT_UDERZO),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Astérix le Gaulois",
                                "Astérix le Gaulois est le premier album de la bande dessinée Astérix, publié en octobre 1961, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le magazine Pilote du no 1 (29 octobre 1959) au no 38 (14 juillet 1960).",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Astérix_le_Gaulois_(album)"))
                        )
                )
        );
        public static final Book LA_SERPE_D_OR = new Book(
                UUID.fromString("2d1a7b82-e636-4e94-b698-f91203755a05"),
                Set.of(RENE_GOSCINNY, ALBERT_UDERZO),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "La Serpe d'or",
                                "La Serpe d'or est le deuxième album de la bande dessinée Astérix, publié en 1962, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le journal Pilote du no 42 (11 août 1960) au no 74 (23 mars 1961).",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/La_Serpe_d'or"))
                        )
                )
        );
        public static final Book ASTERIX_ET_CLEOPATRE = new Book(
                UUID.fromString("71ab800c-8940-4237-87d7-89afdcb92929"),
                Set.of(RENE_GOSCINNY, ALBERT_UDERZO),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Astérix et Cléopâtre",
                                "Astérix et Cléopâtre est le sixième album de la bande dessinée Astérix, publié en 1965, scénarisé par René Goscinny et dessiné par Albert Uderzo.\n\nIl a été pré-publié dans le journal Pilote du no 215 (5 décembre 1963) au no 257 (24 septembre 1964). ",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Astérix_et_Cléopâtre"))
                        )
                )
        );
        public static final Book LE_PETIT_NICOLAS = new Book(
                UUID.fromString("7e58d0d2-92ca-4964-a010-dc174752e01d"),
                Set.of(RENE_GOSCINNY, JEAN_JACQUES_SEMPE),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Le Petit Nicolas",
                                "Le Petit Nicolas est une œuvre de littérature d'enfance et de jeunesse écrite de 1956 à 1965 par René Goscinny, et illustrée par Jean-Jacques Sempé. Écrites sous forme de courts récits dans lesquels se mêlent l'humour et la tendresse de l'enfance, les aventures du Petit Nicolas mettent en scène un petit garçon dans un environnement urbain pendant les années 1960. Le personnage y livre ses pensées intimes grâce à un langage enfantin créé par Goscinny et les thèmes sont avant tout ceux de l'enfance (la camaraderie, les disputes, les rapports avec la maîtresse d'école, les premières amourettes...) mais Goscinny y décrypte également le monde complexe des adultes : l'éducation, les disputes familiales, les rapports entre voisins, la relation du père avec son patron, etc.",
                                Set.of(new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Le_Petit_Nicolas"))
                        )
                )
        );
        public static final Book VOUS_NE_DETESTEZ_PAS_LE_LUNDI = new Book(
                UUID.fromString("6442c845-15a8-4251-ae63-dd2ee25e2cf3"),
                Set.of(NICOLAS_FRAMONT),
                "fr",
                Set.of(
                        new LocalizedDetails(
                                "fr",
                                "Vous ne détestez pas le lundi",
                                "En France, comme ailleurs, le mal-être au travail s’amplifie. Les travailleurs expriment leur mécontentement face à l’organisation, à l’utilité perçue et à la reconnaissance hiérarchique de leur entreprise.",
                                Set.of()
                        )
                )
        );

    }

}
