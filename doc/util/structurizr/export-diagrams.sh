#!/bin/bash

DIRECTORY=$1

echo 'Start structurizr and exporter'
docker compose -f ./$DIRECTORY/docker-compose.yml up &

until $(docker inspect --format "{{.State.Status}}" c4-exporter | grep -q 'exited');
do
    printf '.'
    sleep 1
done
printf '\n'

echo 'Stop structurizr and exporter'
docker compose -f ./$DIRECTORY/docker-compose.yml down
