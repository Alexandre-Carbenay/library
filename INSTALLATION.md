# Installation guide

This guide will help you install your development environment to start collaborating on the project.

## Prerequisites

### Tools installation

* [Git](https://github.com/git-guides/install-git)
* [JDK 21](https://adoptium.net/fr/temurin/releases/)
* [Docker](https://www.docker.com/) and [docker-compose](https://docs.docker.com/compose/install/)
* [jq](https://jqlang.org/download/)

### Configure pre-commit

Git provides pre-commit scripts to execute all sorts of scripts on commit. We will use this mechanism to generate some
images from [PlantUML](https://plantuml.com/) diagrams, as we use PlantUML for documentation diagrams.

Run the following commands to configure the git pre-commit hook (based
on [Pre-Commit PlantUML](https://github.com/weikangchia/pre-commit-hooks-plantuml)):

```shell
echo -e '#!/bin/bash\n\n"$(git rev-parse --git-dir)/../.github/hooks/plantuml.pre-commit.sh"' > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Configure Github Packages usage

The shared libraries under the [libs](./libs) folder are built as maven artifacts, stored on Github Packages.

To be able to use those packages, you need to authenticate first. See the
[Github documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages)
to know how to configure your personal token to access the Github packages.

Then, create a `gradle.properties` file in your local Gradle home directory (usually `~/.gradle/gradle.properties`), or
[any location](https://docs.gradle.org/current/userguide/build_environment.html) that suits you best and fill it with
the following:

```properties
gpr.user=<your github username>
gpr.key=<your github token>
```
