# =============================================================================
# docker-bake.hcl
#
# Declarative build definitions for all 9 Fraud Shield services, used by
# `docker buildx bake`. Centralizes registry, tags, build context, and cache
# config here so docker-ci.yml only has to say WHICH targets to build - not
# HOW to build them.
#
# Tag strategy: every CI build gets "latest" (a moving pointer to the most
# recent build) and a git-SHA tag (an exact, permanent, traceable reference
# to this specific commit). Semantic version tags (v1.0.0 etc.) are NOT
# applied here - those are added separately, only during an explicit human-
# triggered release (see release.yml), never on every ordinary CI run.
# =============================================================================

variable "REGISTRY" {
  default = "ghcr.io/saurav-kumarr/fraud-shield"
}

variable "GIT_SHA" {
  default = "dev"
}

function "tags" {
  params = [service]
  result = [
    "${REGISTRY}/${service}:latest",
    "${REGISTRY}/${service}:${GIT_SHA}",
  ]
}

group "default" {
  targets = [
    "eureka-server",
    "config-server",
    "api-gateway",
    "transaction-service",
    "fraud-detection-service",
    "alert-service",
    "user-service",
    "report-service",
    "fraud-shield-dashboard",
  ]
}

# ---- Backend services (Java, multi-module reactor) ----
# context = "." (repo root) is required for all of these, NOT the service
# folder - each service's pom.xml has a <parent> pointing at the root
# pom.xml, and Docker can only COPY files inside its build context.

target "eureka-server" {
  context    = "."
  dockerfile = "eureka-server/Dockerfile"
  tags       = tags("eureka-server")
  cache-from = ["type=gha,scope=eureka-server"]
  cache-to   = ["type=gha,mode=max,scope=eureka-server"]
}

target "config-server" {
  context    = "."
  dockerfile = "config-server/Dockerfile"
  tags       = tags("config-server")
  cache-from = ["type=gha,scope=config-server"]
  cache-to   = ["type=gha,mode=max,scope=config-server"]
}

target "api-gateway" {
  context    = "."
  dockerfile = "api-gateway/Dockerfile"
  tags       = tags("api-gateway")
  cache-from = ["type=gha,scope=api-gateway"]
  cache-to   = ["type=gha,mode=max,scope=api-gateway"]
}

target "transaction-service" {
  context    = "."
  dockerfile = "transaction-service/Dockerfile"
  tags       = tags("transaction-service")
  cache-from = ["type=gha,scope=transaction-service"]
  cache-to   = ["type=gha,mode=max,scope=transaction-service"]
}

target "fraud-detection-service" {
  context    = "."
  dockerfile = "fraud-detection-service/Dockerfile"
  tags       = tags("fraud-detection-service")
  cache-from = ["type=gha,scope=fraud-detection-service"]
  cache-to   = ["type=gha,mode=max,scope=fraud-detection-service"]
}

target "alert-service" {
  context    = "."
  dockerfile = "alert-service/Dockerfile"
  tags       = tags("alert-service")
  cache-from = ["type=gha,scope=alert-service"]
  cache-to   = ["type=gha,mode=max,scope=alert-service"]
}

target "user-service" {
  context    = "."
  dockerfile = "user-service/Dockerfile"
  tags       = tags("user-service")
  cache-from = ["type=gha,scope=user-service"]
  cache-to   = ["type=gha,mode=max,scope=user-service"]
}

target "report-service" {
  context    = "."
  dockerfile = "report-service/Dockerfile"
  tags       = tags("report-service")
  cache-from = ["type=gha,scope=report-service"]
  cache-to   = ["type=gha,mode=max,scope=report-service"]
}

# ---- Frontend (standalone npm project, NOT part of the Maven reactor) ----
target "fraud-shield-dashboard" {
  context    = "./fraud-shield-dashboard"
  dockerfile = "Dockerfile"
  tags       = tags("fraud-shield-dashboard")
  cache-from = ["type=gha,scope=fraud-shield-dashboard"]
  cache-to   = ["type=gha,mode=max,scope=fraud-shield-dashboard"]
}