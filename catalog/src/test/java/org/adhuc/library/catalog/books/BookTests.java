package org.adhuc.library.catalog.books;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.books.BooksMother.Books.*;
import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@Tag("domain")
@DisplayName("Book should")
class BookTests {

    @Test
    @DisplayName("not be creatable with empty details")
    void bookWithEmptyDetail() {
        assertThrows(IllegalArgumentException.class, () -> new Book(
                id(),
                authors(),
                "fr",
                Set.of()
        ));
    }

    @Test
    @DisplayName("not be creatable without detail in original language")
    void bookWithoutDetailInOriginalLanguage() {
        assertThrows(IllegalArgumentException.class, () -> new Book(
                id(),
                authors(),
                "fr",
                detailsSets("en", Set.of("de", "it"))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fr", "en", "de", "it"})
    @DisplayName("accept its original language")
    void acceptingOriginalLanguage(String language) {
        var book = builder().originalLanguage(language)
                .details(detailsSets(language, Set.of()))
                .build();
        assertThat(book.acceptsLanguage(language)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "fr,en",
            "en,fr",
            "de,it"
    })
    @DisplayName("accept a language that is not the original one but is defined in its details")
    void acceptingOtherLanguageInDetail(String originalLanguage, String language) {
        var book = builder().originalLanguage(originalLanguage)
                .details(detailsSets(originalLanguage, Set.of(language)))
                .build();
        assertThat(book.acceptsLanguage(language)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("notAcceptedLanguageProvider")
    @DisplayName("not accept a language that is not the original one and is not defined in its details")
    void notAcceptingOtherLanguageNotInDetail(String originalLanguage, Set<String> otherLanguages, String notAcceptedLanguage) {
        var book = builder().originalLanguage(originalLanguage)
                .details(detailsSets(originalLanguage, otherLanguages))
                .build();
        assertThat(book.acceptsLanguage(notAcceptedLanguage)).isFalse();
    }

    private static Stream<Arguments> notAcceptedLanguageProvider() {
        return Stream.of(
                Arguments.of("fr", Set.of("en", "de"), "it"),
                Arguments.of("de", Set.of("es", "it"), "fr")
        );
    }

    @ParameterizedTest
    @MethodSource("acceptedOriginalLanguageLocaleProvider")
    @DisplayName("accept its original language")
    void acceptingOriginalLanguageFromLocale(String language, Locale locale) {
        var book = builder().originalLanguage(language)
                .details(detailsSets(language, Set.of()))
                .build();
        assertThat(book.acceptsLanguage(locale)).isTrue();
    }

    public static Stream<Arguments> acceptedOriginalLanguageLocaleProvider() {
        return Stream.of(
                Arguments.of("fr", Locale.FRANCE),
                Arguments.of("fr", Locale.FRENCH),
                Arguments.of("fr", Locale.CANADA_FRENCH),
                Arguments.of("en", Locale.ENGLISH),
                Arguments.of("en", Locale.US)
        );
    }

    @ParameterizedTest
    @MethodSource("acceptedOtherLanguageLocaleProvider")
    @DisplayName("accept a language that is not the original one but is defined in its details")
    void acceptingOtherLanguageInDetailFromLocale(String originalLanguage, String language, Locale locale) {
        var book = builder().originalLanguage(originalLanguage)
                .details(detailsSets(originalLanguage, Set.of(language)))
                .build();
        assertThat(book.acceptsLanguage(locale)).isTrue();
    }

    public static Stream<Arguments> acceptedOtherLanguageLocaleProvider() {
        return Stream.of(
                Arguments.of("fr", "en", Locale.ENGLISH),
                Arguments.of("fr", "en", Locale.US),
                Arguments.of("en", "fr", Locale.FRENCH),
                Arguments.of("en", "fr", Locale.CANADA_FRENCH)
        );
    }

    @ParameterizedTest
    @MethodSource("notAcceptedLanguageLocaleProvider")
    @DisplayName("not accept a language that is not the original one and is not defined in its details")
    void notAcceptingOtherLanguageNotInDetailFromLocale(String originalLanguage, Set<String> otherLanguages, Locale notAcceptedLocale) {
        var book = builder().originalLanguage(originalLanguage)
                .details(detailsSets(originalLanguage, otherLanguages))
                .build();
        assertThat(book.acceptsLanguage(notAcceptedLocale)).isFalse();
    }

    private static Stream<Arguments> notAcceptedLanguageLocaleProvider() {
        return Stream.of(
                Arguments.of("fr", Set.of("en", "de"), Locale.ITALIAN),
                Arguments.of("de", Set.of("es", "it"), Locale.FRENCH)
        );
    }

    @ParameterizedTest
    @MethodSource("titlesAcceptedLanguageProvider")
    @DisplayName("provide title in accepted language")
    void titleInAcceptedLanguage(Book book, String language, String expected) {
        assertThat(book.titleIn(language)).isEqualTo(expected);
    }

    private static Stream<Arguments> titlesAcceptedLanguageProvider() {
        return Stream.of(
                Arguments.of(L_ETRANGER, "fr", "L'Étranger"),
                Arguments.of(L_ETRANGER, "en", "The Stranger"),
                Arguments.of(L_ETRANGER, "de", "Der Fremde"),
                Arguments.of(MADAME_BOVARY, "fr", "Madame Bovary")
        );
    }

    @ParameterizedTest
    @MethodSource("bookWithNotAcceptedLanguageProvider")
    @DisplayName("not provide title in non accepted language")
    void titleInNotAcceptedLanguage(Book book, String language) {
        assertThrows(IllegalArgumentException.class, () -> book.titleIn(language));
    }

    private static Stream<Arguments> bookWithNotAcceptedLanguageProvider() {
        return Stream.of(
                Arguments.of(L_ETRANGER, "it"),
                Arguments.of(MADAME_BOVARY, "en")
        );
    }

    @ParameterizedTest
    @MethodSource("descriptionsAcceptedLanguageProvider")
    @DisplayName("provide description in accepted language")
    void descriptionInAcceptedLanguage(Book book, String language, String expected) {
        assertThat(book.descriptionIn(language)).isEqualTo(expected);
    }

    private static Stream<Arguments> descriptionsAcceptedLanguageProvider() {
        return Stream.of(
                Arguments.of(L_ETRANGER, "fr", "L'Étranger est le premier roman publié d'Albert Camus, paru en 1942. Les premières esquisses datent de 1938, mais le roman ne prend vraiment forme que dans les premiers mois de 1940 et sera travaillé par Camus jusqu’en 1941. Il prend place dans la tétralogie que Camus nommera « cycle de l'absurde » qui décrit les fondements de la philosophie camusienne : l'absurde. Cette tétralogie comprend également l'essai Le Mythe de Sisyphe ainsi que les pièces de théâtre Caligula et Le Malentendu."),
                Arguments.of(L_ETRANGER, "en", "The Stranger, also published in English as The Outsider, is a 1942 novella written by French author Albert Camus. The first of Camus's novels to be published, the story follows Meursault, an indifferent settler in French Algeria, who, weeks after his mother's funeral, kills an unnamed Arab man in Algiers. The story is divided into two parts, presenting Meursault's first-person narrative before and after the killing."),
                Arguments.of(L_ETRANGER, "de", "Der Fremde ist ein Roman des französischen Schriftstellers und Philosophen Albert Camus. Er erschien 1942 im Pariser Verlagshaus Gallimard und wurde einer der meistgedruckten französischen Romane des 20. Jahrhunderts. Er gilt als eines der Hauptwerke der Philosophie des Existentialismus und Absurdismus."),
                Arguments.of(MADAME_BOVARY, "fr", "Madame Bovary. Mœurs de province, couramment abrégé en Madame Bovary, est un roman de Gustave Flaubert paru en 1857 chez Michel Lévy frères, après une préparution en 1856 dans la Revue de Paris. Il s'agit d'une œuvre majeure de la littérature française. L'histoire est celle de l'épouse d'un médecin de province, Emma Bovary, qui lie des relations adultères et vit au-dessus de ses moyens, essayant ainsi d'éviter l’ennui, la banalité et la médiocrité de la vie provinciale.")
        );
    }

    @ParameterizedTest
    @MethodSource("bookWithNotAcceptedLanguageProvider")
    @DisplayName("not provide description in non accepted language")
    void descriptionInNotAcceptedLanguage(Book book, String language) {
        assertThrows(IllegalArgumentException.class, () -> book.descriptionIn(language));
    }

    @ParameterizedTest
    @MethodSource("wikipediaLinksAcceptedLanguageProvider")
    @DisplayName("provide wikipedia link in accepted language")
    void wikipediaLinkInAcceptedLanguage(Book book, String language, String expected) {
        assertThat(book.wikipediaLinkIn(language)).isPresent().hasValue(new ExternalLink("wikipedia", expected));
    }

    private static Stream<Arguments> wikipediaLinksAcceptedLanguageProvider() {
        return Stream.of(
                Arguments.of(L_ETRANGER, "fr", "https://fr.wikipedia.org/wiki/L'Étranger"),
                Arguments.of(L_ETRANGER, "en", "https://en.wikipedia.org/wiki/The_Stranger_(Camus_novel)"),
                Arguments.of(L_ETRANGER, "de", "https://de.wikipedia.org/wiki/Der_Fremde"),
                Arguments.of(MADAME_BOVARY, "fr", "https://fr.wikipedia.org/wiki/Madame_Bovary")
        );
    }

    @Test
    @DisplayName("not provide wikipedia link in accepted language without such link")
    void wikipediaLinkNotPresentInAcceptedLanguage() {
        assertThat(VOUS_NE_DETESTEZ_PAS_LE_LUNDI.wikipediaLinkIn("fr")).isNotPresent();
    }

    @ParameterizedTest
    @MethodSource("bookWithNotAcceptedLanguageProvider")
    @DisplayName("not provide wikipedia link in non accepted language")
    void wikipediaLinkInNotAcceptedLanguage(Book book, String language) {
        assertThrows(IllegalArgumentException.class, () -> book.wikipediaLinkIn(language));
    }

}
