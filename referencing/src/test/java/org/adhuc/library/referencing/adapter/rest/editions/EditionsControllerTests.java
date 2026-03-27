package org.adhuc.library.referencing.adapter.rest.editions;

import net.datafaker.Faker;
import org.adhuc.library.referencing.editions.*;
import org.adhuc.library.support.rest.pagination.PaginationAutoConfiguration;
import org.adhuc.library.support.rest.validation.RequestValidationAutoConfiguration;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.editions.EditionsMother.Editions.*;
import static org.adhuc.library.referencing.editions.EditionsMother.edition;
import static org.adhuc.library.referencing.editions.EditionsMother.editions;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Tag("restApi")
@WebMvcTest(controllers = {EditionsController.class, EditionModelAssembler.class})
@ImportAutoConfiguration({RequestValidationAutoConfiguration.class, PaginationAutoConfiguration.class, ValidationAutoConfiguration.class})
@DisplayName("Editions controller should")
class EditionsControllerTests {

    private static final Faker FAKER = new Faker();

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private EditionsConsultationService editionsConsultationService;
    @MockitoBean
    private EditionsReferencingService editionsReferencingService;

    @Nested
    @DisplayName("getting editions page")
    class EditionsPage {

        private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.captor();

        @Test
        @DisplayName("provide an empty page when no edition has been referenced")
        void listEditionsEmptyDefaultPage() throws Exception {
            var request = PageRequest.of(0, 50);
            when(editionsConsultationService.getPage(any())).thenReturn(Page.empty(request));

            mvc.perform(get("/api/v1/editions").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(0)))
                    .andExpect(jsonPath("page.total_pages", equalTo(0)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions?page=0&size=50")))
                    .andExpect(jsonPath("_links.first").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last").doesNotExist())
                    .andExpect(jsonPath("_embedded").doesNotExist());

            verify(editionsConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isZero();
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @Test
        @DisplayName("provide a unique page when the number of editions is lower than or equal to the requested page size")
        void uniqueDefaultPage() throws Exception {
            var numberOfElements = FAKER.random().nextInt(1, 50);
            var editions = editions(numberOfElements);

            var request = PageRequest.of(0, 50);
            when(editionsConsultationService.getPage(any())).thenReturn(new PageImpl<>(editions, request, numberOfElements));

            var result = mvc.perform(get("/api/v1/editions").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(numberOfElements)))
                    .andExpect(jsonPath("page.total_pages", equalTo(1)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions?page=0&size=50")))
                    .andExpect(jsonPath("_links.first.href").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last.href").doesNotExist())
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllEditions(result, editions);

            verify(editionsConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isZero();
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @Test
        @DisplayName("provide a page with default page size when not specified explicitly")
        void defaultPageSize() throws Exception {
            var pageIndex = FAKER.random().nextInt(1, 100);
            var editions = pageSample(pageIndex, 50);

            when(editionsConsultationService.getPage(any())).thenReturn(editions);

            var result = mvc.perform(get("/api/v1/editions")
                            .accept("application/hal+json")
                            .queryParam("page", String.valueOf(editions.getNumber())))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(editions.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(editions.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(editions.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(editions.getNumber())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions?page=" + editions.getNumber() + "&size=50")))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllEditions(result, editions.toList());

            verify(editionsConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(editions.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"-50", "-2", "-1"})
        @DisplayName("refuse providing a page with a negative page number")
        void getInvalidPageNumber(String pageNumber) throws Exception {
            mvc.perform(get("/api/v1/editions").accept("application/hal+json")
                            .queryParam("page", pageNumber)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Request validation error")))
                    .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo("Numeric instance is lower than the required minimum (minimum: 0, found: " + pageNumber + ")")))
                    .andExpect(jsonPath("errors[0].parameter", equalTo("page")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-50", "-1", "0"})
        @DisplayName("refuse providing a page with a negative page size")
        void getInvalidPageSize(String pageSize) throws Exception {
            mvc.perform(get("/api/v1/editions").accept("application/hal+json")
                            .queryParam("size", pageSize)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Request validation error")))
                    .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail",
                            equalTo("Numeric instance is lower than the required minimum (minimum: 1, found: " + pageSize + ")")))
                    .andExpect(jsonPath("errors[0].parameter", equalTo("size")));
        }

        @ParameterizedTest
        @CsvSource({"-50,-1", "-2,-50", "-1,0"})
        @DisplayName("refuse providing a page with a negative page number")
        void getInvalidPageNumberAndSize(String pageNumber, String pageSize) throws Exception {
            mvc.perform(get("/api/v1/editions").accept("application/hal+json")
                            .queryParam("page", pageNumber)
                            .queryParam("size", pageSize)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Request validation error")))
                    .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(2)))
                    .andExpect(jsonPath("errors[0].detail",
                            equalTo("Numeric instance is lower than the required minimum (minimum: 0, found: " + pageNumber + ")")))
                    .andExpect(jsonPath("errors[0].parameter", equalTo("page")))
                    .andExpect(jsonPath("errors[1].detail",
                            equalTo("Numeric instance is lower than the required minimum (minimum: 1, found: " + pageSize + ")")))
                    .andExpect(jsonPath("errors[1].parameter", equalTo("size")));
        }

        @Test
        @DisplayName("provide a page corresponding to requested page and size")
        void getPage() throws Exception {
            var pageIndex = FAKER.random().nextInt(0, 100);
            var pageSize = FAKER.random().nextInt(2, 100);
            var editions = pageSample(pageIndex, pageSize);

            when(editionsConsultationService.getPage(any())).thenReturn(editions);

            var result = mvc.perform(get("/api/v1/editions")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(editions.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(editions.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(editions.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions?page=" + pageIndex + "&size=" + pageSize)))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllEditions(result, editions.toList());

            verify(editionsConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(editions.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(editions.getSize());
            });
        }

        @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
        @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
        @DisplayName("provide a page with navigation links")
        void getPageWithNavigation(int pageIndex, int pageSize, Page<Edition> editions, boolean hasFirst, boolean hasPrev,
                                   boolean hasNext, boolean hasLast) throws Exception {
            when(editionsConsultationService.getPage(any())).thenReturn(editions);

            var result = mvc.perform(get("/api/v1/editions")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions?page=" + pageIndex + "&size=" + pageSize)));

            verifyNavigationLink(result, hasFirst, "first", "http://localhost/api/v1/editions?page=0&size=" + pageSize);
            verifyNavigationLink(result, hasPrev, "prev", "http://localhost/api/v1/editions?page=" + (pageIndex - 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasNext, "next", "http://localhost/api/v1/editions?page=" + (pageIndex + 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasLast, "last", "http://localhost/api/v1/editions?page=" + (editions.getTotalPages() - 1) + "&size=" + pageSize);
        }

        static void verifyNavigationLink(ResultActions result, boolean hasLink, String linkName, @Nullable String valueIfExists) throws Exception {
            if (hasLink) {
                result.andExpect(jsonPath("_links." + linkName + ".href", equalTo(requireNonNull(valueIfExists))));
            } else {
                result.andExpect(jsonPath("_links." + linkName).doesNotExist());
            }
        }

        static Stream<Arguments> uniquePagesProvider() {
            var requestedPageSize = FAKER.random().nextInt(2, 100);
            var page = lastPage(0, requestedPageSize);
            return Stream.of(Arguments.of(0, page.getPageable().getPageSize(), page, false, false, false, false));
        }

        static Stream<Arguments> firstPagesProvider() {
            var requestedPageSize = FAKER.random().nextInt(2, 100);
            var page = fullPage(0, requestedPageSize);
            return Stream.of(Arguments.of(0, page.getPageable().getPageSize(), page, true, false, true, true));
        }

        static Stream<Arguments> intermediatePagesProvider() {
            var pageIndex = FAKER.random().nextInt(1, 100);
            var requestedPageSize = FAKER.random().nextInt(2, 100);
            var page = fullPage(pageIndex, requestedPageSize);
            return Stream.of(Arguments.of(page.getNumber(), page.getPageable().getPageSize(), page, true, true, true, true));
        }

        static Stream<Arguments> lastPagesProvider() {
            var pageIndex = FAKER.random().nextInt(1, 100);
            var requestedPageSize = FAKER.random().nextInt(2, 100);
            var page = lastPage(pageIndex, requestedPageSize);
            return Stream.of(Arguments.of(page.getNumber(), page.getPageable().getPageSize(), page, true, true, false, true));
        }

        private static Page<Edition> pageSample(int pageIndex, int requestedPageSize) {
            var isLastPage = FAKER.bool().bool();
            if (isLastPage) {
                return lastPage(pageIndex, requestedPageSize);
            }
            return fullPage(pageIndex, requestedPageSize);
        }

        private static PageImpl<Edition> lastPage(int page, int requestedPageSize) {
            int pageSize = FAKER.random().nextInt(1, requestedPageSize);
            int totalRows = pageSize;
            if (page > 0) {
                totalRows += requestedPageSize * page;
            }
            var elements = editions(pageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        private static PageImpl<Edition> fullPage(int page, int requestedPageSize) {
            var totalPages = FAKER.random().nextInt(page + 1, 150);
            var lastPageSize = FAKER.random().nextInt(1, requestedPageSize - 1);
            var totalRows = totalPages * requestedPageSize + lastPageSize;
            var elements = editions(requestedPageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        public static void assertResponseContainsAllEditions(ResultActions result, Collection<Edition> expectedEditions) throws Exception {
            result.andExpect(jsonPath("_embedded.editions").exists())
                    .andExpect(jsonPath("_embedded.editions").isArray())
                    .andExpect(jsonPath("_embedded.editions", hasSize(expectedEditions.size())));
            for (var edition : expectedEditions) {
                result.andExpect(jsonPath(
                        "_embedded.editions.[" +
                                "?(@.isbn == \"%s\" " +
                                "&& @.language == \"%s\" " +
                                "&& @.book == \"%s\" " +
                                "&& @.publisher == \"%s\" " +
                                "&& @.publication_date == \"%s\" " +
                                "&& @.title == \"%s\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/editions/%s\")]",
                        edition.isbn(), edition.language(), edition.book(), edition.publisher(), edition.publicationDate(), edition.title(), edition.isbn()).exists());
            }
        }

    }

    @Nested
    @DisplayName("referencing new edition")
    class EditionReferencing {

        private final ArgumentCaptor<ReferenceEdition> referenceCommandCaptor = ArgumentCaptor.captor();

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("refuse referencing edition when request is invalid")
        void referenceEditionInvalidRequest(String scenario, String requestBody, String expectedDetail, String expectedPointer) throws Exception {
            mvc.perform(post("/api/v1/editions")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Request validation error")))
                    .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo(expectedDetail)))
                    .andExpect(jsonPath("errors[0].pointer", equalTo(expectedPointer)));
        }

        private static Stream<Arguments> invalidRequestProvider() {
            return Stream.of(
                    Arguments.of("ISBN is missing", missingIsbn(), "Missing required property", "/isbn"),
                    Arguments.of("ISBN is empty", emptyIsbn(), "\" \" must be a valid ISBN", "/isbn"),
                    Arguments.of("ISBN is invalid", invalidIsbn(), "\"invalid\" must be a valid ISBN", "/isbn"),
                    Arguments.of("language is missing", missingLanguage(), "Missing required property", "/language"),
                    Arguments.of("language is blank", emptyLanguage(), "\" \" must not be blank", "/language"),
                    Arguments.of("book is missing", missingBook(), "Missing required property", "/book"),
                    Arguments.of("book is blank", emptyBook(), "Input string \"\" is not a valid UUID", "/book"),
                    Arguments.of("book is invalid", invalidBook(), "Input string \"invalid\" is not a valid UUID", "/book"),
                    Arguments.of("publisher is missing", missingPublisher(), "Missing required property", "/publisher"),
                    Arguments.of("publisher is blank", emptyPublisher(), "Input string \"\" is not a valid UUID", "/publisher"),
                    Arguments.of("publisher is invalid", invalidPublisher(), "Input string \"invalid\" is not a valid UUID", "/publisher"),
                    Arguments.of("publication date is missing", missingPublicationDate(), "Missing required property", "/publication_date"),
                    Arguments.of("publication date is blank", emptyPublicationDate(), "String \"\" is invalid against requested date format(s) yyyy-MM-dd", "/publication_date"),
                    Arguments.of("title is blank", emptyTitle(), "\" \" must be null or not blank", "/title"),
                    Arguments.of("summary is blank", emptySummary(), "\" \" must be null or not blank", "/summary")
            );
        }

        private static String missingIsbn() {
            return """
                    {
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(language(), book(), publisher(), publicationDate(), title(), summary());
        }

        private static String emptyIsbn() {
            return """
                    {
                        "isbn": " ",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(language(), book(), publisher(), publicationDate(), title(), summary());
        }

        private static String invalidIsbn() {
            return """
                    {
                        "isbn": "invalid",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(language(), book(), publisher(), publicationDate(), title(), summary());
        }

        private static String missingLanguage() {
            return """
                    {
                        "isbn": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), book(), publisher(), publicationDate(), title(), summary());
        }

        private static String emptyLanguage() {
            return """
                    {
                        "isbn": "%s",
                        "language": " ",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), book(), publisher(), publicationDate(), title(), summary());
        }

        private static String missingBook() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), publisher(), publicationDate(), title(), summary());
        }

        private static String emptyBook() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), publisher(), publicationDate(), title(), summary());
        }

        private static String invalidBook() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "invalid",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), publisher(), publicationDate(), title(), summary());
        }

        private static String missingPublisher() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publicationDate(), title(), summary());
        }

        private static String emptyPublisher() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publicationDate(), title(), summary());
        }

        private static String invalidPublisher() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "invalid",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publicationDate(), title(), summary());
        }

        private static String missingPublicationDate() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publisher(), title(), summary());
        }

        private static String emptyPublicationDate() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publisher(), title(), summary());
        }

        private static String emptyTitle() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": " ",
                        "summary": "%s"
                    }""".formatted(isbn(), language(), book(), publisher(), publicationDate(), summary());
        }

        private static String emptySummary() {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": " "
                    }""".formatted(isbn(), language(), book(), publisher(), publicationDate(), title());
        }

        @Test
        @DisplayName("reference duplicated edition")
        void referenceDuplicatedEdition() throws Exception {
            var isbn = isbn();
            var requestBody = """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn, language(), book(), publisher(), publicationDate(), title(), summary());
            when(editionsReferencingService.referenceEdition(any())).thenThrow(new DuplicateEditionException(isbn));

            mvc.perform(post("/api/v1/editions")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/duplicate-edition")))
                    .andExpect(jsonPath("status", equalTo(409)))
                    .andExpect(jsonPath("title", equalTo("Duplicate edition")))
                    .andExpect(jsonPath("detail", equalTo("Edition with ISBN already exists")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo("Edition with ISBN '%s' already exists".formatted(isbn))))
                    .andExpect(jsonPath("errors[0].pointer", equalTo("/isbn")));
        }

        @Test
        @DisplayName("reference edition with unknown book")
        void referenceEditionUnknownBook() throws Exception {
            var isbn = isbn();
            var bookId = book();
            var requestBody = """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn, language(), bookId, publisher(), publicationDate(), title(), summary());
            when(editionsReferencingService.referenceEdition(any())).thenThrow(new UnknownEditionBookException(isbn, bookId));

            mvc.perform(post("/api/v1/editions")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/unknown-edition-book")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Unknown edition book")))
                    .andExpect(jsonPath("detail", equalTo("Referenced edition's book does not exist")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo("Edition with ISBN '%s' cannot be referenced for unknown book '%s'".formatted(isbn, bookId))))
                    .andExpect(jsonPath("errors[0].pointer", equalTo("/book")));
        }

        @Test
        @DisplayName("reference edition with unknown publisher")
        void referenceEditionUnknownPublisher() throws Exception {
            var isbn = isbn();
            var publisherId = publisher();
            var requestBody = """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(isbn, language(), book(), publisherId, publicationDate(), title(), summary());
            when(editionsReferencingService.referenceEdition(any())).thenThrow(new UnknownEditionPublisherException(isbn, publisherId));

            mvc.perform(post("/api/v1/editions")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/unknown-edition-publisher")))
                    .andExpect(jsonPath("status", equalTo(400)))
                    .andExpect(jsonPath("title", equalTo("Unknown edition publisher")))
                    .andExpect(jsonPath("detail", equalTo("Referenced edition's publisher does not exist")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo("Edition with ISBN '%s' cannot be referenced for unknown publisher '%s'".formatted(isbn, publisherId))))
                    .andExpect(jsonPath("errors[0].pointer", equalTo("/publisher")));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("validRequestProvider")
        @DisplayName("reference edition successfully when request is valid")
        void referenceEditionSuccess(String scenario, String requestBody, Edition edition, BiFunction<Edition, ReferenceEdition, Consumer<SoftAssertions>> assertions) throws Exception {
            when(editionsReferencingService.referenceEdition(any())).thenReturn(edition);

            mvc.perform(post("/api/v1/editions")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/editions/" + edition.isbn())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("isbn", equalTo(edition.isbn())))
                    .andExpect(jsonPath("language", equalTo(edition.language())))
                    .andExpect(jsonPath("book", equalTo(edition.book().toString())))
                    .andExpect(jsonPath("publisher", equalTo(edition.publisher().toString())))
                    .andExpect(jsonPath("publication_date", equalTo(edition.publicationDate().toString())))
                    .andExpect(jsonPath("title", equalTo(edition.title())))
                    .andExpect(jsonPath("summary", equalTo(edition.summary())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/editions/" + edition.isbn())));

            verify(editionsReferencingService).referenceEdition(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(assertions.apply(edition, actual));
        }

        private static Stream<Arguments> validRequestProvider() {
            var edition = edition();
            return Stream.of(
                    Arguments.of("Complete request", completeRequest(edition), edition, assertCompleteRequest()),
                    Arguments.of("Request without title and summary", requestWithoutTitleAndSummary(edition), edition, assertRequestWithoutTitleAndSummary())
            );
        }

        private static String completeRequest(Edition edition) {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s",
                        "title": "%s",
                        "summary": "%s"
                    }""".formatted(edition.isbn(), edition.language(), edition.book(), edition.publisher(), edition.publicationDate(), edition.title(), edition.summary());
        }

        private static BiFunction<Edition, ReferenceEdition, Consumer<SoftAssertions>> assertCompleteRequest() {
            return (edition, command) -> assertion -> {
                assertion.assertThat(command.isbn()).isEqualTo(edition.isbn());
                assertion.assertThat(command.language()).isEqualTo(edition.language());
                assertion.assertThat(command.book()).isEqualTo(edition.book());
                assertion.assertThat(command.publisher()).isEqualTo(edition.publisher());
                assertion.assertThat(command.publicationDate()).isEqualTo(edition.publicationDate());
                assertion.assertThat(command.title()).isPresent().contains(edition.title());
                assertion.assertThat(command.summary()).isPresent().contains(edition.summary());
            };
        }

        private static String requestWithoutTitleAndSummary(Edition edition) {
            return """
                    {
                        "isbn": "%s",
                        "language": "%s",
                        "book": "%s",
                        "publisher": "%s",
                        "publication_date": "%s"
                    }""".formatted(edition.isbn(), edition.language(), edition.book(), edition.publisher(), edition.publicationDate());
        }

        private static BiFunction<Edition, ReferenceEdition, Consumer<SoftAssertions>> assertRequestWithoutTitleAndSummary() {
            return (edition, command) -> assertion -> {
                assertion.assertThat(command.isbn()).isEqualTo(edition.isbn());
                assertion.assertThat(command.language()).isEqualTo(edition.language());
                assertion.assertThat(command.book()).isEqualTo(edition.book());
                assertion.assertThat(command.publisher()).isEqualTo(edition.publisher());
                assertion.assertThat(command.publicationDate()).isEqualTo(edition.publicationDate());
                assertion.assertThat(command.title()).isEmpty();
                assertion.assertThat(command.summary()).isEmpty();
            };
        }

    }

}
