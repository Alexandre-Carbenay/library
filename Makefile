ENV_LOCATION=.
CATALOG_LOCATION=./catalog

# Defined before inclusions to ensure it is the first target
build: build-catalog ## Build all the applications

include $(CATALOG_LOCATION)/catalog.mk common.mk

acceptance: acceptance-catalog ## Run acceptance tests on all the applications

clean: clean-catalog ## Clean the projects folders

structurizr: ## Generate Structurizr C4 diagrams
	./doc/util/structurizr/export-diagrams.sh doc/util/structurizr
