## Organize the library project as a mono repository

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: General

### Context and Problem Statement

The library system will be composed of [multiple services](0001-bounded-contexts-microservices.md), each service being
autonomous in terms of development and delivery lifecycle.

### Considered Options

* dedicated Git project for each service
* single Git project grouping all services

### Decision Outcome

The library Git project will be organized as a monorepo, meaning that all services will be integrated in the same
codebase.

Using a monorepo will help determining the scope of the system, reducing the number of different projects to maintain,
and simplifying the sharing of knowledge between the different services (e.g. shared code, documentation, acceptance
scenarii, ...).

Tooling around the services will have to support monorepo organization (e.g. continuous delivery pipelines
configuration, general documentation, ...).
