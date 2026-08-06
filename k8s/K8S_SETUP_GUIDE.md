# Fraud Shield — Kubernetes Setup Guide (Phase 3)

This runs your entire Fraud Shield stack on **Minikube** — a free, single-node Kubernetes
cluster that runs on your own machine. It demonstrates the exact same skills as a cloud
cluster (EKS/GKE) without any cost, which matches your Docker deployment decision earlier
(you skipped AWS for the same reason).

**Scope note:** to keep resource usage realistic for a low-budget machine, this phase
covers your 8 microservices + their databases/Kafka only. The observability stack
(Prometheus/Grafana/Loki/Tempo/Alloy) stays in Docker Compose for local dev — running
all of that *and* a full app stack on one small machine's Kubernetes cluster isn't a
realistic trade-off, and you can say exactly that in an interview if asked.

---

## 1. Install Minikube (one-time)

```bash
# Windows (using Chocolatey) - or download the installer from minikube.sigs.k8s.io
choco install minikube kubernetes-cli

# Start a cluster sized for this project
minikube start --cpus=4 --memory=6000mb

# Enable auto-scaling support for the HPA on fraud-detection-service
minikube addons enable metrics-server
```

If your machine has less than 8GB RAM total, lower `--memory` to `4000mb` and reduce the
`resources.limits` values in the YAML files (e.g. `512Mi` → `384Mi`) — the app will still
run, just with less headroom.

---

## 2. Where these files go

```
fraud-shield/
└── k8s/                          <- create this new folder in your project root
    ├── 00-namespace.yaml
    ├── 01-config-and-secrets.yaml
    ├── 10-postgres.yaml
    ├── 11-mongodb.yaml
    ├── 12-redis.yaml
    ├── 13-zookeeper.yaml
    ├── 14-kafka.yaml
    ├── 20-eureka-server.yaml
    ├── 21-config-server.yaml
    ├── 22-api-gateway.yaml
    ├── 23-transaction-service.yaml
    ├── 24-fraud-detection-service.yaml
    ├── 25-alert-service.yaml
    ├── 26-user-service.yaml
    └── 27-report-service.yaml
```

The number prefixes control apply order: namespace and config first, then databases,
then services that depend on them — same reasoning as the `depends_on` chain in your
Docker Compose file.

---

## 3. Deploy everything

```bash
# Point your terminal at the Minikube cluster (usually automatic after `minikube start`)
kubectl config use-context minikube

# Apply everything in the folder, in order
kubectl apply -f k8s/

# Watch pods come up
kubectl get pods -n fraud-shield -w
```

Wait until every pod shows `Running` and `1/1` (or `3/3` for fraud-detection-service)
under READY. First-time image pulls take a few minutes.

---

## 4. Verify it's working

```bash
# See everything in the namespace at once
kubectl get all -n fraud-shield

# Confirm fraud-detection-service really has 3 pods (your 3 Kafka partitions)
kubectl get pods -n fraud-shield -l app=fraud-detection-service

# Check the HPA is watching CPU
kubectl get hpa -n fraud-shield

# Reach the API Gateway from your machine
minikube service api-gateway -n fraud-shield --url
# then curl the URL it prints, e.g.:
curl $(minikube service api-gateway -n fraud-shield --url)/actuator/health

# Check logs for any one service
kubectl logs -n fraud-shield deployment/fraud-detection-service
```

## 5. See the parallelism from the concurrency guide, live

```bash
# Watch all 3 fraud-detection-service pods at once
kubectl logs -n fraud-shield -l app=fraud-detection-service -f --prefix
```

Send a handful of transactions through the gateway and watch the log prefixes —
you'll see different pod names handling different transactions, which is the literal,
visible proof of the "3 partitions → 3 parallel consumers" story, now running on a
real orchestrator instead of one Docker container.

---

## 6. Cleaning up

```bash
kubectl delete namespace fraud-shield   # removes everything in one command
minikube stop                            # or: minikube delete to remove the cluster entirely
```

---

## 7. What's genuinely different from Docker Compose (know this distinction)

| Docker Compose | Kubernetes |
|---|---|
| `depends_on` + healthcheck | `readinessProbe` / `livenessProbe` |
| Manual `docker compose up --scale` | `replicas: 3` + HPA auto-scales for you |
| One host, no self-healing | Kubernetes restarts crashed pods automatically |
| `.env` values in compose file | ConfigMap (non-secret) + Secret (sensitive), same pattern |
| Kafka needed 2 listeners (host vs container) | Kafka needs only 1 (everything is "inside" the cluster) |

---

## 8. Resume bullets for this phase

> Deployed the full 8-service microservices architecture to Kubernetes (Minikube),
> including PostgreSQL, MongoDB, Redis, and Kafka, with liveness/readiness probes,
> ConfigMaps/Secrets for environment-specific configuration, and a Horizontal Pod
> Autoscaler on the fraud-detection service scaling 3-10 pods on CPU utilization.

> Ran fraud-detection-service with 3 replicas matched to its Kafka partition count,
> demonstrating real parallel fraud-check processing across pods within one consumer
> group.

## 9. Interview Q&A for this phase

**Q: Why 3 replicas for fraud-detection-service specifically?**
"It matches my Kafka topic's 3 partitions. Kafka only lets one consumer per partition
within a group, so 3 replicas is the exact number that gives full parallelism without
any idle pods. If I needed more throughput, I'd increase partitions and replicas together."

**Q: How does the HPA decide when to scale?**
"It watches average CPU utilization across the fraud-detection-service pods via the
metrics-server. Past 70% average utilization it adds pods, up to a max of 10, and
scales back down when load drops - so I get burst capacity without paying for it at
idle."

**Q: Why ConfigMap and Secret separately instead of one config source?**
"ConfigMap holds values that are safe to see in plain text - hostnames, URLs. Secret
holds things like database passwords and the JWT signing key, which Kubernetes
base64-encodes and which I'd back with a proper secrets manager (Vault, AWS Secrets
Manager) in a real production cluster instead of committing values to a YAML file."

**Q: What would you change for a real production cluster vs this Minikube demo?**
"Move the databases to managed services (RDS, Atlas, ElastiCache) instead of
StatefulSets I manage myself, put the images behind a proper Ingress with TLS instead
of a NodePort, and add the observability stack back in via Helm charts. The application
architecture wouldn't change at all - only the infrastructure underneath it."

---

## What's next

That completes Docker → CI/CD → Kubernetes. The last piece is a single PDF that ties
your entire DevOps flow together end-to-end - one diagram from `git push` to a running
pod, plus a master resume/interview script covering all three phases at once.

Say "continue" and I'll build that final guide.
