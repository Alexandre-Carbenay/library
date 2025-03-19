@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"books", "authors"}
)
// By default, all classes are domain model
@org.jmolecules.architecture.onion.classical.DomainModelRing
package org.adhuc.library.catalog.editions;
