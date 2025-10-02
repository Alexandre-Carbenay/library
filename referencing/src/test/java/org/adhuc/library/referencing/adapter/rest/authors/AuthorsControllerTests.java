package org.adhuc.library.referencing.adapter.rest.authors;

import net.datafaker.Faker;
import org.adhuc.library.referencing.adapter.rest.PaginationSerializationConfiguration;
import org.adhuc.library.referencing.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.referencing.authors.Author;
import org.adhuc.library.referencing.authors.AuthorsConsultationService;
import org.adhuc.library.referencing.authors.AuthorsMother.Authors;
import org.adhuc.library.referencing.authors.AuthorsReferencingService;
import org.adhuc.library.referencing.authors.ReferenceAuthor;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.authors.AuthorsMother.authors;
import static org.adhuc.library.referencing.authors.AuthorsMother.builder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"NotNullFieldNotInitialized", "preview", "StringTemplateMigration"})
@Tag("integration")
@Tag("restApi")
@WebMvcTest(controllers = {AuthorsController.class, AuthorModelAssembler.class})
@Import({RequestValidationConfiguration.class, PaginationSerializationConfiguration.class})
@DisplayName("Authors controller should")
class AuthorsControllerTests {

    private static final Faker FAKER = new Faker();

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthorsConsultationService authorsConsultationService;
    @MockitoBean
    private AuthorsReferencingService authorsReferencingService;
    private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.captor();
    private final ArgumentCaptor<ReferenceAuthor> referenceCommandCaptor = ArgumentCaptor.captor();

    @Test
    @DisplayName("provide an empty page when no author has been referenced")
    void listAuthorsEmptyDefaultPage() throws Exception {
        var request = PageRequest.of(0, 50);
        when(authorsConsultationService.getPage(any())).thenReturn(Page.empty(request));

        mvc.perform(get("/api/v1/authors").accept("application/hal+json"))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(50)))
                .andExpect(jsonPath("page.total_elements", equalTo(0)))
                .andExpect(jsonPath("page.total_pages", equalTo(0)))
                .andExpect(jsonPath("page.number", equalTo(0)))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/authors?page=0&size=50")))
                .andExpect(jsonPath("_links.first").doesNotExist())
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.last").doesNotExist())
                .andExpect(jsonPath("_embedded").doesNotExist());

        verify(authorsConsultationService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    @Test
    @DisplayName("provide a unique page when the number of authors is lower than or equal to the requested page size")
    void uniqueDefaultPage() throws Exception {
        var numberOfElements = FAKER.random().nextInt(1, 50);
        var authors = authors(numberOfElements);

        var request = PageRequest.of(0, 50);
        when(authorsConsultationService.getPage(any())).thenReturn(new PageImpl<>(authors, request, numberOfElements));

        var result = mvc.perform(get("/api/v1/authors").accept("application/hal+json"))
                .andDo(print())
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(50)))
                .andExpect(jsonPath("page.total_elements", equalTo(numberOfElements)))
                .andExpect(jsonPath("page.total_pages", equalTo(1)))
                .andExpect(jsonPath("page.number", equalTo(0)))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/authors?page=0&size=50")))
                .andExpect(jsonPath("_links.first.href").doesNotExist())
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.last.href").doesNotExist())
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllAuthors(result, authors);

        verify(authorsConsultationService).getPage(pageableCaptor.capture());
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
        var authors = pageSample(pageIndex, 50);

        when(authorsConsultationService.getPage(any())).thenReturn(authors);

        var result = mvc.perform(get("/api/v1/authors")
                        .accept("application/hal+json")
                        .queryParam("page", String.valueOf(authors.getNumber())))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(authors.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(authors.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(authors.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(authors.getNumber())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/authors?page=\{authors.getNumber()}&size=50")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllAuthors(result, authors.toList());

        verify(authorsConsultationService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(authors.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"-50", "-2", "-1"})
    @DisplayName("refuse providing a page with a negative page number")
    void getInvalidPageNumber(String pageNumber) throws Exception {
        mvc.perform(get("/api/v1/authors").accept("application/hal+json")
                        .queryParam("page", pageNumber)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(STR."Numeric instance is lower than the required minimum (minimum: 0, found: \{pageNumber})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("page")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-50", "-1", "0"})
    @DisplayName("refuse providing a page with a negative page size")
    void getInvalidPageSize(String pageSize) throws Exception {
        mvc.perform(get("/api/v1/authors").accept("application/hal+json")
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
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 1, found: \{pageSize})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("size")));
    }

    @ParameterizedTest
    @CsvSource({"-50,-1", "-2,-50", "-1,0"})
    @DisplayName("refuse providing a page with a negative page number")
    void getInvalidPageNumberAndSize(String pageNumber, String pageSize) throws Exception {
        mvc.perform(get("/api/v1/authors").accept("application/hal+json")
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
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 0, found: \{pageNumber})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("page")))
                .andExpect(jsonPath("errors[1].detail",
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 1, found: \{pageSize})")))
                .andExpect(jsonPath("errors[1].parameter", equalTo("size")));
    }

    @Test
    @DisplayName("provide a page corresponding to requested page and size")
    void getPage() throws Exception {
        var pageIndex = FAKER.random().nextInt(0, 100);
        var pageSize = FAKER.random().nextInt(2, 100);
        var authors = pageSample(pageIndex, pageSize);

        when(authorsConsultationService.getPage(any())).thenReturn(authors);

        var result = mvc.perform(get("/api/v1/authors")
                        .accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageIndex))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(authors.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(authors.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(authors.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/authors?page=\{pageIndex}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllAuthors(result, authors.toList());

        verify(authorsConsultationService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(authors.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(authors.getSize());
        });
    }

    @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
    @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
    @DisplayName("provide a page with navigation links")
    void getPageWithNavigation(int pageIndex, int pageSize, Page<Author> authors, boolean hasFirst, boolean hasPrev,
                               boolean hasNext, boolean hasLast) throws Exception {
        when(authorsConsultationService.getPage(any())).thenReturn(authors);

        var result = mvc.perform(get("/api/v1/authors")
                        .accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageIndex))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/authors?page=\{pageIndex}&size=\{pageSize}")));

        verifyNavigationLink(result, hasFirst, "first", STR."http://localhost/api/v1/authors?page=0&size=\{pageSize}");
        verifyNavigationLink(result, hasPrev, "prev", STR."http://localhost/api/v1/authors?page=\{pageIndex - 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasNext, "next", STR."http://localhost/api/v1/authors?page=\{pageIndex + 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasLast, "last", STR."http://localhost/api/v1/authors?page=\{authors.getTotalPages() - 1}&size=\{pageSize}");
    }

    static void verifyNavigationLink(ResultActions result, boolean hasLink, String linkName, @Nullable String valueIfExists) throws Exception {
        if (hasLink) {
            result.andExpect(jsonPath(STR."_links.\{linkName}.href", equalTo(requireNonNull(valueIfExists))));
        } else {
            result.andExpect(jsonPath(STR."_links.\{linkName}").doesNotExist());
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

    private static Page<Author> pageSample(int pageIndex, int requestedPageSize) {
        var isLastPage = FAKER.bool().bool();
        if (isLastPage) {
            return lastPage(pageIndex, requestedPageSize);
        }
        return fullPage(pageIndex, requestedPageSize);
    }

    private static PageImpl<Author> lastPage(int page, int requestedPageSize) {
        int pageSize = FAKER.random().nextInt(1, requestedPageSize);
        int totalRows = pageSize;
        if (page > 0) {
            totalRows += requestedPageSize * page;
        }
        var elements = authors(pageSize);
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private static PageImpl<Author> fullPage(int page, int requestedPageSize) {
        var totalPages = FAKER.random().nextInt(page + 1, 150);
        var lastPageSize = FAKER.random().nextInt(1, requestedPageSize - 1);
        var totalRows = totalPages * requestedPageSize + lastPageSize;
        var elements = authors(requestedPageSize);
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    public static void assertResponseContainsAllAuthors(ResultActions result, Collection<Author> expectedAuthors) throws Exception {
        result.andExpect(jsonPath("_embedded.authors").exists())
                .andExpect(jsonPath("_embedded.authors").isArray())
                .andExpect(jsonPath("_embedded.authors", hasSize(expectedAuthors.size())));
        for (var author : expectedAuthors) {
            if (author.dateOfDeath().isPresent()) {
                result.andExpect(jsonPath(
                        "_embedded.authors.[" +
                                "?(@.id == \"" + author.id() + "\" " +
                                "&& @.name == \"" + author.name() + "\" " +
                                "&& @.date_of_birth == \"" + author.dateOfBirth() + "\" " +
                                "&& @.date_of_death == \"" + author.dateOfDeath().get() + "\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/authors/" + author.id() + "\")]").exists());
            } else {
                // TODO ensure date of death is not present
                result.andExpect(jsonPath(
                        "_embedded.authors.[" +
                                "?(@.id == \"" + author.id() + "\" " +
                                "&& @.name == \"" + author.name() + "\" " +
                                "&& @.date_of_birth == \"" + author.dateOfBirth() + "\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/authors/" + author.id() + "\")]").exists());
            }
        }
    }

    @Test
    @DisplayName("refuse referencing author when name is missing")
    void referenceAuthorMissingName() throws Exception {
        var dateOfBirth = Authors.dateOfBirth();
        mvc.perform(post("/api/v1/authors")
                        .contentType("application/json")
                        .content(STR."{\"date_of_birth\":\"\{dateOfBirth}\"}")
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo("Missing required property")))
                .andExpect(jsonPath("errors[0].pointer", equalTo("name")));
    }

    @Test
    @DisplayName("refuse referencing author when date of birth is missing")
    void referenceAuthorMissingDateOfBirth() throws Exception {
        var name = Authors.name();
        mvc.perform(post("/api/v1/authors")
                        .contentType("application/json")
                        .content(STR."{\"name\":\"\{name}\"}")
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo("Missing required property")))
                .andExpect(jsonPath("errors[0].pointer", equalTo("date_of_birth")));
    }

    @Test
    @DisplayName("reference alive author successfully when request is valid")
    void referenceAliveAuthorSuccess() throws Exception {
        var author = builder().alive().build();

        when(authorsReferencingService.referenceAuthor(any())).thenReturn(author);

        mvc.perform(post("/api/v1/authors")
                        .contentType("application/json")
                        .content(STR."{\"name\":\"\{author.name()}\", \"date_of_birth\":\"\{author.dateOfBirth()}\"}")
                ).andExpect(status().isCreated())
                .andExpect(header().string("Location", equalTo(STR."http://localhost/api/v1/authors/\{author.id()}")))
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("id", equalTo(author.id().toString())))
                .andExpect(jsonPath("name", equalTo(author.name())))
                .andExpect(jsonPath("date_of_birth", equalTo(author.dateOfBirth().toString())))
                .andExpect(jsonPath("date_of_death").doesNotExist())
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/authors/\{author.id()}")));

        verify(authorsReferencingService).referenceAuthor(referenceCommandCaptor.capture());
        var actual = referenceCommandCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.name()).isEqualTo(author.name());
            s.assertThat(actual.dateOfBirth()).isEqualTo(author.dateOfBirth());
        });
    }

    @Test
    @DisplayName("reference dead author successfully when request is valid")
    void referenceDeadAuthorSuccess() throws Exception {
        var author = builder().dead().build();

        when(authorsReferencingService.referenceAuthor(any())).thenReturn(author);

        mvc.perform(post("/api/v1/authors")
                        .contentType("application/json")
                        .content(STR."{\"name\":\"\{author.name()}\", \"date_of_birth\":\"\{author.dateOfBirth()}\", \"date_of_death\":\"\{author.dateOfDeath().orElseThrow()}\"}")
                ).andExpect(status().isCreated())
                .andExpect(header().string("Location", equalTo(STR."http://localhost/api/v1/authors/\{author.id()}")))
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("id", equalTo(author.id().toString())))
                .andExpect(jsonPath("name", equalTo(author.name())))
                .andExpect(jsonPath("date_of_birth", equalTo(author.dateOfBirth().toString())))
                .andExpect(jsonPath("date_of_death", equalTo(author.dateOfDeath().orElseThrow().toString())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/authors/\{author.id()}")));

        verify(authorsReferencingService).referenceAuthor(referenceCommandCaptor.capture());
        var actual = referenceCommandCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.name()).isEqualTo(author.name());
            s.assertThat(actual.dateOfBirth()).isEqualTo(author.dateOfBirth());
            s.assertThat(actual.dateOfDeath()).isEqualTo(author.dateOfDeath());
        });
    }

}
