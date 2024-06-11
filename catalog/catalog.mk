gradleCatalog = $(CATALOG_LOCATION)/gradlew -p $(CATALOG_LOCATION)

build-catalog: ## Build the catalog application
	$(gradleCatalog) check bootJar

clean-catalog: ## Clean the catalog project folder
	$(gradleCatalog) clean
