## Deploy each bounded context as a microservice

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: General

### Context and Problem Statement

The library system will be composed of multiple [bounded contexts](https://martinfowler.com/bliki/BoundedContext.html),
each bounded context having its own business scope and the associated model.

We need to determine if the bounded contexts will be deployed as a single modular monolith or be split into different
autonomously deployed services, aka [microservices](https://martinfowler.com/articles/microservices.html).

### Considered Options

* modular monolith
* microservices

### Decision Outcome

The library system will be split into different microservices, each service corresponding to a bounded context,
associated to its own subdomain.

Using microservices instead of a modular monolith will allow us to demonstrate more easily, among others, how to:

* integrate different services, either through synchronous or asynchronous communications
* validate contracts for those communications
* build resilient distributed system
* handle distributed traces to understand what happens in the whole system
