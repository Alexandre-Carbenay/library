## Use Java as the programming language for backend services

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: Backend services

### Context and Problem Statement

We need to choose a programming language to implement the library services, with primary focus on the catalog service,
that will be a backend service.

### Decision Drivers

The library project is a showcase project to demonstrate different good practices about how a modularized system may be
implemented, so the chosen language must:

* help the developer (me) be confident in the implementation, to avoid too much work on my personal time
* be broadly used on the developers market, by colleagues or trainees met during my training sessions
* provide a suite of tools and frameworks to provide both REST APIs and Kafka integration, plus cloud-ready capabilities
* be supported by a large community

### Considered Options

* Java
* Kotlin
* Go

### Decision Outcome

Java will be used as the programming language for the catalog service, and most likely for the future backend services,
because:

* it is my most mastered language
* it provides general frameworks, like Spring, which I know and master
* most good practices that I want to share can be achieved with the language

#### Positive Consequences

* Usage of Java as the programming language will speed up implementation.
* Java is a widely used language and provides many open-source frameworks and libraries.
* Usage of Java implies usage of object-oriented programming paradigm but also support kind of functional programming
  paradigm.
* Java is a general-purpose programming language.
* Other programming languages running on the JVM may be introduced during implementation, sharing the same libraries as
  the ones used in the Java implementation.

#### Negative Consequences

* Java ecosystem is less efficient (i.e. slow build and startup, high memory footprint) than other languages that do not
  depend on a virtual machine (e.g. Go).
* Using Java won't help me master other languages.
