package org.adhuc.library.catalog.adapter.rest.catalog;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.CatalogService;
import org.adhuc.library.catalog.books.PublicationDate;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("library-catalog")
@PactBroker(url = "http://localhost:9292")
class CatalogControllerPactTests {

    @LocalServerPort
    int port;
    @MockitoBean
    private CatalogService catalogService;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context, HttpRequest request) {
        context.verifyInteraction();
    }

    @State("Catalog contains books")
    void defaultWebsitePageState() {
        var request = PageRequest.of(0, 10);
        when(catalogService.getPage(request)).thenReturn(new PageImpl<>(List.of(
                new Book(
                        "9782081275232",
                        "Du contrat social",
                        PublicationDate.of(LocalDate.parse("2012-01-04")),
                        Set.of(new Author(
                                UUID.fromString("83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725"),
                                "Jean-Jacques Rousseau",
                                LocalDate.parse("1712-06-28"),
                                LocalDate.parse("1778-07-02")
                        )),
                        "fr",
                        "Paru en 1762, le Contrat social, ..."
                )
        ), request, 67));
    }

}
