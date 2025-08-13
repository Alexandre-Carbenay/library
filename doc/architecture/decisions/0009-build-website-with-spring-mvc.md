## Build website with Spring MVC

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-11-26
* Scope: Website

### Context and Problem Statement

We need to expose a website for library members to browse the catalog and eventually borrow a book. We must define which
technology to use to implement the website.

### Decision Drivers

The website will be kept simple, with basic features, and we must avoid spending much time on this part of the system,
as it is not the primary purpose of the project to provide a beautiful, well-designed website. We will rather focus on
having a technology that:

* does not evolve too quickly, so we don't spend too much time following the evolutions
* is simple to implement for a more backend-oriented developer
* provide capabilities to render HTML and execute asynchronous HTTP calls to backend services
* can easily implement [consumer-driven contract testing](https://martinfowler.com/articles/consumerDrivenContracts.html)
  with various tools ([Pact](https://docs.pact.io/),
  [Spring Cloud Contract](https://docs.spring.io/spring-cloud-contract/reference/index.html), ...)

### Considered Options

* a Javascript library or framework that can build both single-page applications or server-side rendering technique (
  e.g. [VueJS](https://vuejs.org), [Angular](https://angular.dev/) or [React](https://react.dev/))
* a combination of Spring MVC, Thymeleaf and HTMX

### Decision Outcome

For a first version of the library website, we prefer using some technologies that we already master rather than
investing time to learn a library or framework that we occasionally used in precedent projects, but without mastering
any of them.

The website will then be implemented using Spring MVC with Thymeleaf to render the pages, and HTMX to provide some
reactivity on the pages.
