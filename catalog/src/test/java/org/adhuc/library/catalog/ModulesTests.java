package org.adhuc.library.catalog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

@Tag("integration")
class ModulesTests {

    private static final String ADAPTERS = "..library.catalog.adapter..";

    @Test
    void writeDocumentationSnippets() {
        var modules = ApplicationModules.of(Application.class, resideInAPackage(ADAPTERS)).verify();

        modules.forEach(System.out::println);
        new Documenter(modules).writeDocumentation();
    }

}
