# Library

This is an opensource project aimed at demonstrating the usage of many different tools and practices in a concrete
example of library system.

Those tools and practices include:

- the [C4 model](https://c4model.com/) and tooling to generate schemas from the model.
- the API specification-first approach, bringing high quality documentation and automatic basic validation.
- the [Behavior-driven Development](https://en.wikipedia.org/wiki/Behavior-driven_development) (BDD) approach to define
  features specifications and relevant acceptance scenarii, and automate the execution of those scenarii using JUnit and
  Cucumber.

## Project structure

The project is structured as a mono-repository, with each individual application (i.e. [C4 model](https://c4model.com/)
container) having its dedicated folder (e.g. [catalog](./catalog) for the catalog API).

Here is the C4 system context diagram of the Library system:

![Library system context](doc/architecture/c4/Library-Context.png)

And the container diagram representing the Library system:

![Library containers](doc/architecture/c4/Library-Containers.png)

## Usage

Each application provides a `Makefile` with similar targets to build and run the application. Use `make help` to get the
list of all available targets and their purpose.

Here is a list of the common targets:

- `build` to build the application as a docker image
- `start` to start the application and all its dependencies using docker compose
- `stop` to stop the application previously started with `start`
- `acceptance` to run the application's acceptance tests in a docker compose environment

### Environment configuration

To configure the execution environment, you can create a `.env` file at the root of the project, in which you can declare
properties that will be used during execution. By default, those values are set as:

```properties
PACT_BROKER_PORT=9292
CATALOG_API_PORT=8080
WEBSITE_PORT=9000
```
