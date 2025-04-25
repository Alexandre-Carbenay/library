package org.adhuc.library.catalog.adapter.rest;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.*;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsService;
import org.adhuc.library.catalog.editions.PublicationDate;
import org.adhuc.library.catalog.editions.Publisher;
import org.apache.hc.core5.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("library-catalog")
@PactBroker(url = "http://localhost:9292")
class CatalogPactTests {

    @LocalServerPort
    int port;
    @MockitoBean
    private CatalogService catalogService;
    @MockitoBean
    private BooksService booksService;
    @MockitoBean
    private EditionsService editionsService;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context, HttpRequest request) {
        context.verifyInteraction();
    }

    @State("First page of 10 elements contains books")
    void page0Size10() {
        var bookId = UUID.fromString("b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
        var book = new Book(
                bookId,
                Set.of(new Author(
                        UUID.fromString("83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725"),
                        "Jean-Jacques Rousseau",
                        LocalDate.parse("1712-06-28"),
                        LocalDate.parse("1778-07-02")
                )),
                "fr",
                Set.of(new LocalizedDetails(
                        "fr",
                        "Du contrat social",
                        "Du contrat social est un traité de philosophie politique présentant ...",
                        Set.of(
                                new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Du_contrat_social")
                        )
                ))
        );

        var request = PageRequest.of(0, 10);
        when(catalogService.getPage(request, FRENCH)).thenReturn(new PageImpl<>(List.of(book), request, 67));
    }

    @State("First page of 10 elements in english contains books")
    void page0Size10English() {
        var bookId = UUID.fromString("b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
        var book = new Book(
                bookId,
                Set.of(new Author(
                        UUID.fromString("83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725"),
                        "Jean-Jacques Rousseau",
                        LocalDate.parse("1712-06-28"),
                        LocalDate.parse("1778-07-02")
                )),
                "fr",
                Set.of(new LocalizedDetails(
                        "fr",
                        "Du contrat social",
                        "Du contrat social est un traité de philosophie politique présentant ...",
                        Set.of(
                                new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Du_contrat_social")
                        )
                ), new LocalizedDetails(
                        "en",
                        "The Social Contract",
                        "The Social Contract, originally published as On the Social Contract; or, Principles of Political Right...",
                        Set.of(
                                new ExternalLink("wikipedia", "https://en.wikipedia.org/wiki/The_Social_Contract")
                        )
                ))
        );

        var request = PageRequest.of(0, 10);
        when(catalogService.getPage(request, ENGLISH)).thenReturn(new PageImpl<>(List.of(book), request, 67));
    }

    @State("Next page of 25 elements contains books")
    void page1Size25() {
        var bookId = UUID.fromString("b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
        var book = new Book(
                bookId,
                Set.of(new Author(
                        UUID.fromString("83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725"),
                        "Jean-Jacques Rousseau",
                        LocalDate.parse("1712-06-28"),
                        LocalDate.parse("1778-07-02")
                )),
                "fr",
                Set.of(new LocalizedDetails(
                        "fr",
                        "Du contrat social",
                        "Du contrat social est un traité de philosophie politique présentant ...",
                        Set.of(
                                new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Du_contrat_social")
                        )
                ))
        );

        var request = PageRequest.of(1, 25);
        when(catalogService.getPage(request, FRENCH)).thenReturn(new PageImpl<>(List.of(book), request, 67));
    }

    @State("Book detail is reachable")
    void bookDetail() {
        var bookId = UUID.fromString("b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
        var book = new Book(
                bookId,
                Set.of(new Author(
                        UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"),
                        "Jean-Jacques Rousseau",
                        LocalDate.parse("1712-06-28"),
                        LocalDate.parse("1778-07-02")
                )),
                "fr",
                Set.of(new LocalizedDetails(
                        "fr",
                        "Du contrat social",
                        "Du contrat social est un traité de philosophie politique présentant ...",
                        Set.of(
                                new ExternalLink("wikipedia", "https://fr.wikipedia.org/wiki/Du_contrat_social")
                        )
                ))
        );
        var edition = new Edition(
                "9782290385050",
                "Du contrat social ou Principes du droit politique",
                PublicationDate.of(LocalDate.parse("2023-02-08")),
                book,
                new Publisher(UUID.fromString("c0c990bd-c455-40c4-92e7-0b62befb200d"), "Flammarion"),
                "fr",
                "Comment se forment les sociétés ? Qu'est-ce qui détermine une organisation sociale juste ? Comment concilier les intérêts de chacun et l'intérêt général ?"
        );

        when(booksService.getBook(bookId)).thenReturn(Optional.of(book));
        when(editionsService.getBookEditions(bookId)).thenReturn(List.of(edition));
    }

}
