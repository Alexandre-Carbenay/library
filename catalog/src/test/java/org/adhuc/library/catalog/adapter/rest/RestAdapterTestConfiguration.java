package org.adhuc.library.catalog.adapter.rest;

import org.adhuc.library.support.rest.validation.RequestValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@ImportAutoConfiguration(RequestValidationAutoConfiguration.class)
@Import(PaginationSerializationConfiguration.class)
public class RestAdapterTestConfiguration {
}
