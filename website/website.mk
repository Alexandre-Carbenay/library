# Define environment variables for targets executions (excluding docker compose), and override from .env if exist
WEBSITE_PORT=9000
CATALOG_API_PORT=8080

# Prepare environment variables for docker compose from .env if exist, using --env-file parameter
INCLUDE_ENV=
ifneq (,$(wildcard $(ENV_LOCATION)/.env))
	INCLUDE_ENV=--env-file $(ENV_LOCATION)/.env
endif

gradleWebsite = $(WEBSITE_LOCATION)/gradlew -p $(WEBSITE_LOCATION)

build-website: ## Build the website application
	$(gradleWebsite) check bootJar && \
		docker build -t website:latest --build-arg REVISION=$(REVISION) --build-arg VERSION=$(VERSION) --build-arg CREATION_DATE=$(shell date --utc --iso-8601=seconds) $(WEBSITE_LOCATION)

start-website: ## Start the website application
	docker compose $(INCLUDE_ENV) \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.port.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.dependencies.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.dependencies.port.yml \
		up -d

stop-website: ## Stop the website application
	docker compose $(INCLUDE_ENV) \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.port.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.dependencies.yml \
		-f $(WEBSITE_LOCATION)/docker/docker-compose.dependencies.port.yml \
		down

clean-website: ## Clean the website project folder
	$(gradleWebsite) clean
