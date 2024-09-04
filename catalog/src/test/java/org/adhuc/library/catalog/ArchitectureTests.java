package org.adhuc.library.catalog;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;

@SuppressWarnings("unused")
@AnalyzeClasses(packages = "org.adhuc.library.catalog")
class ArchitectureTests {

    @ArchTest
    ArchRule onion = JMoleculesArchitectureRules.ensureOnionClassical();

}
