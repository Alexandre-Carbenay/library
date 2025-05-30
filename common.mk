# Define environment variables for targets executions (excluding docker compose), and override from .env if exist
PACT_BROKER_PORT=9292
PACT_START_TIMEOUT=10
ZIPKIN_PORT=9411

start-pact-broker: ## Start the Pact broker
	docker compose $(INCLUDE_ENV) \
		-f $(PACT_LOCATION)/docker/docker-compose.yml \
		up -d
	./$(PACT_LOCATION)/docker/init-pacts.sh $(PACT_START_TIMEOUT) $(PACT_BROKER_PORT) $(PACT_LOCATION)

stop-pact-broker: ## Stop the catalog application
	docker compose $(INCLUDE_ENV) \
		-f $(PACT_LOCATION)/docker/docker-compose.yml \
		down --volumes

start-observability: ## Start the Observability tools
	docker compose $(INCLUDE_ENV) \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.port.yml \
		up -d

stop-observability: ## Stop the Observability tools
	docker compose $(INCLUDE_ENV) \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.yml \
		-f $(ENV_LOCATION)/docker/docker-compose.observability.port.yml \
		down

help: ## This help dialog
	@echo "Usage: make [target]. Find the available targets below:"
	@echo "$$(grep -hE '^\S+:.*##' $(MAKEFILE_LIST) | sed 's/:.*##\s*/:/' | column -c2 -t -s :)"
