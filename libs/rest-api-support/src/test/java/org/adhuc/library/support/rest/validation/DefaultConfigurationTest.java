package org.adhuc.library.support.rest.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Configuration by default should")
class DefaultConfigurationTest {

    @Autowired
    private RequestValidationProperties properties;

    @Test
    @DisplayName("contain resource bean to access the OpenAPI specification file")
    void resourceBean() {
        assertThat(properties.openApi()).isNotNull();
    }

}
