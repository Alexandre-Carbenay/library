package org.adhuc.library.website.catalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.adhuc.library.website.catalog.BooksMother.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CatalogController.class)
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private CatalogClient catalogClient;

    @Test
    @DisplayName("provide the catalog page")
    void catalogPage() throws Exception {
        var content = List.of(
                DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS, BULLSHIT_JOBS
        );
        when(catalogClient.listBooks()).thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 4));

        mvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/root"))
                .andExpect(model().attribute("books", content));
    }

}
