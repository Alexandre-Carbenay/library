## Manage build with Gradle for backend services

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: Backend services

### Context and Problem Statement

As we chose [Java](0003-use-java-as-language.md) as the programming language, we need to choose a tool that provides
build lifecycle and dependencies management capabilities.

### Decision Drivers

The chosen build tool must:

* be broadly used on the developers market
* be fast and extensible
* be supported by a large community

### Considered Options

* [Maven](https://maven.apache.org/)
* [Gradle](https://gradle.org/)

### Decision Outcome

Gradle will be used to manage project dependencies and build tasks for services developed in Java.

#### Positive Consequences

* Gradle supports Maven dependencies.
* Gradle builds are fast.
* Extra testing build lifecycles, such as acceptance testing or performance testing, will take advantage of the Gradle
  extensible model.
* The library showcase project may contribute to provide production-ready examples for developers that would otherwise
  continue using Maven because they do not feel confident with Gradle.

#### Negative Consequences

* Gradle is not as widely adopted as Maven, so examples may not suit every developer needs.

### Pros and Cons of the Options

https://gradle.org/maven-vs-gradle/
