# Define environment variables for targets executions (excluding docker compose), and override from .env if exist
REFERENCING_API_PORT=8081

# Prepare environment variables for docker compose from .env if exist, using --env-file parameter
INCLUDE_ENV=
ifneq (,$(wildcard $(ENV_LOCATION)/.env))
	INCLUDE_ENV=--env-file $(ENV_LOCATION)/.env
endif

gradleReferencing = $(REFERENCING_LOCATION)/gradlew -p $(REFERENCING_LOCATION)

build-referencing: build-referencing-compile build-referencing-test build-referencing-jar build-referencing-docker ## Build the referencing application

build-referencing-compile: ## Compile the referencing application
	$(gradleReferencing) classes

build-referencing-test: ## Run tests on the referencing application
	$(gradleReferencing) check

build-referencing-test-publish: ## Publish tests report for the referencing application
	# For now, we don't publish anything

build-referencing-jar: ## Build JAR for the referencing application
	$(gradleReferencing) bootJar

build-referencing-docker: ## Build Docker image for the referencing application
	docker build -t referencing:latest --build-arg REVISION=$(REVISION) --build-arg VERSION=$(VERSION) --build-arg CREATION_DATE=$(shell date --utc --iso-8601=seconds) $(REFERENCING_LOCATION)

start-referencing: ## Start the referencing application
	docker compose $(INCLUDE_ENV) \
		-f $(REFERENCING_LOCATION)/docker/docker-compose.yml \
		-f $(REFERENCING_LOCATION)/docker/docker-compose.port.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.port.yml \
		up -d

stop-referencing: ## Stop the referencing application
	docker compose $(INCLUDE_ENV) \
		-f $(REFERENCING_LOCATION)/docker/docker-compose.yml \
		-f $(REFERENCING_LOCATION)/docker/docker-compose.port.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.port.yml \
		down

acceptance-referencing: ## Run acceptance tests on the referencing application
	$(gradleReferencing) acceptance

clean-referencing: ## Clean the referencing project folder
	$(gradleReferencing) clean
