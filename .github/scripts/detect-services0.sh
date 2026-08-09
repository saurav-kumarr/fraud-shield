#!/bin/bash

set -e

echo "Detecting changed services..."

BASE_SHA="${{ github.event.before }}"
HEAD_SHA="${{ github.sha }}"

if [ -z "$BASE_SHA" ] || [ "$BASE_SHA" = "0000000000000000000000000000000000000000" ]; then
    BASE_SHA=$(git rev-list --max-parents=0 HEAD)
fi

SERVICES=()

SERVICE_LIST=(
eureka-server
config-server
api-gateway
transaction-service
fraud-detection-service
alert-service
user-service
report-service
fraud-shield-dashboard
)

for SERVICE in "${SERVICE_LIST[@]}"
do
    if git diff --name-only "$BASE_SHA" "$HEAD_SHA" | grep -q "^$SERVICE/"; then
        SERVICES+=("$SERVICE")
    fi
done

echo "Changed services: ${SERVICES[*]}"

printf "%s\n" "${SERVICES[@]}" > changed-services.txt