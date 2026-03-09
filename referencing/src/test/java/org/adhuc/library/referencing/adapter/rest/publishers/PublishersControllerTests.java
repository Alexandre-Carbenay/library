package org.adhuc.library.referencing.adapter.rest.publishers;

import net.datafaker.Faker;
import org.adhuc.library.referencing.publishers.*;
import org.adhuc.library.referencing.publishers.PublishersMother.Publishers;
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
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.publishers.PublishersMother.builder;
import static org.adhuc.library.referencing.publishers.PublishersMother.publishers;
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
@WebMvcTest(controllers = {PublishersController.class, PublisherModelAssembler.class})
@ImportAutoConfiguration({RequestValidationAutoConfiguration.class, PaginationAutoConfiguration.class, ValidationAutoConfiguration.class})
@DisplayName("Publishers controller should")
class PublishersControllerTests {

    private static final Faker FAKER = new Faker();

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PublishersConsultationService publishersConsultationService;
    @MockitoBean
    private PublishersReferencingService publishersReferencingService;

    @Nested
    @DisplayName("getting publishers page")
    class PublishersPage {

        private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.captor();

        @Test
        @DisplayName("provide an empty page when no publisher has been referenced")
        void listPublishersEmptyDefaultPage() throws Exception {
            var request = PageRequest.of(0, 50);
            when(publishersConsultationService.getPage(any())).thenReturn(Page.empty(request));

            mvc.perform(get("/api/v1/publishers").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(0)))
                    .andExpect(jsonPath("page.total_pages", equalTo(0)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers?page=0&size=50")))
                    .andExpect(jsonPath("_links.first").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last").doesNotExist())
                    .andExpect(jsonPath("_embedded").doesNotExist());

            verify(publishersConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isZero();
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @Test
        @DisplayName("provide a unique page when the number of publishers is lower than or equal to the requested page size")
        void uniqueDefaultPage() throws Exception {
            var numberOfElements = FAKER.random().nextInt(1, 50);
            var publishers = publishers(numberOfElements);

            var request = PageRequest.of(0, 50);
            when(publishersConsultationService.getPage(any())).thenReturn(new PageImpl<>(publishers, request, numberOfElements));

            var result = mvc.perform(get("/api/v1/publishers").accept("application/hal+json"))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(50)))
                    .andExpect(jsonPath("page.total_elements", equalTo(numberOfElements)))
                    .andExpect(jsonPath("page.total_pages", equalTo(1)))
                    .andExpect(jsonPath("page.number", equalTo(0)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers?page=0&size=50")))
                    .andExpect(jsonPath("_links.first.href").doesNotExist())
                    .andExpect(jsonPath("_links.prev").doesNotExist())
                    .andExpect(jsonPath("_links.next").doesNotExist())
                    .andExpect(jsonPath("_links.last.href").doesNotExist())
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllPublishers(result, publishers);

            verify(publishersConsultationService).getPage(pageableCaptor.capture());
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
            var publishers = pageSample(pageIndex, 50);

            when(publishersConsultationService.getPage(any())).thenReturn(publishers);

            var result = mvc.perform(get("/api/v1/publishers")
                            .accept("application/hal+json")
                            .queryParam("page", String.valueOf(publishers.getNumber())))
                    .andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(publishers.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(publishers.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(publishers.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(publishers.getNumber())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers?page=" + publishers.getNumber() + "&size=50")))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllPublishers(result, publishers.toList());

            verify(publishersConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(publishers.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(50);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"-50", "-2", "-1"})
        @DisplayName("refuse providing a page with a negative page number")
        void getInvalidPageNumber(String pageNumber) throws Exception {
            mvc.perform(get("/api/v1/publishers").accept("application/hal+json")
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
            mvc.perform(get("/api/v1/publishers").accept("application/hal+json")
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
            mvc.perform(get("/api/v1/publishers").accept("application/hal+json")
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
            var publishers = pageSample(pageIndex, pageSize);

            when(publishersConsultationService.getPage(any())).thenReturn(publishers);

            var result = mvc.perform(get("/api/v1/publishers")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("page.size", equalTo(publishers.getSize())))
                    .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(publishers.getTotalElements()).intValue())))
                    .andExpect(jsonPath("page.total_pages", equalTo(publishers.getTotalPages())))
                    .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers?page=" + pageIndex + "&size=" + pageSize)))
                    .andExpect(jsonPath("_embedded").exists());

            assertResponseContainsAllPublishers(result, publishers.toList());

            verify(publishersConsultationService).getPage(pageableCaptor.capture());
            var actual = pageableCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getPageNumber()).isEqualTo(publishers.getNumber());
                s.assertThat(actual.getPageSize()).isEqualTo(publishers.getSize());
            });
        }

        @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
        @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
        @DisplayName("provide a page with navigation links")
        void getPageWithNavigation(int pageIndex, int pageSize, Page<Publisher> publishers, boolean hasFirst, boolean hasPrev,
                                   boolean hasNext, boolean hasLast) throws Exception {
            when(publishersConsultationService.getPage(any())).thenReturn(publishers);

            var result = mvc.perform(get("/api/v1/publishers")
                            .accept("application/hal+json")
                            .queryParam("page", Integer.toString(pageIndex))
                            .queryParam("size", Integer.toString(pageSize))
                    ).andExpect(status().isPartialContent())
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers?page=" + pageIndex + "&size=" + pageSize)));

            verifyNavigationLink(result, hasFirst, "first", "http://localhost/api/v1/publishers?page=0&size=" + pageSize);
            verifyNavigationLink(result, hasPrev, "prev", "http://localhost/api/v1/publishers?page=" + (pageIndex - 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasNext, "next", "http://localhost/api/v1/publishers?page=" + (pageIndex + 1) + "&size=" + pageSize);
            verifyNavigationLink(result, hasLast, "last", "http://localhost/api/v1/publishers?page=" + (publishers.getTotalPages() - 1) + "&size=" + pageSize);
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

        private static Page<Publisher> pageSample(int pageIndex, int requestedPageSize) {
            var isLastPage = FAKER.bool().bool();
            if (isLastPage) {
                return lastPage(pageIndex, requestedPageSize);
            }
            return fullPage(pageIndex, requestedPageSize);
        }

        private static PageImpl<Publisher> lastPage(int page, int requestedPageSize) {
            int pageSize = FAKER.random().nextInt(1, requestedPageSize);
            int totalRows = pageSize;
            if (page > 0) {
                totalRows += requestedPageSize * page;
            }
            var elements = publishers(pageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        private static PageImpl<Publisher> fullPage(int page, int requestedPageSize) {
            var totalPages = FAKER.random().nextInt(page + 1, 150);
            var lastPageSize = FAKER.random().nextInt(1, requestedPageSize - 1);
            var totalRows = totalPages * requestedPageSize + lastPageSize;
            var elements = publishers(requestedPageSize);
            return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
        }

        public static void assertResponseContainsAllPublishers(ResultActions result, Collection<Publisher> expectedPublishers) throws Exception {
            result.andExpect(jsonPath("_embedded.publishers").exists())
                    .andExpect(jsonPath("_embedded.publishers").isArray())
                    .andExpect(jsonPath("_embedded.publishers", hasSize(expectedPublishers.size())));
            for (var publisher : expectedPublishers) {
                result.andExpect(jsonPath(
                        "_embedded.publishers.[" +
                                "?(@.id == \"%s\" " +
                                "&& @.name == \"%s\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/publishers/%s\")]",
                        publisher.id(), publisher.name(), publisher.id()).exists());
            }
        }

    }

    @Nested
    @DisplayName("referencing new publisher")
    class PublisherReferencing {

        private final ArgumentCaptor<ReferencePublisher> referenceCommandCaptor = ArgumentCaptor.captor();

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("refuse referencing publisher when request is invalid")
        void referencePublisherInvalidRequest(String scenario, String requestBody, String expectedDetail, String expectedPointer) throws Exception {
            mvc.perform(post("/api/v1/publishers")
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
                    Arguments.of("name is missing", missingName(), "Missing required property", "/name"),
                    Arguments.of("name is blank", emptyName(), "\" \" must not be blank", "/name")
            );
        }

        private static String missingName() {
            return "{}";
        }

        private static String emptyName() {
            return """
                    {
                        "name": " "
                    }
                    """;
        }

        @Test
        @DisplayName("reference duplicated publisher")
        void referenceDuplicatedPublisher() throws Exception {
            var name = Publishers.name();
            var requestBody = """
                    {
                        "name": "%s"
                    }
                    """.formatted(name);

            when(publishersReferencingService.referencePublisher(any())).thenThrow(new DuplicatePublisherException(name));

            mvc.perform(post("/api/v1/publishers")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("type", equalTo("/problems/duplicate-publisher")))
                    .andExpect(jsonPath("status", equalTo(409)))
                    .andExpect(jsonPath("title", equalTo("Duplicate publisher")))
                    .andExpect(jsonPath("detail", equalTo("Publisher with name already exists")))
                    .andExpect(jsonPath("errors").isArray())
                    .andExpect(jsonPath("errors", hasSize(1)))
                    .andExpect(jsonPath("errors[0].detail", equalTo("Publisher '%s' already exists".formatted(name))))
                    .andExpect(jsonPath("errors[0].pointer", equalTo("/name")));
        }

        @Test
        @DisplayName("reference publisher successfully when request is valid")
        void referencePublisherSuccess() throws Exception {
            var publisher = builder().build();

            when(publishersReferencingService.referencePublisher(any())).thenReturn(publisher);

            var requestBody = """
                    {
                        "name": "%s"
                    }
                    """.formatted(publisher.name());
            mvc.perform(post("/api/v1/publishers")
                            .contentType("application/json")
                            .content(requestBody)
                    ).andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("http://localhost/api/v1/publishers/" + publisher.id())))
                    .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                    .andExpect(jsonPath("id", equalTo(publisher.id().toString())))
                    .andExpect(jsonPath("name", equalTo(publisher.name())))
                    .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/publishers/" + publisher.id())));

            verify(publishersReferencingService).referencePublisher(referenceCommandCaptor.capture());
            var actual = referenceCommandCaptor.getValue();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.name()).isEqualTo(publisher.name());
            });
        }

    }

}
