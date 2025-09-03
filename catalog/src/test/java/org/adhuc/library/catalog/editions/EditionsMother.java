package org.adhuc.library.catalog.editions;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.LocalizedDetails;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static java.time.LocalDate.now;
import static net.jqwik.api.Arbitraries.integers;
import static net.jqwik.api.Arbitraries.strings;
import static net.jqwik.time.api.Dates.dates;
import static org.adhuc.library.catalog.authors.AuthorsMother.Real.NICOLAS_FRAMONT;
import static org.adhuc.library.catalog.editions.IsbnGenerator.isbn13s;
import static org.adhuc.library.catalog.editions.PublishersMother.Real.*;

public final class EditionsMother {

    public static Arbitrary<Edition> editions() {
        return Combinators.combine(
                Editions.isbns(),
                Editions.titles(),
                Editions.publicationDates(),
                Editions.books(),
                Editions.publishers(),
                Editions.languages(),
                Editions.summaries()
        ).as(Edition::new);
    }

    public static Arbitrary<Edition> editionsOf(Book book) {
        return Combinators.combine(
                Editions.isbns(),
                Editions.titles(),
                Editions.publicationDates(),
                Arbitraries.just(book),
                Editions.publishers(),
                Editions.languages(),
                Editions.summaries()
        ).as(Edition::new);
    }

    public static EditionBuilder builder() {
        return new EditionBuilder();
    }

    public static final class Editions {
        public static Arbitrary<String> isbns() {
            return isbn13s();
        }

        public static Arbitrary<String> titles() {
            return strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(100)
                    .filter(s -> !s.isBlank());
        }

        public static Arbitrary<PublicationDate> publicationDates() {
            return Arbitraries.oneOf(
                    exactPublicationDates(),
                    yearADPublicationDates(),
                    yearBCPublicationDates()
            );
        }

        public static Arbitrary<PublicationDate> exactPublicationDates() {
            return dates().atTheEarliest(LocalDate.parse("1800-01-01")).atTheLatest(now()).map(PublicationDate::of);
        }

        public static Arbitrary<PublicationDate> yearADPublicationDates() {
            return integers().between(0, now().getYear()).map(PublicationDate::of);
        }

        public static Arbitrary<PublicationDate> yearBCPublicationDates() {
            return integers().between(-1000, -1).map(PublicationDate::of);
        }

        public static Arbitrary<Book> books() {
            return BooksMother.books();
        }

        public static Arbitrary<Publisher> publishers() {
            return PublishersMother.publishers();
        }

        public static Arbitrary<Book> booksAuthoredWith(UUID authorId) {
            return BooksMother.notableBooksOf(authorId);
        }

        public static Arbitrary<String> languages() {
            return Arbitraries.of("French", "English");
        }

        public static Arbitrary<String> summaries() {
            return strings().alpha().numeric().withChars(" ,;.?!:-()[]{}&\"'àéèïöù").ofMinLength(30)
                    .filter(s -> !s.isBlank());
        }
    }

    public static class EditionBuilder {
        private Edition edition = editions().sample();

        public EditionBuilder isbn(String isbn) {
            edition = new Edition(isbn, edition.title(), edition.publicationDate(), edition.book(),
                    edition.publisher().orElse(null), edition.language(), edition.summary());
            return this;
        }

        public EditionBuilder title(String title) {
            edition = new Edition(edition.isbn(), title, edition.publicationDate(), edition.book(),
                    edition.publisher().orElse(null), edition.language(), edition.summary());
            return this;
        }

        public EditionBuilder publicationDate(PublicationDate publicationDate) {
            edition = new Edition(edition.isbn(), edition.title(), publicationDate, edition.book(),
                    edition.publisher().orElse(null), edition.language(), edition.summary());
            return this;
        }

        public EditionBuilder book(Book book) {
            edition = new Edition(edition.isbn(), edition.title(), edition.publicationDate(), book,
                    edition.publisher().orElse(null), edition.language(), edition.summary());
            return this;
        }

        public EditionBuilder publisher(@Nullable Publisher publisher) {
            edition = new Edition(edition.isbn(), edition.title(), edition.publicationDate(), edition.book(),
                    publisher, edition.language(), edition.summary());
            return this;
        }

        public EditionBuilder language(String language) {
            edition = new Edition(edition.isbn(), edition.title(), edition.publicationDate(), edition.book(),
                    edition.publisher().orElse(null), language, edition.summary());
            return this;
        }

        public EditionBuilder summary(String summary) {
            edition = new Edition(edition.isbn(), edition.title(), edition.publicationDate(), edition.book(),
                    edition.publisher().orElse(null), edition.language(), summary);
            return this;
        }

        public Edition build() {
            return edition;
        }
    }

    public static final class Real {

        public static final Edition L_ETRANGER = new Edition(
                "9782070360024",
                "L'étranger",
                PublicationDate.of(LocalDate.parse("1972-01-07")),
                BooksMother.Real.L_ETRANGER,
                GALLIMARD,
                "fr",
                "Quand la sonnerie a encore retenti, que la porte du box s'est ouverte, c'est le silence de la salle qui est monté vers moi, le silence, et cette singulière sensation que j'ai eue lorsque j'ai constaté que le jeune journaliste avait détourné les yeux. Je n'ai pas regardé du côté de Marie. Je n'en ai pas eu le temps parce que le président m'a dit dans une forme bizarre que j'aurais la tête tranchée sur une place publique au nom du peuple français..."
        );
        public static final Edition LA_PESTE = new Edition(
                "9782070360420",
                "La peste",
                PublicationDate.of(LocalDate.parse("2000-02-27")),
                BooksMother.Real.LA_PESTE,
                GALLIMARD,
                "fr",
                "C'est moi qui remplace la peste, s'écriait Caligula, l'empereur dément. Bientôt, la \"peste brune\" déferlait sur l'Europe dans un grand bruit de bottes. France déchirée aux coutures de Somme et de Loire, troupeaux de prisonniers, esclaves voués par millions aux barbelés et aux crématoires, La Peste éternise ces jours de ténèbres, cette \"passion collective\" d'une Europe en folie, détournée comme Oran de la mer et de sa mesure.\nSans doute la guerre accentue-t-elle la séparation, la maladie, l'insécurité. Mais ne sommes-nous pas toujours plus ou moins séparés, menacés, exilés, rongés comme le fruit par le ver ? Face aux souffrances comme à la mort, à l'ennui des recommencenments, La Peste recense les conduites ; elle nous impose la vision d'un univers sans avenir ni finalité, un monde de la répétition et de l'étouffante monotonie, où le drame même cesse de paraître dramatique et s'imprègne d'humour macabre, où les hommes se définissent moins par leur démarche, leur langage et leur poids de chair que par leurs silences, leurs secrètes blessures, leurs ombres portées et leurs réactions aux défis de l'existence.\nLa Peste sera donc, au gré des interprétations, la \"chronique de la résistance\" ou un roman de la permanence, le prolongement de L'Étranger ou \"un progrès\" sur L'Étranger, le livre des \"damnés\" et des solitaires ou le manuel du relatif et de la solidarité - en tout cas, une ouvre pudique et calculée qu'Albert Camus douta parfois de mener à bien, au cours de sept années de gestation, de maturation et de rédaction difficiles..."
        );
        public static final Edition LA_CHUTE = new Edition(
                "9782070360109",
                "La chute",
                PublicationDate.of(LocalDate.parse("1972-01-18")),
                BooksMother.Real.LA_CHUTE,
                GALLIMARD,
                "fr",
                "\"Sur le pont, je passai derrière une forme penchée sur le parapet, et qui semblait regarder le fleuve. De plus près, je distinguai une mince jeune femme, habillée de noir. Entre les cheveux sombres et le col du manteau, on voyait seulement une nuque, fraîche et mouillée, à laquelle je fus sensible. Mais je poursuivis ma route, après une hésitation. [... ] J'avais déjà parcouru une cinquantaine de mètres à peu près, lorsque j'entendis le bruit, qui, malgré la distance, me parut formidable dans le silence nocturne, d'un corps qui s'abat sur l'eau.\nJe m'arrêtai net, mais sans me retourner. Presque aussitôt, j'entendis un cri, plusieurs fois répété, qui descendait lui aussi le fleuve, puis s'éteignit brusquement\"."
        );
        public static final Edition MADAME_BOVARY = new Edition(
                "9782070413119",
                "Madame Bovary",
                PublicationDate.of(LocalDate.parse("2001-05-16")),
                BooksMother.Real.MADAME_BOVARY,
                GALLIMARD,
                "fr",
                "C'est l'histoire d'une femme mal mariée, de son médiocre époux, de ses amants égoïstes et vains, de ses rêves, de ses chimères, de sa mort. C'est l'histoire d'une province étroite, dévote et bourgeoise. C'est, aussi, l'histoire du roman français. Rien, dans ce tableau, n'avait de quoi choquer la société du Second Empire. Mais, inexorable comme une tragédie, flamboyant comme un drame, mordant comme une comédie, le livre s'était donné une arme redoutable : le style. Pour ce vrai crime, Flaubert se retrouva en correctionnelle. Aucun roman n'est innocent : celui-là moins qu'un autre. Lire Madame Bovary, au XXIe siècle, c'est affronter le scandale que représente une œuvre aussi sincère qu'impérieuse. Dans chacune de ses phrases, Flaubert a versé une dose de cet arsenic dont Emma Bovary s'empoisonne : c'est un livre offensif, corrosif, dont l'ironie outrage toutes nos valeurs, et la littérature même, qui ne s'en est jamais vraiment remise."
        );
        public static final Edition SALAMMBO = new Edition(
                "9782070308781",
                "Salammbô",
                PublicationDate.of(LocalDate.parse("2005-06-02")),
                BooksMother.Real.SALAMMBO,
                GALLIMARD,
                "fr",
                "Il arriva juste au pied de la terrasse. Salammbô était penchée sur la balustrade; ces effroyables prunelles la contemplaient, et la conscience lui surgit de tout ce qu'il avait souffert pour elle. Bien qu'il agonisât, elle le revoyait dans sa tente, à genoux, lui entourant la taille de ses bras, balbutiant des paroles douces; elle avait soif de les sentir encore, de les entendre; elle ne voulait pas qu'il mourût! À ce moment-là, Mâtho eut un grand tressaillement; elle allait crier. Il s'abattit à la renverse et ne bougea plus. \" Un prétexte à joyaux et à rêves \" (Albert Thibaudet)."
        );
        public static final Edition ANNA_KARENINE = new Edition(
                "9782253098386",
                "Anna Karénine",
                PublicationDate.of(LocalDate.parse("1997-05-28")),
                BooksMother.Real.ANNA_KARENINE,
                LGF,
                "fr",
                "Anna n'est pas qu'une femme, qu'un splendide spécimen du sexe féminin, c'est une femme dotée d'un sens moral entier, tout d'un bloc, prédominant : tout ce qui fait partie de sa personne est important, a une intensité dramatique, et cela s'applique aussi bien à son amour.Elle n'est pas, comme Emma Bovary, une rêveuse de province, une femme désenchantée qui court en rasant des murs croulants vers les lits d'amants interchangeables.\nAnna donne à Vronski toute sa vie.Elle part vivre avec lui d'abord en Italie, puis dans les terres de la Russie centrale, bien que cette liaison « notoire » la stigmatise, aux yeux du monde immoral dans lequel elle évolue, comme une femme immorale. Anna scandalise la société hypocrite moins par sa liaison amoureuse que par son mépris affiché des conventions sociales.Avec Anna Karénine, Tolstoï atteint le comble de la perfection créative.Vladimir Nabokov.Préface d'André Maurois.Commentaires de Marie Sémon."
        );
        public static final Edition LA_GUERRE_ET_LA_PAIX_1 = new Edition(
                "9782070425174",
                "La guerre et la paix. Tome 1",
                PublicationDate.of(LocalDate.parse("2002-07-10")),
                BooksMother.Real.GUERRE_ET_PAIX,
                GALLIMARD,
                "fr",
                "Au début du XIXe siècle, Pierre Bézoukhov, fils illégitime héritier d'une grande fortune, et son ami André Bolkonsky, officier tourmenté, évoluent dans une haute société russe francophile et mondaine qui ne tardera pas à être rattrapée par les tourments de la guerre qui s'annonce. Le parcours spirituel et politique de Pierre, comme le trajet militaire d'André, est inséparable du destin contrarié de la Russie : Saint-Pétersbourg et Moscou, la campagne et la ville, la Sibérie et l'Europe.\nLa Russie est bicéphale, tragiquement clivée par le désir patiné de haine qui l'attache au reste de l'Occident. La France et Napoléon sont l'incarnation de cet idéal policé et calculateur : un ennemi mortel que les personnages admireront avant de le combattre. Au coeur des guerres napoléoniennes qui ravagèrent le vieux continent, Tolstoï tourne les pages d'un roman immortel : l'âme russe."
        );
        public static final Edition LA_GUERRE_ET_LA_PAIX_2 = new Edition(
                "9782070425181",
                "La guerre et la paix. Tome 2",
                PublicationDate.of(LocalDate.parse("2002-07-09")),
                BooksMother.Real.GUERRE_ET_PAIX,
                GALLIMARD,
                "fr",
                "- Couchez-vous ! cria l'aide de camp en se jetant à terre. Le prince André, debout, hésitait. La grenade fumante tournait comme une toupie entre lui et l'aide de camp, à la limite de la prairie et du champ, près d'une touffe d'armoise. \"Est-ce vraiment la mort ? \" se dit le prince André en considérant d'un regard neuf, envieux, l'herbe, l'armoise et le filet de fumée qui s'élevait de la balle noire tourbillonnante.\n\"Je ne veux pas, je ne veux pas mourir, j'aime la vie, j'aime cette herbe, cette terre et l'air...\""
        );
        public static final Edition LES_COSAQUES = new Edition(
                "9782070368501",
                "Les cosaques",
                PublicationDate.of(LocalDate.parse("1976-11-05")),
                BooksMother.Real.LES_COSAQUES,
                GALLIMARD,
                "fr",
                "A travers les paysages du Caucase et le régiment de Cosaques auquel il est affecté, un jeune officier, Olénine, qui n'est autre que Tolstoï lui-même, découvre la splendeur du monde primitif. \"Dieu que notre Russie est triste\", soupirait Pouchkine ; le Caucase, c'est pour Tolstoï la découverte de la joie, l'oubli de l'accablant sentiment de culpabilité qui est au fond de l'âme russe. D'admirables évocations de nature.\nLe pittoresque éclatant des voyages romantiques. Et une histoire d'amour où nous voyons Olénine s'éprendre d'une jeune Cosaque, Marion, qui est pour lui le symbole d'une liberté encore insaisissable. Marion refusera d'épouser Olénine mais celui-ci ne l'oubliera jamais, et Les Cosaques sont le point de départ de l'évolution morale de Tolstoï."
        );
        public static final Edition CHER_CONNARD = new Edition(
                "9782253079699",
                "Cher connard",
                PublicationDate.of(LocalDate.parse("2023-08-23")),
                BooksMother.Real.CHER_CONNARD,
                LGF,
                "fr",
                "\"J'ai lu ce que tu as publié sur ton compte Insta. Tu es comme un pigeon qui m'aurait chié sur l'épaule en passant. C'est salissant, et très désagréable. Ouin ouin ouin je suis une petite baltringue qui n'intéresse personne et je couine comme un chihuahua parce que je rêve qu'on me remarque. Gloire aux réseaux sociaux : tu l'as eu, ton quart d'heure de gloire. La preuve, je t'écris.\" Après sa trilogie Vernon Subutex, Virginie Despentes nous revient avec ces Liaisons dangereuses ultracontemporaines, un roman de rage et de consolation, de colère et d'acceptation, où l'amitié transcende les faiblesses humaines."
        );
        public static final Edition BAISE_MOI = new Edition(
                "9782253087557",
                "Baise moi",
                PublicationDate.of(LocalDate.parse("2016-03-02")),
                BooksMother.Real.BAISE_MOI,
                LGF,
                "fr",
                "C’est l’histoire d’une amitié passionnelle : deux filles sans repères dont les chemins se croisent par hasard, et qui vont découvrir qu’elles n’ont plus rien à perdre. Paru en 1993 et traduit dans plus de vingt langues, Baise-moi est une déclaration de guerre au bon goût, aux beaux sentiments et à l’élégance. A la croisée du roman « hard boiled » et de la culture hard core, un roman nihiliste et trash, que sauve un humour grinçant.\nVirginie Despentes et Coralie Trinh Thi l’ont adapté à l’écran en 2000, avec Karene Bach et Rafaella Anderson dans les rôles titres. Censuré en France, le film a connu un succès durable à l’international."
        );
        public static final Edition APOCALYPSE_BEBE = new Edition(
                "9782253159711",
                "Apocalypse bébé",
                PublicationDate.of(LocalDate.parse("2012-02-29")),
                BooksMother.Real.APOCALYPSE_BEBE,
                LGF,
                "fr",
                "Valentine disparue... Qui la cherche vraiment ? Entre satire sociale, polar contemporain et romance lesbienne, le nouveau roman de Virginie Despentes est un road-book qui promène le lecteur entre Paris et Barcelone, sur les traces de tous ceux qui ont connu Valentine, l'adolescente égarée... Les différents personnages se croisent sans forcément se rencontrer, et finissent par composer, sur un ton tendre et puissant, le portrait d'une époque."
        );
        public static final Edition ASTERIX_LE_GAULOIS = new Edition(
                "9782012101333",
                "Astérix Tome 1 - Astérix le Gaulois",
                PublicationDate.of(LocalDate.parse("2004-06-16")),
                BooksMother.Real.ASTERIX_LE_GAULOIS,
                HACHETTE,
                "fr",
                "La toute première histoire d'Astérix, pour faire connaissance avec la troupe des irréductibles Gaulois."
        );
        public static final Edition LA_SERPE_D_OR = new Edition(
                "9782012101340",
                "Astérix Tome 2 - La serpe d'or",
                PublicationDate.of(LocalDate.parse("2004-06-16")),
                BooksMother.Real.LA_SERPE_D_OR,
                HACHETTE,
                "fr",
                "Astérix et Obélix partent pour Lutèce acheter une nouvelle serpe au druide Panoramix. Mais sur place, ils se rendent compte que la contrebande de serpes fait rage…"
        );
        public static final Edition ASTERIX_ET_CLEOPATRE = new Edition(
                "9782012101388",
                "Astérix Tome 6 - Astérix et CLéopâtre",
                PublicationDate.of(LocalDate.parse("2004-06-16")),
                BooksMother.Real.ASTERIX_ET_CLEOPATRE,
                HACHETTE,
                "fr",
                "Cléopâtre fait le pari avec César que son peuple est encore capable de grandes réalisations. Elle ordonne à Numérobis de construire un palais somptueux pour César."
        );
        public static final Edition LE_PETIT_NICOLAS = new Edition(
                "9782070612765",
                "Le Petit Nicolas",
                PublicationDate.of(LocalDate.parse("2007-03-15")),
                BooksMother.Real.LE_PETIT_NICOLAS,
                GALLIMARD_JEUNESSE,
                "fr",
                "La maîtresse est inquiète, le photographe s'éponge le front, le Bouillon devient tout rouge, les mamans ont mauvaise mine, les papas font les guignols, le directeur part à la retraite, quant à l'inspecteur, il est reparti aussi vite qu'il était venu. Pourtant, à l'école ou en famille, Geoffroy, Agnan, Eudes, Rufus, Clotaire, Maixent, Alceste, Joachim... et le Petit Nicolas sont - presque - toujours sages."
        );
        public static final Edition VOUS_NE_DETESTEZ_PAS_LE_LUNDI = new Edition(
                "9791020923561",
                "Vous ne détestez pas le lundi",
                PublicationDate.of(LocalDate.parse("2024-10-02")),
                BooksMother.Real.VOUS_NE_DETESTEZ_PAS_LE_LUNDI,
                LES_LIENS_QUI_LIBERENT,
                "fr",
                "En France, comme ailleurs, le mal-être au travail s’amplifie. Les travailleurs expriment leur mécontentement face à l’organisation, à l’utilité perçue et à la reconnaissance hiérarchique de leur entreprise. Tous les secteurs, y compris l’associatif et le service public, sont désormais contaminés par des formes de management absurdes et violentes qui font redouter le lundi matin."
        );

    }

}
