#!/bin/bash

MAX_WAIT_TIME=$1
PORT=$2
PACT_LOCATION=$3

# Wait for pact broker to startup
printf "Waiting to initialize pact broker ($MAX_WAIT_TIME seconds)\n"
count=0
until $(curl --output /dev/null --silent --fail http://localhost:$PORT) || ((count == MAX_WAIT_TIME)); do
   printf '.'
   ((count++))
   sleep 1
done

if ((count < MAX_WAIT_TIME))
then
    printf "\nPact broker up in $count seconds\n"
else
    printf "\nFailed to start pact broker under $MAX_WAIT_TIME seconds\n"
    curl http://localhost:$PORT
    printf '\n'
    exit 1
fi

for PACT in "$PACT_LOCATION"/contents/*.json; do
  echo "Initialize pact from location: $PACT"
  CONSUMER=$(jq -r '.consumer.name' $PACT)
  PROVIDER=$(jq -r '.provider.name' $PACT)
  VERSION=$(git rev-parse --short HEAD)
  curl -X PUT --output /dev/null --silent http://localhost:$PORT/pacts/provider/$PROVIDER/consumer/$CONSUMER/version/$VERSION -H "Content-Type: application/json" -d "@./$PACT"
done
