ENV_LOCATION=..
PACT_LOCATION=../pact
WEBSITE_LOCATION=.

# Defined before inclusions to ensure it is the first target
build: build-website ## Build the website application

include ./website.mk ../common.mk
-include ../.env

start: start-website ## Start the website application

stop: stop-website ## Stop the website application

clean: clean-website ## Clean the website project folder
