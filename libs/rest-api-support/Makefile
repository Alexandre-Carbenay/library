build: build-compile build-test build-jar ## Build the library

build-compile: ## Compile the library
	./gradlew classes

build-test: ## Run tests on the library
	./gradlew check

build-test-publish: ## Publish tests report for the library
	# For now, we don't publish anything

build-jar: ## Build JAR for the library
	./gradlew jar

clean: ## Clean the library project folder
	$(gradleCatalog) clean
