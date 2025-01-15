# Define environment variables for targets executions (excluding docker compose), and override from .env if exist
CATALOG_API_PORT=8080

# Prepare environment variables for docker compose from .env if exist, using --env-file parameter
INCLUDE_ENV=
ifneq (,$(wildcard $(ENV_LOCATION)/.env))
	INCLUDE_ENV=--env-file $(ENV_LOCATION)/.env
endif

gradleCatalog = $(CATALOG_LOCATION)/gradlew -p $(CATALOG_LOCATION)

build-catalog: ## Build the catalog application
	$(MAKE) start-pact-broker
	$(gradleCatalog) check bootJar && \
		docker build -t catalog:latest --build-arg REVISION=$(REVISION) --build-arg VERSION=$(VERSION) --build-arg CREATION_DATE=$(shell date --utc --iso-8601=seconds) $(CATALOG_LOCATION)
	$(MAKE) stop-pact-broker

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
