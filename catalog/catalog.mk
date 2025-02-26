# Define environment variables for targets executions (excluding docker compose), and override from .env if exist
CATALOG_API_PORT=8080

# Prepare environment variables for docker compose from .env if exist, using --env-file parameter
INCLUDE_ENV=
ifneq (,$(wildcard $(ENV_LOCATION)/.env))
	INCLUDE_ENV=--env-file $(ENV_LOCATION)/.env
endif

gradleCatalog = $(CATALOG_LOCATION)/gradlew -p $(CATALOG_LOCATION)

build-catalog: build-catalog-compile build-catalog-test build-catalog-jar build-catalog-docker ## Build the catalog application

build-catalog-compile: ## Compile the catalog application
	$(gradleCatalog) classes

build-catalog-test: ## Run tests on the catalog application
	$(MAKE) start-pact-broker
	$(gradleCatalog) check
	$(MAKE) stop-pact-broker

build-catalog-test-publish: ## Publish tests report for the catalog application
	# For now, we don't publish anything

build-catalog-jar: ## Build JAR for the catalog application
	$(gradleCatalog) bootJar

build-catalog-docker: ## Build Docker image for the catalog application
	docker build -t catalog:latest --build-arg REVISION=$(REVISION) --build-arg VERSION=$(VERSION) --build-arg CREATION_DATE=$(shell date --utc --iso-8601=seconds) $(CATALOG_LOCATION)

start-catalog: ## Start the catalog application
	docker compose $(INCLUDE_ENV) \
		-f $(CATALOG_LOCATION)/docker/docker-compose.yml \
		-f $(CATALOG_LOCATION)/docker/docker-compose.port.yml \
		up -d

stop-catalog: ## Stop the catalog application
	docker compose $(INCLUDE_ENV) \
		-f $(CATALOG_LOCATION)/docker/docker-compose.yml \
		-f $(CATALOG_LOCATION)/docker/docker-compose.port.yml \
		down

acceptance-catalog: ## Run acceptance tests on the catalog application
	$(gradleCatalog) acceptance

clean-catalog: ## Clean the catalog project folder
	$(gradleCatalog) clean
