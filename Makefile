CATALOG_LOCATION=./catalog

# Defined before inclusions to ensure it is the first target
build: build-catalog ## Build all the applications

include $(CATALOG_LOCATION)/catalog.mk common.mk

clean: clean-catalog ## Clean the projects folders
