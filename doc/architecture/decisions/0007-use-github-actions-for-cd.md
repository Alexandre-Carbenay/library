## Use Github actions for CI/CD

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-06-11
* Scope: General

### Context and Problem Statement

To achieve Continuous Integration and Continuous Delivery, we need to run a deployment pipeline with an automated tool.

### Decision Drivers

The chosen tool must:

* be integrated with Github
* support multi-stage pipelines
* has a configuration-as-code approach

### Considered Options

* [Github Actions](https://docs.github.com/en/actions)
* [Travis CI](https://www.travis-ci.com/)
* [CircleCI](https://circleci.com/)

### Decision Outcome

Github Actions will be used to define and run the deployment pipeline.

Other tools have equivalent features for simple pipeline implementation, but are not so easily integrated in the Github
ecosystem.
