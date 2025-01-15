ENV_LOCATION=..
PACT_LOCATION=../pact
CATALOG_LOCATION=.

# Defined before inclusions to ensure it is the first target
build: build-catalog ## Build the catalog application

include ./catalog.mk ../common.mk
-include ../.env

start: start-catalog ## Start the catalog application

stop: stop-catalog ## Stop the catalog application

acceptance: acceptance-catalog ## Run acceptance tests on the catalog application

clean: clean-catalog ## Clean the catalog project folder
