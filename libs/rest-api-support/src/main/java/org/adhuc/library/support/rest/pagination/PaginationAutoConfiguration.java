package org.adhuc.library.support.rest.pagination;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(PaginationSerializationConfiguration.class)
public class PaginationAutoConfiguration {
}
