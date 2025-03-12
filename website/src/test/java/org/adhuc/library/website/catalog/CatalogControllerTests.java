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
            DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS, BULLSHIT_JOBS
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
        when(catalogClient.listBooks()).thenReturn(page);

        mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT))
                .andExpect(model().attributeDoesNotExist(
                        FIRST_PAGE_LINK_ATTRIBUTE,
                        PREVIOUS_PAGE_LINK_ATTRIBUTE,
                        NEXT_PAGE_LINK_ATTRIBUTE,
                        LAST_PAGE_LINK_ATTRIBUTE
                ));

        verify(catalogClient).listBooks();
        verifyNoMoreInteractions(catalogClient);
        verify(navigationSession).switchPage(same(page));
        verifyNoMoreInteractions(navigationSession);
    }

    @ParameterizedTest
    @MethodSource("linksProvider")
    @DisplayName("provide the catalog default page including links")
    void catalogDefaultPageWithLinks(List<Link> links, Map<String, Matcher<String>> linksAttributesMatcher) throws Exception {
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, links);
        when(catalogClient.listBooks()).thenReturn(page);

        var result = mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT));
        for (var attribute : linksAttributesMatcher.keySet()) {
            var matcher = linksAttributesMatcher.get(attribute);
            result.andExpect(model().attribute(attribute, matcher));
        }

        verify(catalogClient).listBooks();
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
        when(catalogClient.listBooks()).thenReturn(page);

        mvc.perform(get("/catalog").param("link", linkToFollow))
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
        var previousPage = new NavigablePageImpl<>(List.of(A_DANCE_WITH_DRAGONS),
                PageRequest.of(0, 10), 4, List.of());
        var page = new NavigablePageImpl<>(PAGE_CONTENT, PageRequest.of(0, 10), 4, List.of());
        when(navigationSession.currentPage()).thenReturn(Optional.of(previousPage));
        when(catalogClient.listBooks(any(), any())).thenReturn(page);

        mvc.perform(get("/catalog").param("link", linkToFollow))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", PAGE_CONTENT));

        verify(catalogClient).listBooks(any(), eq(linkToFollow));
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

        when(catalogClient.listBooks()).thenThrow(exception);

        mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on client error"));

        verify(catalogClient).listBooks();
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

        when(catalogClient.listBooks()).thenThrow(exception);

        mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Some details on server error"));

        verify(catalogClient).listBooks();
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
