#!/usr/bin/env bash
# =============================================================================
# detect-services.sh
#
# Determines which of the 9 Fraud Shield services actually changed since the
# last successful CI build, so docker-ci.yml only builds/pushes what's new -
# directly solving "every push builds every service."
#
# BASELINE: a lightweight git tag "last-ci-build" that docker-ci.yml moves to
# point at HEAD after every successful build+push. This works correctly
# whether CI runs on every push or sporadically via manual dispatch (this
# repo's actual pattern) - the diff is always "since the last real build,"
# never "since the last commit" or "since the last push."
#
# OUTPUT: prints a space-separated list of changed service names to stdout,
# in the same format docker-ci.yml's old $SERVICES variable used. Prints
# nothing (empty string) if nothing changed.
# =============================================================================
set -euo pipefail

ALL_SERVICES="eureka-server config-server api-gateway transaction-service fraud-detection-service alert-service user-service report-service fraud-shield-dashboard"
BACKEND_SERVICES="eureka-server config-server api-gateway transaction-service fraud-detection-service alert-service user-service report-service"

# --- Bootstrap case: no baseline exists yet (first-ever run) ---
if ! git rev-parse last-ci-build >/dev/null 2>&1; then
  echo "No last-ci-build tag found - first run, building everything." >&2
  echo "$ALL_SERVICES"
  exit 0
fi

CHANGED_FILES=$(git diff --name-only last-ci-build..HEAD)

if [ -z "$CHANGED_FILES" ]; then
  echo "No files changed since last-ci-build - nothing to build." >&2
  echo ""
  exit 0
fi

echo "Files changed since last-ci-build:" >&2
echo "$CHANGED_FILES" | sed 's/^/  /' >&2

CHANGED_SERVICES=""

# --- Case 1: the parent POM changed -> every backend Java service is
# affected, since they all inherit shared dependency/plugin versions from it.
if echo "$CHANGED_FILES" | grep -qx "pom.xml"; then
  echo "Parent pom.xml changed - all backend services affected." >&2
  CHANGED_SERVICES="$BACKEND_SERVICES"
fi

# --- Case 2: the build pipeline itself changed -> rebuild EVERYTHING
# (backend + frontend) as a safety net, since we can't be sure what changed
# in the orchestration would do to any individual service's build.
PIPELINE_FILES="docker-bake.hcl .github/workflows/docker-ci.yml .github/scripts/detect-services.sh"
for f in $PIPELINE_FILES; do
  if echo "$CHANGED_FILES" | grep -qx "$f"; then
    echo "Pipeline file changed ($f) - rebuilding all services as a safety net." >&2
    CHANGED_SERVICES="$ALL_SERVICES"
  fi
done

# --- Case 3: normal per-service detection - anything under <service>/ ---
if [ "$CHANGED_SERVICES" != "$ALL_SERVICES" ]; then
  for svc in $ALL_SERVICES; do
    # Skip services already captured by Case 1 above.
    if echo " $CHANGED_SERVICES " | grep -qw "$svc"; then
      continue
    fi
    if echo "$CHANGED_FILES" | grep -q "^${svc}/"; then
      CHANGED_SERVICES="$CHANGED_SERVICES $svc"
    fi
  done
fi

# Trim, dedupe, normalize whitespace into a single space-separated line.
CHANGED_SERVICES=$(echo "$CHANGED_SERVICES" | xargs -n1 2>/dev/null | sort -u | xargs)

echo "$CHANGED_SERVICES"
