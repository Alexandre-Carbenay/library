package org.adhuc.library.support.rest;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;

import java.util.Set;

/**
 * An {@link AutoConfigurationImportFilter} implementation that disables the Spring Data Rest WebMvc auto-configurations.
 */
public class SpringDataWebAutoConfigurationDisabler implements AutoConfigurationImportFilter {

    private static final Set<String> DISABLED_CONFIGURATIONS = Set.of(
            RepositoryRestMvcAutoConfiguration.class.getCanonicalName(),
            SpringDataWebAutoConfiguration.class.getCanonicalName()
    );

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            matches[i] = !DISABLED_CONFIGURATIONS.contains(autoConfigurationClasses[i]);
        }
        return matches;
    }

}
