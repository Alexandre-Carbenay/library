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

Run the following commands to configure the git pre-commit hook (based on [Pre-Commit PlantUML](https://github.com/weikangchia/pre-commit-hooks-plantuml)):

```shell
echo -e '#!/bin/bash\n\n"$(git rev-parse --git-dir)/../.github/hooks/plantuml.pre-commit.sh"' > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```
