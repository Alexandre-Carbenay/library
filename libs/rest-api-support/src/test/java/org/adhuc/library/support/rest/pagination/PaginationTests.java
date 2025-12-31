package org.adhuc.library.support.rest.pagination;

import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Tag("restApi")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "org.adhuc.library.support.rest.validation.enabled=false"
})
@DisplayName("Pagination should")
class PaginationTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private BeerService beerService;
    private final Faker faker = new Faker();

    @Test
    @DisplayName("ensure that paginated response has a dedicated property containing information about current page")
    void responseWithPage() throws Exception {
        var metadata = generateMetadata();
        var beers = generateBeers(metadata);

        when(beerService.getBeers()).thenReturn(PagedModel.of(beers, metadata));

        mvc.perform(get("/test/pagination").accept("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("page.size", equalTo(Long.valueOf(metadata.getSize()).intValue())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(metadata.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(Long.valueOf(metadata.getTotalPages()).intValue())))
                .andExpect(jsonPath("page.number", equalTo(Long.valueOf(metadata.getNumber()).intValue())))
                .andExpect(jsonPath("_embedded.beers").isArray());
    }

    @SpringBootApplication
    static class PaginationTestApplication {
        static void main(String[] args) {
            SpringApplication.run(PaginationTestApplication.class, args);
        }

        @RestController
        static class ValidationTestController {
            private final BeerService beerService;

            ValidationTestController(BeerService beerService) {
                this.beerService = beerService;
            }

            @GetMapping("/test/pagination")
            ResponseEntity<Object> testPagination() {
                var page = beerService.getBeers();
                var response = HalModelBuilder.halModelOf(Objects.requireNonNull(page.getMetadata()))
                        .embed(page.getContent(), LinkRelation.of("beers"))
                        .build();
                return ResponseEntity.ok(response);
            }
        }
    }

    private PagedModel.PageMetadata generateMetadata() {
        var size = faker.number().numberBetween(1, 10);
        var number = faker.number().numberBetween(0, 100);
        var totalPages = faker.number().numberBetween(number, 100);
        var totalElements = faker.number().numberBetween(totalPages * size, totalPages * (size + 1) - 1);
        return new PagedModel.PageMetadata(size, number, totalElements, totalPages);
    }

    private List<Beer> generateBeers(PagedModel.PageMetadata metadata) {
        return LongStream.range(0, metadata.getSize())
                .mapToObj(_ -> new Beer(faker.beer().name(), faker.beer().brand(), faker.beer().style()))
                .toList();
    }

    interface BeerService {
        PagedModel<Beer> getBeers();
    }

    record Beer(String name, String brand, String style) {
    }

}
