ENV_LOCATION=.
PACT_LOCATION=./pact
CATALOG_LOCATION=./catalog
WEBSITE_LOCATION=./website

# Defined before inclusions to ensure it is the first target
build: build-catalog build-website ## Build all the applications

include $(CATALOG_LOCATION)/catalog.mk $(WEBSITE_LOCATION)/website.mk common.mk

acceptance: acceptance-catalog ## Run acceptance tests on all the applications

clean: clean-catalog clean-website ## Clean the projects folders

start: start-website ## Start all the applications

stop: stop-website ## Stop all the applications

structurizr: ## Generate Structurizr C4 diagrams
	./doc/util/structurizr/export-diagrams.sh doc/util/structurizr
