# Config Server

## Overview
Centralized configuration management. Provides external configuration for all microservices.

## Port
`8888`

## Responsibilities
- Centralized config storage
- Dynamic config refresh via Spring Cloud Bus
- Environment-specific configurations
- Single source of truth for settings

## How It Works
1. Each service requests config on startup
2. Config Server returns YAML based on service name
3. Spring Cloud Bus broadcasts refresh events via Kafka

## Configuration Location