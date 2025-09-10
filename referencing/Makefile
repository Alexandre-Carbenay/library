ENV_LOCATION=..
PACT_LOCATION=../pact
REFERENCING_LOCATION=.

# Defined before inclusions to ensure it is the first target
build: build-referencing ## Build the referencing application

include ./referencing.mk ../common.mk
-include ../.env

build-compile: build-referencing-compile ## Compile the referencing application

build-test: build-referencing-test ## Run tests on the referencing application

build-test-publish: build-referencing-test-publish ## Publish tests report for the referencing application

build-jar: build-referencing-jar ## Build JAR for the referencing application

build-docker: build-referencing-docker ## Build Docker image for the referencing application

start: start-referencing ## Start the referencing application

stop: stop-referencing ## Stop the referencing application

acceptance: acceptance-referencing ## Run acceptance tests on the referencing application

clean: clean-referencing ## Clean the referencing project folder
