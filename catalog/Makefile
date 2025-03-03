ENV_LOCATION=..
PACT_LOCATION=../pact
CATALOG_LOCATION=.

# Defined before inclusions to ensure it is the first target
build: build-catalog ## Build the catalog application

include ./catalog.mk ../common.mk
-include ../.env

build-compile: build-catalog-compile ## Compile the catalog application

build-test: build-catalog-test ## Run tests on the catalog application

build-test-publish: build-catalog-test-publish ## Publish tests report for the catalog application

build-jar: build-catalog-jar ## Build JAR for the catalog application

build-docker: build-catalog-docker ## Build Docker image for the catalog application

start: start-catalog ## Start the catalog application

stop: stop-catalog ## Stop the catalog application

acceptance: acceptance-catalog ## Run acceptance tests on the catalog application

clean: clean-catalog ## Clean the catalog project folder
