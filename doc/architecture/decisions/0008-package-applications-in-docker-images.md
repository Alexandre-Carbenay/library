## Package applications in docker image

* Status: ACCEPTED
* Deciders:
    * [Alexandre Carbenay](mailto:acarbenay@adhuc.fr)
* Date: 2024-09-25
* Scope: General

### Context and Problem Statement

Each library service needs to be packaged as a deployable artifact, able to run in every environment with as few
installation / configuration as possible.

Ideally, the same technology should be used to run a service in all environments, including cloud environments.

A containerizable artifact would be ideal to be able to deploy in a Kubernetes environment.

### Decision Drivers

* Docker image is the most widely known and used container technology
* Docker can be easily used in developer environment (linux & windows), directly or through docker-compose
* Docker is supported by most cloud container technologies, including Kubernetes
* Major open source technologies provide official docker images
* Docker ecosystem provides tools to scan images vulnerabilities (i.e. Prisma)

### Decision Outcome

Docker images will be used to package the applications.
