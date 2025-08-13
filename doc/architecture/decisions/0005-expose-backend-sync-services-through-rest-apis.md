## Expose backend synchronous services through REST APIs

* Status: ACCEPTED
* Deciders:
  * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-04-23
* Scope: Backend services

### Context and Problem Statement

The library services will need to expose APIs to let other services or front-end services interact with them in a
synchronous way.

We also want to demonstrate how an API can be well documented and self-discoverable with appropriate tooling.

This decision will be only relevant for system or process APIs, as defined in the
[API-led connectivity](https://www.gravitee.io/blog/api-led-connectivity-detailed-overview) approach. Experience APIs
may use more appropriate implementations depending on context.

### Decision Drivers

The chosen API structure must:

* be easy to consume by:
  * web frontend applications (either using server-side rendering or client-side rendering)
  * mobile applications
  * backend-for-frontend services
  * other backend services
* be self-discoverable, or at least provide ways to avoid state management on the client side
* provide authentication / authorization through OAuth2 / OpenID Connect protocols
* provide standards for the documentation, such as OpenAPI specification

### Considered Options

* [REST](https://en.wikipedia.org/wiki/REST) API
* [GraphQL](https://en.wikipedia.org/wiki/GraphQL)
* [gRPC](https://en.wikipedia.org/wiki/GRPC)

### Decision Outcome

The library backend services will expose data and services in a synchronous way through a REST API.

The [Hypertext Application Language](https://stateless.group/hal_specification.html) _(HAL)_ specification will be used
to structure the resources exposed by the different services, unless a more appropriate specification help reaching the
goals of a specific service.

#### Consequences

REST is an architectural style that expose resources as an API to consumers. This architectural style involves many
constraints:

* adopting a **client-server architecture** that promotes separation of concerns. It improves scalability by simplifying
  the server components, reusability of the server features by multiple clients, and allows clients and servers to
  evolve independently.

* allowing **layered system** between the client who requests a representation of a resource’s state, and the server who
  sends the response back. Layered system can provide a number of features (e.g. security, caching, load-balancing).
  Those layers should not affect the request or the response. The client is agnostic as to how many layers, if any,
  there are between the client and the actual server responding to the request.

* enforcing **stateless** communication, so that each request from client to server must contain all the information
  necessary to understand the request, and cannot take advantage of any stored context on the server. Session state
  (e.g. authenticated user) is therefore kept entirely by the client.

* enabling **cacheable** content, meaning that the data within a response to a request be implicitly or explicitly
  labeled as cacheable or non-cacheable. If a response is cacheable, then a client cache is given the right to reuse
  that response data for later, equivalent requests.

* exposing a **uniform interface**, through:
    * identification of resources
    * manipulation of resources through representations
    * self-descriptive messages
    * hypermedia as the engine of application state

The backend services will embrace all those constraints. It means that:

* the APIs will be based on the HTTP protocol as an application layer, using the protocol semantics.
* REST adapters will expose API resources through representations that will be decorrelated from domain objects.
* each resource will contain at least its own resource identifier in the form of a link included in the representation.
* every service in the application will be stateless, state being stored through the repositories in the underlying
  persistence technology.
