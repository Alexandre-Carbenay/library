gradleCatalog = $(CATALOG_LOCATION)/gradlew -p $(CATALOG_LOCATION)

build-catalog: ## Build the catalog application
	$(gradleCatalog) check bootJar && \
		docker build -t catalog:latest --build-arg REVISION=$(REVISION) --build-arg VERSION=$(VERSION) --build-arg CREATION_DATE=$(shell date --utc --iso-8601=seconds) $(CATALOG_LOCATION)

clean-catalog: ## Clean the catalog project folder
	$(gradleCatalog) clean
