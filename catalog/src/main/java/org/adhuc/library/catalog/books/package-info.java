@NullMarked
@ApplicationModule(
        allowedDependencies = "authors"
)
// By default, all classes are domain model
@DomainModelRing
package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.DomainModelRing;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;