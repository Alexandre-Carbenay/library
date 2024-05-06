package org.adhuc.library.catalog.adapter.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Tag("restApi")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "management.endpoints.web.base-path=/management-custom"
})
@DisplayName("Open API validation should")
class OpenApiValidationTests {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {
            "/health",
            "/info",
            "/beans"
    })
    @DisplayName("ignore management paths")
    void managementPaths(String path) throws Exception {
        mvc.perform(get("/management-custom" + path))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("not allow accessing path not specified in openapi endpoints")
    void unknownPath() throws Exception {
        mvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/json",
            "application/hal+json"
    })
    @DisplayName("allow accessing path specified in openapi endpoints")
    void knownPath(String acceptHeader) throws Exception {
        mvc.perform(get("/api/v1/catalog").header("Accept", acceptHeader))
                .andExpect(status().is2xxSuccessful());
    }

}
