package org.adhuc.library.referencing;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@SuppressWarnings("unused")
@AnalyzeClasses(packages = "org.adhuc.library.referencing", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    private static final String AUTHORS_PACKAGE = "org.adhuc.library.referencing.authors";
    private static final String BOOKS_PACKAGE = "org.adhuc.library.referencing.books";
    private static final String EDITIONS_PACKAGE = "org.adhuc.library.referencing.editions";
    private static final String PUBLISHERS_PACKAGE = "org.adhuc.library.referencing.publishers";

    @ArchTest
    ArchRule onion = JMoleculesArchitectureRules.ensureOnionClassical();

    @ArchTest
    ArchRule noCycles = slices().matching("org.adhuc.library.referencing.(*)..").should().beFreeOfCycles();

    @ArchTest
    ArchRule authors = noClasses().that().resideInAPackage(AUTHORS_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(
                    BOOKS_PACKAGE,
                    EDITIONS_PACKAGE,
                    PUBLISHERS_PACKAGE
            )
            .as("Authors should not depend on other domain concepts")
            .because("Authors are independent of other domain concepts");

    @ArchTest
    ArchRule books = noClasses().that().resideInAPackage(BOOKS_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(
                    EDITIONS_PACKAGE,
                    PUBLISHERS_PACKAGE
            )
            .as("Books should only depend on authors concepts")
            .because("Books depend only on authors");

    @ArchTest
    ArchRule editions = noClasses().that().resideInAPackage(EDITIONS_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(
                    AUTHORS_PACKAGE
            )
            .as("Editions should only depend on books concepts")
            .because("Editions depend only on books");

    @ArchTest
    ArchRule publishers = noClasses().that().resideInAPackage(PUBLISHERS_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(
                    AUTHORS_PACKAGE,
                    BOOKS_PACKAGE,
                    EDITIONS_PACKAGE
            )
            .as("Publishers should not depend on other domain concepts")
            .because("Publishers are independent of other domain concepts");

}
