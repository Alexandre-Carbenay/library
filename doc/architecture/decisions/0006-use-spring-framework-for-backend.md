## Use Spring framework for backend services

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: Backend services

### Context and Problem Statement

As [Java](0003-use-java-as-language.md) is the chosen programming language for the module, we want to choose a framework
to ease the application development with the least burden.

### Decision Drivers

The chosen web-development framework must:

* provide dependency injection mechanisms
* be able to expose and consume REST APIs
* handle OAuth2 / OpenID protocols
* provide capabilities for event-driven architecture
* integrate with most persistent framework
* help the developer (me) be confident in the implementation, to avoid too much work on my personal time

### Considered Options

* [Spring-Boot](https://spring.io/projects/spring-boot)
* [Quarkus](https://quarkus.io/)

### Decision Outcome

Spring-Boot will be used by the catalog service and will be the default choice for other backend services.

### Pros and Cons of the Options

#### Spring-Boot

* Usage of Spring-Boot as the programming language will speed up implementation.
* Spring-Boot is a well established framework for Java. It is non-invasive and provides multiple features such as IoC,
  AOP, REST services implementation, security and integration for many event-driven tools that will help speed up
  implementation in a cohesive way.
* Spring-Boot also provides support to run with the GraalVM, although this is not the default.

#### Quarkus

* Quarkus offers built-in GraalVM integration, providing fast startup and low memory footprint.
* Quarkus is relatively new and its ecosystem is less cohesive compared to Spring-Boot.
* The development team (me) has very little knowledge of the framework.
