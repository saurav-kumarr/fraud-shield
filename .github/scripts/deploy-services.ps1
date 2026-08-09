# =============================================================================
# deploy-services.ps1
#
# Deploys ONLY the services it's told to - pulls their new image tag, brings
# them up, health-checks each one, and automatically rolls back to the
# PREVIOUSLY running tag for any service that fails to become healthy.
#
# Services NOT passed in are left completely untouched - no pull, no
# restart, no interruption. This is what makes "restart only changed
# services" real instead of just a comment.
#
# Usage:
#   .\deploy-services.ps1 -Services "transaction-service alert-service" -ImageTag "a1b2c3d"
# =============================================================================
param(
    [Parameter(Mandatory = $true)]
    [string]$Services,

    [Parameter(Mandatory = $true)]
    [string]$ImageTag
)

$ErrorActionPreference = "Stop"
$ComposeFile = "docker-compose.deploy.yml"
$ServiceList = $Services -split '\s+' | Where-Object { $_ -ne '' }

if ($ServiceList.Count -eq 0) {
    Write-Host "No services to deploy - nothing to do." -ForegroundColor Yellow
    exit 0
}

# Health check endpoint per service. eureka-server and the dashboard don't
# have Spring Actuator - eureka's own landing page and nginx's root both
# always respond regardless, same reasoning as the Docker Compose healthchecks.
$HealthPaths = @{
    "eureka-server"           = "http://localhost:8761/"
    "config-server"           = "http://localhost:8888/actuator/health"
    "api-gateway"             = "http://localhost:8080/actuator/health"
    "transaction-service"     = "http://localhost:8081/actuator/health"
    "fraud-detection-service" = "http://localhost:8082/actuator/health"
    "alert-service"           = "http://localhost:8083/actuator/health"
    "user-service"            = "http://localhost:8084/actuator/health"
    "report-service"          = "http://localhost:8085/actuator/health"
    "fraud-shield-dashboard"  = "http://localhost:5173/"
}

Write-Host "=== Deploying: $($ServiceList -join ', ') ===" -ForegroundColor Cyan
Write-Host "=== Image tag: $ImageTag ===" -ForegroundColor Cyan

# --- Step 1: capture what's currently running, BEFORE touching anything ---
# This is the rollback safety net - if the new version fails health checks,
# we redeploy exactly this tag using compose itself, not a bare `docker run`
# that would lose ports/env/network/healthcheck config.
Write-Host "`n--- Capturing current image tags (rollback safety net) ---" -ForegroundColor Cyan
$PreviousTags = @{}
foreach ($svc in $ServiceList) {
    $containerName = "fraud-shield-$svc"
    $currentImage = docker inspect --format '{{.Config.Image}}' $containerName 2>$null
    if ($LASTEXITCODE -eq 0 -and $currentImage) {
        $prevTag = $currentImage.Substring($currentImage.LastIndexOf(':') + 1)
        $PreviousTags[$svc] = $prevTag
        Write-Host "  $svc currently on tag: $prevTag"
    }
    else {
        Write-Host "  $svc has no existing container (first deploy - no rollback target)"
    }
}

# --- Step 2: pull the new images for ONLY these services ---
Write-Host "`n--- Pulling new images ---" -ForegroundColor Cyan
$env:IMAGE_TAG = $ImageTag
docker compose -f $ComposeFile pull $ServiceList
if ($LASTEXITCODE -ne 0) {
    Write-Error "Pull failed for one or more services - aborting before touching any running containers."
    exit 1
}

# --- Step 3: bring up ONLY these services ---
Write-Host "`n--- Starting new containers ---" -ForegroundColor Cyan
docker compose -f $ComposeFile up -d --remove-orphans $ServiceList
if ($LASTEXITCODE -ne 0) {
    Write-Error "docker compose up failed."
    exit 1
}

# --- Step 4: health check each deployed service, with retries ---
Write-Host "`n--- Health checking ---" -ForegroundColor Cyan
$MaxRetries = 10
$RetryDelaySeconds = 6
$Failed = @()

foreach ($svc in $ServiceList) {
    $url = $HealthPaths[$svc]
    if (-not $url) {
        Write-Host "  $svc - no health endpoint configured, skipping check" -ForegroundColor Yellow
        continue
    }

    $healthy = $false
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Host "  $svc - healthy (attempt $i/$MaxRetries)" -ForegroundColor Green
                $healthy = $true
                break
            }
        }
        catch {
            Write-Host "  $svc - not ready yet (attempt $i/$MaxRetries)" -ForegroundColor Yellow
        }
        Start-Sleep -Seconds $RetryDelaySeconds
    }

    if (-not $healthy) {
        Write-Host "  $svc - FAILED health check after $MaxRetries attempts ($($MaxRetries * $RetryDelaySeconds)s)" -ForegroundColor Red
        $Failed += $svc
    }
}

# --- Step 5: automatic rollback for anything that failed ---
if ($Failed.Count -gt 0) {
    Write-Host "`n=== Rolling back failed service(s) ===" -ForegroundColor Red
    foreach ($svc in $Failed) {
        if ($PreviousTags.ContainsKey($svc)) {
            $prevTag = $PreviousTags[$svc]
            Write-Host "  Rolling back $svc to previous tag: $prevTag"
            # Re-run through compose with IMAGE_TAG set back to the old value -
            # this rebuilds the FULL correct service definition (ports, env,
            # network, healthcheck) from docker-compose.deploy.yml, not just
            # a bare container with none of that config.
            $env:IMAGE_TAG = $prevTag
            docker compose -f $ComposeFile up -d $svc
        }
        else {
            Write-Host "  $svc has no previous tag recorded - cannot auto-rollback, manual intervention needed" -ForegroundColor Red
        }
    }
    Write-Host "`nDeployment FAILED - $($Failed.Count) service(s) did not become healthy." -ForegroundColor Red
    docker compose -f $ComposeFile ps
    exit 1
}

Write-Host "`n=== Deployment successful - all services healthy ===" -ForegroundColor Green
docker compose -f $ComposeFile ps
