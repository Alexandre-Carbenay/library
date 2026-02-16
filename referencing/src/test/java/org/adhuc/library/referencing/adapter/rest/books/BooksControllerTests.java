package org.adhuc.library.referencing.adapter.rest.books;

import net.datafaker.Faker;
import org.adhuc.library.referencing.books.Book;
import org.adhuc.library.referencing.books.BooksConsultationService;
import org.adhuc.library.referencing.books.BooksMother.Books;
import org.adhuc.library.referencing.books.BooksReferencingService;
import org.adhuc.library.referencing.books.ReferenceBook;
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
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.adhuc.library.referencing.books.BooksMother.books;
import static org.adhuc.library.referencing.books.BooksMother.builder;
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
@WebMvcTest(controllers = {BooksController.class, BookModelAssembler.class})
@ImportAutoConfiguration({RequestValidationAutoConfiguration.class, PaginationAutoConfiguration.class, ValidationAutoConfiguration.class})
@DisplayName("Books controller should")
class BooksControllerTests {

    private static final Faker FAKER = new Faker();

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private BooksConsultationService booksConsultationService;
    @MockitoBean
    private BooksReferencingService booksReferencingService;

    @Nested
    @DisplayName("getting books page")
    class BooksPage {

        private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.captor();

        @Test
        @DisplayName("provide an empty page when no book has been referenced")
        void listBooksEmptyDefaultPage() throws Exception {
            var request = PageRequest.of(0, 50);
            when(booksConsultationService.getPage(any())).thenReturn(Page.empty(request));

            mvc.perform(get("/api/v1/books").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(0)))
                    .andExpect(jsonPath("page.total_pages", equalTo(0)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books?page=0&size=50")))
                    .andExpect(jsonPath("_links.first").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last").doesNotExist())
                    .andExpect(jsonPath("_embedded").doesNotExist());

            verify(booksConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isZero();
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @Test
        @DisplayName("provide a unique page when the number of books is lower than or equal to the requested page size")
        void uniqueDefaultPage() throws Exception {
            var numberOfElements = FAKER.random().nextInt(1, 50);
            var books = books(numberOfElements);

            var request = PageRequest.of(0, 50);
            when(booksConsultationService.getPage(any())).thenReturn(new PageImpl<>(books, request, numberOfElements));

            var result = mvc.perform(get("/api/v1/books").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(numberOfElements)))
                    .andExpect(jsonPath("page.total_pages", equalTo(1)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books?page=0&size=50")))
                    .andExpect(jsonPath("_links.first.href").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last.href").doesNotExist())
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllBooks(result, books);

            verify(booksConsultationService).getPage(pageableCaptor.capture());
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
            var books = pageSample(pageIndex, 50);

            when(booksConsultationService.getPage(any())).thenReturn(books);

            var result = mvc.perform(get("/api/v1/books")
                            .accept("application/hal+json")
                            .queryParam("page", String.valueOf(books.getNumber())))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(books.getNumber())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books?page=" + books.getNumber() + "&size=50")))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllBooks(result, books.toList());

            verify(booksConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"-50", "-2", "-1"})
        @DisplayName("refuse providing a page with a negative page number")
        void getInvalidPageNumber(String pageNumber) throws Exception {
            mvc.perform(get("/api/v1/books").accept("application/hal+json")
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
            mvc.perform(get("/api/v1/books").accept("application/hal+json")
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
            mvc.perform(get("/api/v1/books").accept("application/hal+json")
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
            var books = pageSample(pageIndex, pageSize);

            when(booksConsultationService.getPage(any())).thenReturn(books);

            var result = mvc.perform(get("/api/v1/books")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books?page=" + pageIndex + "&size=" + pageSize)))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllBooks(result, books.toList());

            verify(booksConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(books.getSize());
            });
        }

        @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
        @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
        @DisplayName("provide a page with navigation links")
        void getPageWithNavigation(int pageIndex, int pageSize, Page<Book> books, boolean hasFirst, boolean hasPrev,
                                   boolean hasNext, boolean hasLast) throws Exception {
            when(booksConsultationService.getPage(any())).thenReturn(books);

            var result = mvc.perform(get("/api/v1/books")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books?page=" + pageIndex + "&size=" + pageSize)));

            verifyNavigationLink(result, hasFirst, "first", "http://localhost/api/v1/books?page=0&size=" + pageSize);
            verifyNavigationLink(result, hasPrev, "prev", "http://localhost/api/v1/books?page=" + (pageIndex - 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasNext, "next", "http://localhost/api/v1/books?page=" + (pageIndex + 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasLast, "last", "http://localhost/api/v1/books?page=" + (books.getTotalPages() - 1) + "&size=" + pageSize);
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

        private static Page<Book> pageSample(int pageIndex, int requestedPageSize) {
            var isLastPage = FAKER.bool().bool();
            if (isLastPage) {
                return lastPage(pageIndex, requestedPageSize);
            }
            return fullPage(pageIndex, requestedPageSize);
        }

        private static PageImpl<Book> lastPage(int page, int requestedPageSize) {
            int pageSize = FAKER.random().nextInt(1, requestedPageSize);
            int totalRows = pageSize;
            if (page > 0) {
                totalRows += requestedPageSize * page;
            }
            var elements = books(pageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        private static PageImpl<Book> fullPage(int page, int requestedPageSize) {
            var totalPages = FAKER.random().nextInt(page + 1, 150);
            var lastPageSize = FAKER.random().nextInt(1, requestedPageSize - 1);
            var totalRows = totalPages * requestedPageSize + lastPageSize;
            var elements = books(requestedPageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        public static void assertResponseContainsAllBooks(ResultActions result, Collection<Book> expectedBooks) throws Exception {
            result.andExpect(jsonPath("_embedded.books").exists())
                    .andExpect(jsonPath("_embedded.books").isArray())
                    .andExpect(jsonPath("_embedded.books", hasSize(expectedBooks.size())));
            for (var book : expectedBooks) {
                // TODO expectation with all fields in book
                result.andExpect(jsonPath(
                        "_embedded.books.[" +
                                "?(@.id == \"%s\" " +
                                "&& @.original_language == \"%s\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/books/%s\")]",
                        book.id(), book.originalLanguage(), book.id()).exists());
            }
        }

    }

    @Nested
    @DisplayName("referencing new book")
    class BookReferencing {

        private final ArgumentCaptor<ReferenceBook> referenceCommandCaptor = ArgumentCaptor.captor();

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("refuse referencing book when request is invalid")
        void referenceBookInvalidRequest(String scenario, String requestBody, String expectedDetail, String expectedPointer) throws Exception {
            mvc.perform(post("/api/v1/books")
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
                    Arguments.of("authors list is missing", missingAuthors(), "Missing required property", "/authors"),
                    Arguments.of("authors list is empty", emptyAuthors(), "Array is too short: must have at least 1 elements but instance has 0 elements", "/authors"),
                    Arguments.of("original language is missing", missingOriginalLanguage(), "Missing required property", "/original_language"),
                    Arguments.of("original language is blank", emptyOriginalLanguage(), "\" \" must not be blank", "/original_language"),
                    Arguments.of("details is missing", missingDetails(), "Missing required property", "/details"),
                    Arguments.of("details is empty", emptyDetails(), "Array is too short: must have at least 1 elements but instance has 0 elements", "/details"),
                    Arguments.of("language in detail is missing", missingDetailLanguage(), "Missing required property", "/details/0/language"),
                    Arguments.of("language in detail is blank", emptyDetailLanguage(), "\" \" must not be blank", "/details/0/language"),
                    Arguments.of("multiple details with the same language (fr)", duplicateDetailsLanguage("fr"), "\"fr\" language is duplicated in details", "/details/1/language"),
                    Arguments.of("multiple details with the same language (it)", duplicateDetailsLanguage("it"), "\"it\" language is duplicated in details", "/details/1/language"),
                    Arguments.of("title is missing", missingTitle(), "Missing required property", "/details/0/title"),
                    Arguments.of("title is blank", emptyTitle(), "\" \" must not be blank", "/details/0/title"),
                    Arguments.of("description is missing", missingDescription(), "Missing required property", "/details/0/description"),
                    Arguments.of("description is blank", emptyDescription(), "\" \" must not be blank", "/details/0/description")
            );
        }

        private static String missingAuthors() {
            var language = Books.language();
            return """
                    {
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(language, language, Books.title(), Books.description());
        }

        private static String emptyAuthors() {
            var language = Books.language();
            return """
                    {
                        "authors": [],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(language, language, Books.title(), Books.description());
        }

        private static String missingOriginalLanguage() {
            return """
                    {
                        "authors": ["%s"],
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.title(), Books.description());
        }

        private static String emptyOriginalLanguage() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": " ",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.title(), Books.description());
        }

        private static String missingDetails() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s"
                    }
                    """.formatted(Books.author(), Books.language());
        }

        private static String emptyDetails() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": []
                    }
                    """.formatted(Books.author(), Books.language());
        }

        private static String missingDetailLanguage() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.title(), Books.description());
        }

        private static String emptyDetailLanguage() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": " ",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.title(), Books.description());
        }

        private static String duplicateDetailsLanguage(String language) {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        },
                        {
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), language, language, Books.title(), Books.description(), language, Books.title(), Books.description());
        }

        private static String missingTitle() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.language(), Books.description());
        }

        private static String emptyTitle() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": " ",
                            "description": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.language(), Books.description());
        }

        private static String missingDescription() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s"
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.language(), Books.title());
        }

        private static String emptyDescription() {
            return """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": " "
                        }]
                    }
                    """.formatted(Books.author(), Books.language(), Books.language(), Books.title());
        }

        @Test
        @DisplayName("reference book with one author and one detail successfully when request is valid")
        void referenceBookOneAuthorOneDetailSuccess() throws Exception {
            var book = builder().oneAuthor().detailInOriginalLanguage().build();
            var author = book.authors().stream().findFirst().orElseThrow();
            var detail = book.details().stream().findFirst().orElseThrow();

            when(booksReferencingService.referenceBook(any())).thenReturn(book);

            var requestBody = """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(author, book.originalLanguage(), detail.language(), detail.title(), detail.description());
            mvc.perform(post("/api/v1/books")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/books/" + book.id())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("id", equalTo(book.id().toString())))
                    .andExpect(jsonPath("authors").isArray())
                    .andExpect(jsonPath("authors", hasSize(1)))
                    .andExpect(jsonPath("authors[0]", equalTo(author.toString())))
                    .andExpect(jsonPath("details").isArray())
                    .andExpect(jsonPath("details", hasSize(1)))
                    .andExpect(jsonPath("details[0].language", equalTo(detail.language())))
                    .andExpect(jsonPath("details[0].title", equalTo(detail.title())))
                    .andExpect(jsonPath("details[0].description", equalTo(detail.description())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.id())));

            verify(booksReferencingService).referenceBook(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.authors()).containsExactlyInAnyOrderElementsOf(book.authors());
                s.assertThat(actual.originalLanguage()).isEqualTo(book.originalLanguage());
                s.assertThat(actual.details()).containsExactlyInAnyOrderElementsOf(book.details());
            });
        }

        @Test
        @DisplayName("reference book with one author and one detail with different language than original language successfully")
        void referenceBookOneAuthorOneDetailDifferentThanOriginalLanguageSuccess() throws Exception {
            var book = builder().oneAuthor().detailInOtherLanguage().build();
            var author = book.authors().stream().findFirst().orElseThrow();
            var detail = book.details().stream().findFirst().orElseThrow();

            when(booksReferencingService.referenceBook(any())).thenReturn(book);

            var requestBody = """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(author, book.originalLanguage(), detail.language(), detail.title(), detail.description());
            mvc.perform(post("/api/v1/books")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/books/" + book.id())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("id", equalTo(book.id().toString())))
                    .andExpect(jsonPath("authors").isArray())
                    .andExpect(jsonPath("authors", hasSize(1)))
                    .andExpect(jsonPath("authors[0]", equalTo(author.toString())))
                    .andExpect(jsonPath("details").isArray())
                    .andExpect(jsonPath("details", hasSize(1)))
                    .andExpect(jsonPath("details[0].language", equalTo(detail.language())))
                    .andExpect(jsonPath("details[0].title", equalTo(detail.title())))
                    .andExpect(jsonPath("details[0].description", equalTo(detail.description())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.id())));

            verify(booksReferencingService).referenceBook(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.authors()).containsExactlyInAnyOrderElementsOf(book.authors());
                s.assertThat(actual.originalLanguage()).isEqualTo(book.originalLanguage());
                s.assertThat(actual.details()).containsExactlyInAnyOrderElementsOf(book.details());
            });
        }

        @Test
        @DisplayName("reference book with multiple authors and one detail successfully when request is valid")
        void referenceBookMultipleAuthorsOneDetailSuccess() throws Exception {
            var book = builder().multipleAuthors().detailInOriginalLanguage().build();
            var authors = List.copyOf(book.authors());
            var detail = book.details().stream().findFirst().orElseThrow();

            when(booksReferencingService.referenceBook(any())).thenReturn(book);

            var requestAuthors = book.authors().stream()
                    .map("\"%s\""::formatted)
                    .collect(joining(", "));
            var requestBody = """
                    {
                        "authors": [%s],
                        "original_language": "%s",
                        "details": [{
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }]
                    }
                    """.formatted(requestAuthors, book.originalLanguage(), detail.language(), detail.title(), detail.description());
            mvc.perform(post("/api/v1/books")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/books/" + book.id())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("id", equalTo(book.id().toString())))
                    .andExpect(jsonPath("authors").isArray())
                    .andExpect(jsonPath("authors", hasSize(2)))
                    .andExpect(jsonPath("authors[0]", equalTo(authors.getFirst().toString())))
                    .andExpect(jsonPath("authors[1]", equalTo(authors.getLast().toString())))
                    .andExpect(jsonPath("details").isArray())
                    .andExpect(jsonPath("details", hasSize(1)))
                    .andExpect(jsonPath("details[0].language", equalTo(detail.language())))
                    .andExpect(jsonPath("details[0].title", equalTo(detail.title())))
                    .andExpect(jsonPath("details[0].description", equalTo(detail.description())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.id())));

            verify(booksReferencingService).referenceBook(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.authors()).containsExactlyInAnyOrderElementsOf(book.authors());
                s.assertThat(actual.originalLanguage()).isEqualTo(book.originalLanguage());
                s.assertThat(actual.details()).containsExactlyInAnyOrderElementsOf(book.details());
            });
        }

        @Test
        @DisplayName("reference book with one author and multiple details successfully when request is valid")
        void referenceBookOneAuthorMultipleDetailsSuccess() throws Exception {
            var book = builder().oneAuthor().detailInOriginalLanguage().plusDetailInOtherLanguage().build();
            var author = book.authors().stream().findFirst().orElseThrow();
            var details = List.copyOf(book.details());

            when(booksReferencingService.referenceBook(any())).thenReturn(book);

            var requestDetails = book.details().stream().map(detail -> """
                        {
                            "language": "%s",
                            "title": "%s",
                            "description": "%s"
                        }""".formatted(detail.language(), detail.title(), detail.description()))
                    .collect(joining(",\n"));
            var requestBody = """
                    {
                        "authors": ["%s"],
                        "original_language": "%s",
                        "details": [%s]
                    }
                    """.formatted(author, book.originalLanguage(), requestDetails);
            mvc.perform(post("/api/v1/books")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/books/" + book.id())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("id", equalTo(book.id().toString())))
                    .andExpect(jsonPath("authors").isArray())
                    .andExpect(jsonPath("authors", hasSize(1)))
                    .andExpect(jsonPath("authors[0]", equalTo(author.toString())))
                    .andExpect(jsonPath("details").isArray())
                    .andExpect(jsonPath("details", hasSize(2)))
                    .andExpect(jsonPath("details[0].language", equalTo(details.getFirst().language())))
                    .andExpect(jsonPath("details[0].title", equalTo(details.getFirst().title())))
                    .andExpect(jsonPath("details[0].description", equalTo(details.getFirst().description())))
                    .andExpect(jsonPath("details[1].language", equalTo(details.getLast().language())))
                    .andExpect(jsonPath("details[1].title", equalTo(details.getLast().title())))
                    .andExpect(jsonPath("details[1].description", equalTo(details.getLast().description())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.id())));

            verify(booksReferencingService).referenceBook(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.authors()).containsExactlyInAnyOrderElementsOf(book.authors());
                s.assertThat(actual.originalLanguage()).isEqualTo(book.originalLanguage());
                s.assertThat(actual.details()).containsExactlyInAnyOrderElementsOf(book.details());
            });
        }

    }

}
