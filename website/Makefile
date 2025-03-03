ENV_LOCATION=..
PACT_LOCATION=../pact
WEBSITE_LOCATION=.

# Defined before inclusions to ensure it is the first target
build: build-website ## Build the website application

include ./website.mk ../common.mk
-include ../.env

build-compile: build-website-compile ## Compile the website application

build-test: build-website-test ## Run tests on the website application

build-test-publish: build-website-test-publish ## Publish tests report for the website application

build-jar: build-website-jar ## Build JAR for the website application

build-docker: build-website-docker ## Build Docker image for the website application

start: start-website ## Start the website application

stop: stop-website ## Stop the website application

clean: clean-website ## Clean the website project folder
