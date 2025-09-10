ENV_LOCATION=.
PACT_LOCATION=./pact
CATALOG_LOCATION=./catalog
REFERENCING_LOCATION=./referencing
WEBSITE_LOCATION=./website

# Defined before inclusions to ensure it is the first target
build: build-catalog build-referencing build-website ## Build all the applications

include $(CATALOG_LOCATION)/catalog.mk $(REFERENCING_LOCATION)/referencing.mk $(WEBSITE_LOCATION)/website.mk common.mk

acceptance: acceptance-catalog ## Run acceptance tests on all the applications

clean: clean-catalog clean-referencing clean-website ## Clean the projects folders

start: start-website ## Start all the applications

stop: stop-website ## Stop all the applications

structurizr: ## Generate Structurizr C4 diagrams
	./doc/util/structurizr/export-diagrams.sh doc/util/structurizr
