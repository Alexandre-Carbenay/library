## Use shared libs for reusable components

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2025-12-12
* Scope: General

### Context and Problem Statement

Some applications in the library system have similar features in terms of non-functional requirements, such as API
validation or pagination. We need to determine how we want to implement those similar features through all the
requesting applications.

### Decision Drivers

* We want to benefit from reusability and configurability when possible.
* We want to avoid duplicating the code, to improve maintainability.
* We want to reduce coupling between applications in case of breaking change in a library.

### Considered Options

* create shared libraries
* create shared projects

### Decision Outcome

Common features will be shared between applications through shared libraries.

#### Consequences

This implies building libraries each time a change is made, then publishing the library in a central registry, with a
version number, following semantic versioning.

Each library will have its own version, to avoid building and releasing all libraries every time only one of them
change. Thus, we need to define a dedicated release workflow for the libraries, then declare a workflow for each shared
library.

### Pros and Cons of the Options

#### Shared libraries

This option proposes to:

* create libraries in a dedicated directory in the mono-repository.
* build libraries independently.
* release libraries independently.
* import libraries in applications based on semantic versioning.

* Good, because the libraries can evolve separately from the applications.
* Good, because the build time is separated between libraries and applications.
* Bad, because it requires to store libraries in a dedicated registry.
* Bad, because it complexifies the development workflow: first develop a feature in a library, then import it in
  applications.

#### Shared projects

This option proposes to:

* create shareable code in a dedicated directory in the mono-repository.
* add a step during the build process of applications to compile the shared code, each time.
* rebuild dependent applications each time a piece of shared code is changed.

* Good, because shared code can be directly compiled and used in applications.
* Bad, because shared code must compile for every application using it at any time.
* Bad, because it increases the build time of the applications.

