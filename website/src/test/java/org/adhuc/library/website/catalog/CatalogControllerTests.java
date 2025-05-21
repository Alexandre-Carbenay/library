package org.adhuc.library.website.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.website.support.pagination.NavigablePageImpl;
import org.adhuc.library.website.support.pagination.NavigablePageImpl.Link;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.website.catalog.BooksMother.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CatalogController.class)
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    private static final String FIRST_PAGE_LINK_ATTRIBUTE = "firstPageLinkName";
    private static final String PREVIOUS_PAGE_LINK_ATTRIBUTE = "previousPageLinkName";
    private static final String NEXT_PAGE_LINK_ATTRIBUTE = "nextPageLinkName";
    private static final String LAST_PAGE_LINK_ATTRIBUTE = "lastPageLinkName";

    private static final List<Book> PAGE_CONTENT = List.of(
            DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS_FR, BULLSHIT_JOBS
    );

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CatalogClient catalogClient;
    @MockitoBean
    private NavigationSession navigationSession;

    @Test
    @DisplayName("provide the catalog default page")
    void catalogDefaultPage() throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(catalogClient.listBooks(any())).thenReturn(page);

        mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attribute("pageUrl", "/catalog/0"))
                .andExpect(model().attributeDoesNotExist(
                        FIRST_PAGE_LINK_ATTRIBUTE,
                        PREVIOUS_PAGE_LINK_ATTRIBUTE,
                        NEXT_PAGE_LINK_ATTRIBUTE,
                        LAST_PAGE_LINK_ATTRIBUTE
                ));

        verify(catalogClient).listBooks(eq(""));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fr",
            "en",
            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"
    })
    @DisplayName("provide the catalog default page with accept languages")
    void catalogDefaultPageWithAcceptLanguages(String acceptLanguages) throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(catalogClient.listBooks(any())).thenReturn(page);

        mvc.perform(get("/catalog").header("Accept-Language", acceptLanguages))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attribute("pageUrl", "/catalog/0"))
                .andExpect(model().attributeDoesNotExist(
                        FIRST_PAGE_LINK_ATTRIBUTE,
                        PREVIOUS_PAGE_LINK_ATTRIBUTE,
                        NEXT_PAGE_LINK_ATTRIBUTE,
                        LAST_PAGE_LINK_ATTRIBUTE
                ));

        verify(catalogClient).listBooks(eq(acceptLanguages));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @ParameterizedTest
    @MethodSource("linksProvider")
    @DisplayName("provide the catalog default page including links")
    void catalogDefaultPageWithLinks(List<Link> links, Map<String, Matcher<String>> linksAttributesMatcher) throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, links);
        when(catalogClient.listBooks(any())).thenReturn(page);

        var result = mvc.perform(get("/catalog").header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attribute("pageUrl", "/catalog/0"));
        for (var attribute : linksAttributesMatcher.keySet()) {
            var matcher = linksAttributesMatcher.get(attribute);
            result.andExpect(model().attribute(attribute, matcher));
        }

        verify(catalogClient).listBooks(eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    private static Stream<Arguments> linksProvider() {
        var first = new Link("first", "linkToFirst");
        var previous = new Link("prev", "linkToPrevious");
        var next = new Link("next", "linkToNext");
        var last = new Link("last", "linkToLast");
        return Stream.of(
                Arguments.of(
                        List.of(new Link("unknown", "unknownLink")),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(first),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(first, previous),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("first")),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("prev")),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(previous),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("prev")),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(next),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("next")),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(last),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(next, last),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("next")),
                                LAST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("last"))
                        )
                ),
                Arguments.of(
                        List.of(first, previous, next, last),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("first")),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("prev")),
                                NEXT_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("next")),
                                LAST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("last"))
                        )
                ),
                Arguments.of(
                        List.of(first, previous),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("first")),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("prev")),
                                NEXT_PAGE_LINK_ATTRIBUTE, nullValue(),
                                LAST_PAGE_LINK_ATTRIBUTE, nullValue()
                        )
                ),
                Arguments.of(
                        List.of(next, last),
                        Map.of(
                                FIRST_PAGE_LINK_ATTRIBUTE, nullValue(),
                                PREVIOUS_PAGE_LINK_ATTRIBUTE, nullValue(),
                                NEXT_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("next")),
                                LAST_PAGE_LINK_ATTRIBUTE, both(notNullValue()).and(equalTo("last"))
                        )
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "prev", "next", "last"})
    @DisplayName("provide the catalog default page when navigating through a link, without previous navigation session")
    void catalogPageWithoutPreviousSession(String linkToFollow) throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(catalogClient.listBooks(any())).thenReturn(page);

        mvc.perform(get("/catalog").param("link", linkToFollow)
                        .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/catalog"));

        verifyNoInteractions(catalogClient);
        verify(navigationSession).currentPage();
        verify(navigationSession).clearCurrentPage();
        verifyNoMoreInteractions(navigationSession);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "prev", "next", "last"})
    @DisplayName("provide the catalog page when navigating through a link, with previous navigation session")
    void catalogPageWithPageInPreviousSession(String linkToFollow) throws Exception {
        var previousPage = new NavigablePageImpl<>(List.of(A_DANCE_WITH_DRAGONS_FR),
                PageRequest.of(0, 10), 4, List.of());
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(navigationSession.currentPage()).thenReturn(Optional.of(previousPage));
        when(catalogClient.listBooks(any(), any(), any())).thenReturn(page);

        mvc.perform(get("/catalog").param("link", linkToFollow)
                        .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attribute("pageUrl", "/catalog/0"));

        verify(catalogClient).listBooks(any(), eq(linkToFollow), eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).currentPage();
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "first|fr",
            "first|en",
            "first|fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
            "prev|fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
            "next|fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
            "last|fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"
    })
    @DisplayName("provide the catalog page when navigating through a link, with previous navigation session, with accept languages")
    void catalogPageWithPageInPreviousSessionAcceptLanguages(String linkToFollow, String acceptLanguages) throws Exception {
        var previousPage = new NavigablePageImpl<>(List.of(A_DANCE_WITH_DRAGONS_FR),
                PageRequest.of(0, 10), 4, List.of());
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(navigationSession.currentPage()).thenReturn(Optional.of(previousPage));
        when(catalogClient.listBooks(any(), any(), any())).thenReturn(page);

        mvc.perform(get("/catalog").param("link", linkToFollow)
                        .header("Accept-Language", acceptLanguages))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attribute("pageUrl", "/catalog/0"));

        verify(catalogClient).listBooks(any(), eq(linkToFollow), eq(acceptLanguages));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).currentPage();
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises client error")
    void catalogClientError() throws Exception {
        var body = """
                {
                    "type": "/problems/bad-request",
                    "status": "400",
                    "title": "Test error",
                    "detail": "Some details on client error"
                }
                """;
        var exception = prepareException(
                HttpClientErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.listBooks(any())).thenThrow(exception);

        mvc.perform(get("/catalog").header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on client error"));

        verify(catalogClient).listBooks(eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises server error")
    void catalogServerError() throws Exception {
        var body = """
                {
                    "type": "/problems/internal-server-error",
                    "status": "500",
                    "title": "Test error",
                    "detail": "Some details on server error"
                }
                """;
        var exception = prepareException(
                HttpServerErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.listBooks(any())).thenThrow(exception);

        mvc.perform(get("/catalog").header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on server error"));

        verify(catalogClient).listBooks(eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    @ParameterizedTest
    @CsvSource({
            "0,fr",
            "0,en",
            "0,fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
            "1,fr",
            "1,en",
            "1,fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
            "10,fr",
            "10,en",
            "10,fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"
    })
    @DisplayName("provide the catalog expected page")
    void catalogPage(int pageNumber, String acceptLanguages) throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(pageNumber, 10), 4, List.of());
        when(catalogClient.listBooks(anyInt(), any())).thenReturn(page);

        mvc.perform(get("/catalog/{page}", pageNumber)
                        .header("Accept-Language", acceptLanguages))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("pageUrl", "/catalog/" + pageNumber))
                .andExpect(model().attribute("books", PAGE_CONTENT));

        verify(catalogClient).listBooks(eq(pageNumber), eq(acceptLanguages));
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises client error while getting page")
    void catalogPageClientError() throws Exception {
        var body = """
                {
                    "type": "/problems/bad-request",
                    "status": "400",
                    "title": "Test error",
                    "detail": "Some details on client error"
                }
                """;
        var exception = prepareException(
                HttpClientErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.listBooks(anyInt(), any())).thenThrow(exception);

        mvc.perform(get("/catalog/1").header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on client error"));

        verify(catalogClient).listBooks(eq(1), eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises server error while getting page")
    void catalogPageServerError() throws Exception {
        var body = """
                {
                    "type": "/problems/internal-server-error",
                    "status": "500",
                    "title": "Test error",
                    "detail": "Some details on server error"
                }
                """;
        var exception = prepareException(
                HttpServerErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.listBooks(anyInt(), any())).thenThrow(exception);

        mvc.perform(get("/catalog/1").header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on server error"));

        verify(catalogClient).listBooks(eq(1), eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    @Test
    @DisplayName("provide the book detail in default language")
    void bookDetails() throws Exception {
        when(catalogClient.getBook(any(), any())).thenReturn(DU_CONTRAT_SOCIAL);

        var bookId = UUID.randomUUID().toString();
        mvc.perform(get("/catalog/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/book-detail"))
                .andExpect(model().attribute("book", DU_CONTRAT_SOCIAL));

        verify(catalogClient).getBook(eq(bookId), eq(""));
        verifyNoMoreInteractions(catalogClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fr",
            "en",
            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"
    })
    @DisplayName("provide the book detail with accept languages")
    void bookDetailsWithAcceptLanguages(String acceptLanguages) throws Exception {
        when(catalogClient.getBook(any(), any())).thenReturn(DU_CONTRAT_SOCIAL);

        var bookId = UUID.randomUUID().toString();
        mvc.perform(get("/catalog/books/{id}", bookId).header("Accept-Language", acceptLanguages))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/book-detail"))
                .andExpect(model().attribute("book", DU_CONTRAT_SOCIAL));

        verify(catalogClient).getBook(eq(bookId), eq(acceptLanguages));
        verifyNoMoreInteractions(catalogClient);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises client error while retrieving book detail")
    void bookDetailsClientError() throws Exception {
        var body = """
                {
                    "type": "/problems/bad-request",
                    "status": "400",
                    "title": "Test error",
                    "detail": "Some details on client error"
                }
                """;
        var exception = prepareException(
                HttpClientErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.getBook(any(), any())).thenThrow(exception);

        var bookId = UUID.randomUUID().toString();
        mvc.perform(get("/catalog/books/{id}", bookId).header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on client error"));

        verify(catalogClient).getBook(eq(bookId), eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    @Test
    @DisplayName("provide the error page with error details when catalog client raises server error while retrieving book detail")
    void bookDetailsServerError() throws Exception {
        var body = """
                {
                    "type": "/problems/internal-server-error",
                    "status": "500",
                    "title": "Test error",
                    "detail": "Some details on server error"
                }
                """;
        var exception = prepareException(
                HttpServerErrorException.create(BAD_REQUEST, "Bad request",
                        jsonProblemResponseHeaders(), body.getBytes(), Charset.defaultCharset()
                ),
                body
        );

        when(catalogClient.getBook(any(), any())).thenThrow(exception);

        var bookId = UUID.randomUUID().toString();
        mvc.perform(get("/catalog/books/{id}", bookId).header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on server error"));

        verify(catalogClient).getBook(eq(bookId), eq("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
        verifyNoMoreInteractions(catalogClient);
    }

    private <T extends HttpStatusCodeException> T prepareException(T exception, String body) {
        exception.setBodyConvertFunction(resolvableType -> {
            try {
                return new ObjectMapper().reader().forType(resolvableType.getType()).readValue(body);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return exception;
    }

    private HttpHeaders jsonProblemResponseHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_PROBLEM_JSON);
        return headers;
    }

}
