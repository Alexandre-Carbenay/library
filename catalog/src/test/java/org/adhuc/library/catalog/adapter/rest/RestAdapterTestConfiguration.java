package org.adhuc.library.catalog.adapter.rest;

import org.adhuc.library.support.rest.pagination.PaginationAutoConfiguration;
import org.adhuc.library.support.rest.validation.RequestValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@ImportAutoConfiguration({RequestValidationAutoConfiguration.class, PaginationAutoConfiguration.class})
public class RestAdapterTestConfiguration {
}
