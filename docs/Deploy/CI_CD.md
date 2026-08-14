# Fraud Shield CI/CD Pipeline — Complete Mastery Guide
### (Expanded Edition: plain-language explanations, extra examples, and a full configuration walkthrough)

> This is a rewrite and expansion of your original interview guide. Everything you already had is kept, but rewritten in plainer language with real-world analogies, plus a lot of new material: CI/CD fundamentals, how each *type* of tool works and why it exists, a full "build this from zero" configuration walkthrough, extra senior-level questions, and a glossary. Read Parts 0–4 once to build real understanding. Skim Parts 5–9 the night before an interview.

---

## Table of Contents

- [Part 0: CI/CD Fundamentals — The Concepts Behind Everything Here](#part-0)
- [Part 1: What This Pipeline Actually Does](#part-1)
- [Part 2: The Complete Flow — One Diagram, One Analogy](#part-2)
- [Part 3: Key Design Decisions — And the Follow-Up Questions They Invite](#part-3)
- [Part 4: The Real Code — Explained Line by Line](#part-4)
- [Part 5: Real Bugs You Solved — Root Cause, Not Just Symptom](#part-5)
- [Part 6: Configuration Walkthrough — "How Would You Set This Up?"](#part-6)
- [Part 7: Senior Interview Q&A](#part-7)
- [Part 8: What Is Deliberately Not Built Yet](#part-8)
- [Part 9: 90-Second Pitch + Resume Bullets](#part-9)
- [Part 10: Bonus — General CI/CD Questions Beyond This Project](#part-10)
- [Part 11: Glossary — Every Term, Defined Simply](#part-11)

---

<a name="part-0"></a>
## Part 0: CI/CD Fundamentals — The Concepts Behind Everything Here

Before touching Fraud Shield specifically, get these five ideas rock-solid. Almost every interview question about your pipeline is really testing whether you understand *these*, using your project as the example.

### 0.1 What "CI" and "CD" actually mean (people mix this up constantly)

| Term | What it means | In one sentence |
|---|---|---|
| **CI** — Continuous Integration | Every time code changes, automatically build it and run checks against it | "Does this still work when combined with everyone else's code?" |
| **Continuous Delivery** | Code is *always* in a state that's ready to release — but a human still clicks the button to actually release it | "We *could* ship this right now, if someone says go." |
| **Continuous Deployment** | Every change that passes CI is automatically released to production, no human involved | "It ships itself." |

**💡 Where does Fraud Shield sit?** It's CI (automated) + **on-demand, one-click Continuous Delivery** — not full Continuous Deployment. You manually trigger the run, but from that single click, everything (build → push → deploy → verify → rollback) happens with zero further human input.

**🎤 Say this in the interview:**
> "It's not strictly continuous deployment, because I chose a manual trigger — I have other workflows in the repo that already own the push trigger, and I didn't want two workflows racing on the same event. But everything downstream of that one click is fully automated: build, push, deploy, health-check, rollback. So it's closer to on-demand continuous delivery."

This answer is good because it shows you know the *precise* terminology instead of saying "yeah it's CI/CD" and hoping nobody asks a follow-up.

### 0.2 Why any of this exists — the problem before CI/CD

Picture a team without any of this: someone finishes a feature, zips their project folder, emails it to another engineer, who manually copies it onto a server, restarts things by hand, and hopes nothing else broke. This was normal for decades. Problems:

- **"Works on my machine"** — no guarantee the build environment matches production.
- **Slow feedback** — you find out something's broken days later, not in 5 minutes.
- **Manual deploys are error-prone** — humans forget steps under pressure.
- **No fast way back** — if something breaks, "undo" means someone remembering what the old version looked like.

CI/CD attacks all four: automated, identical build steps every time (no "works on my machine"), fast feedback (build fails in minutes), a scripted and repeatable deploy (no forgotten steps), and — the part your pipeline focuses hardest on — **a fast, automatic way back** if a deploy goes wrong.

### 0.3 Deployment strategies — where "capture-then-rollback" fits in the bigger picture

Interviewers love asking "how would you make this zero-downtime" or "what's the difference between this and blue-green." Know the landscape:

| Strategy | How it works | Downtime? | Rollback speed | Fraud Shield? |
|---|---|---|---|---|
| **Recreate** | Stop old version, start new version | Yes, briefly | Slow (manual) | — |
| **Rolling update** | Replace instances one at a time behind a load balancer | No (if you have multiple instances) | Medium | Closest match, single-instance version |
| **Blue-Green** | Run two full environments; switch traffic all at once | No | Instant (flip traffic back) | Not this — no second environment |
| **Canary** | Send a small % of traffic to the new version first | No | Fast | Not this — no traffic splitting |

**💡 Where Fraud Shield actually sits:** it's a **single-instance rolling replace with an automatic health-gated rollback** — each service has one running container, so there's a brief moment of unavailability for *that specific service* while it restarts, but the blast radius is contained to just that one service (thanks to `--no-deps`), and if it comes up unhealthy, the script reverts it within seconds using the previous image tag.

**🎤 Say this if asked "how would you make this true zero-downtime":**
> "Right now each service is a single container, so there's a brief restart window — this is a rolling replace, not blue-green. To get real zero-downtime I'd need at least two instances per service behind a load balancer, so I could bring up the new version alongside the old one, health-check it, then shift traffic, and only then stop the old one. That's a meaningful infrastructure jump — it's the natural 'what's next' for this pipeline."

Anticipating that question *before* they ask it is exactly the kind of answer that reads as senior.

### 0.4 Idempotency — the principle behind half your design decisions

**💡 In plain English:** an operation is **idempotent** if doing it once and doing it five times leaves you in the exact same state. `--no-deps` and per-service scoping exist because Compose's *default* behavior is **not** idempotent across an unrelated service if you're not careful — running `docker compose up` for one service can, by design, also touch things it depends on.

This one concept explains Decision 5 (`--no-deps`), Bug 3 (shared `IMAGE_TAG`), and is a fantastic word to drop into an interview answer because it signals you understand *why*, not just *what*.

### 0.5 Immutable tags vs. moving tags — why you can't trust `latest`

A tag like `latest` is a **pointer**, not a fixed thing — it gets reassigned to a new image every build. A tag like the git SHA (`a3f9c21`) is **immutable** — it will always point at that exact image, forever.

**💡 Why this matters for rollback:** if you only ever deployed with `latest` and something breaks, you have no idea what the *previous* `latest` actually was — it's already been overwritten. That's exactly why the deploy script's first move, before touching anything, is to `docker inspect` the **currently running** container and record its real SHA tag. It doesn't trust "latest" to mean anything — it captures ground truth.

---

<a name="part-1"></a>
## Part 1: What This Pipeline Actually Does

**💡 The one-paragraph version, in plain English:**
You have 9 backend services. You push code, click one button. GitHub looks at what actually changed, builds only those services (using a cache so it's fast), pushes the new images to a free registry, then deploys *only those services* to your own machine, checks each one is actually healthy (not just "running"), and automatically puts the old version back if it isn't. One click, zero-cost, self-healing.

### The problem this solves

**🍽️ Restaurant analogy:** Imagine a restaurant where, every time one dish's recipe changes, the *entire kitchen* stops, every dish gets remade from scratch, and every table gets a new plate — even the tables who ordered something that didn't change. That's a naive pipeline: touch one service, rebuild and redeploy all 9. It's slow, wastes free CI minutes, and every unrelated service gets restarted for no reason — a bigger blast radius than the change deserves.

Fraud Shield's pipeline is the version where only the dish that actually changed gets remade, and only that table's plate gets swapped.

### The two halves

| Half | Runs where | Cost | Why split this way |
|---|---|---|---|
| **CI** — detect + build | GitHub-hosted runner (`ubuntu-latest`) | Free (public repo) | Ephemeral, disposable — perfect for "spin up, build, throw away" |
| **CD** — deploy + health check | Your own machine (self-hosted runner) | Free (your hardware) | Needs to reach your local Docker daemon and actually serve the app |

**🧠 Why interviewers ask "why split it this way":** they're checking if you understand that GitHub-hosted runners are stateless VMs that vanish after each job — they *can't* be where your live services run, because there's nothing persistent to run them on. The self-hosted runner is the only piece with a persistent environment, so deployment has to happen there.

---

<a name="part-2"></a>
## Part 2: The Complete Flow — One Diagram, One Analogy

This is the mental model to have loaded before "walk me through your pipeline."

```
 1. YOU                          2. DETECT (github-hosted, free)
 push code                       diff HEAD vs "last-ci-build" tag
 click "Run workflow"    ───▶    outputs: which services changed
                                          │
                                          ▼
 4. DEPLOY (self-hosted,        3. BUILD (github-hosted, free)
    your PC)                    docker buildx bake -f docker-bake.hcl
 pull + up --no-deps    ◀───    GHA cache, push :latest + :SHA to GHCR
 (changed services only)
 health check, retries
 auto-rollback if unhealthy
```

**One manual trigger. Four stages. No second button.** Jobs inside a single GitHub Actions workflow run can read each other's outputs directly (`needs.detect.outputs.services`), which is why "deploy" can just be the third job in the same run — no artifacts to pass between separate workflows, no extra plumbing.

### 🍽️ The restaurant analogy, mapped to each stage

| Stage | Restaurant equivalent |
|---|---|
| You push + click Run | An order ticket comes in |
| Detect | The expo checks: which dishes on this ticket actually need remaking? |
| Build | The kitchen cooks *only* those dishes (using prepped ingredients — the cache) |
| Deploy | The dish is plated and sent to the table |
| Health check | Someone tastes it before it leaves the pass |
| Rollback | If it's bad, the previous good plate goes out instead — the whole restaurant doesn't stop |

### Step-by-step, precisely

1. **You** push code, then manually click **Run workflow** on `docker-ci.yml`.
2. **DETECT** — diffs against the `last-ci-build` git tag, outputs the changed-service list.
3. **BUILD** — `docker buildx bake -f docker-bake.hcl`, only the changed targets, cached via GitHub Actions cache, pushes `latest` + git-SHA tags to GHCR (free, public).
4. **DEPLOY** — a job in the *same* workflow run calls `deploy.yml` (a reusable workflow) on your self-hosted runner: pulls and starts *only* the changed services (`--no-deps`), health-checks each one, auto-rolls-back any that fail.

---

<a name="part-3"></a>
## Part 3: Key Design Decisions — And the Follow-Up Questions They Invite

For each decision: the plain-language reasoning, then the follow-up question a sharp interviewer will ask next — with the answer.

### Decision 1 — Git tag as the change-detection baseline

**💡 Plain English:** Since you trigger this manually instead of on every push, several commits can pile up between runs. Diffing against "the previous commit" would only catch the *last* commit, missing everything before it. A tag (`last-ci-build`) that only moves forward after a *successful* build always reflects "what did we actually last build," not "what was last committed."

**🔍 Likely follow-up: "What happens if the tag-push step itself fails?"**
> "Then `last-ci-build` stays where it was, so the *next* run's diff will correctly include everything from the failed run too — nothing gets silently skipped. The failure mode is 'diff against an older baseline,' which just means a slightly bigger changed-service list next time, not a missed change. That's a safe failure mode, not a silent one."

### Decision 2 — Two tags per build, not one

**💡 Plain English:** `latest` is convenient but disposable — it gets overwritten every build. The git-SHA tag is permanent. Semantic version tags (`v1.0.0`) are deliberately *not* automatic — a version bump is a human decision about what to call a release, not something that should happen on every commit.

### Decision 3 — Buildx Bake over `docker compose build`

**💡 Plain English:** Bake is declarative, per-target build configuration with *native* GitHub Actions cache integration (`type=gha`). Each service gets its own cache **scope**, so one service's cache writes can never evict another's layers.

**🔍 Likely follow-up: "What is Buildx, actually, under the hood?"**
> "Buildx is Docker's CLI plugin that runs builds through BuildKit instead of the old legacy builder — it supports multi-platform builds, better parallelism, and pluggable cache backends. Bake sits one level above that: instead of running one `docker build` command per service by hand, I define every service as a 'target' in one HCL file, and one `bake` command builds all the targets that need building, in parallel, sharing the same cache backend configuration."

### Decision 4 — Reusable workflow (`workflow_call`), not a duplicated file

**💡 Plain English:** `deploy.yml` is called by `docker-ci.yml` today, and will be called by a future `rollback.yml` with a different, specific tag. One file defines **how** to deploy; callers only decide **what** to deploy. No copy-pasted deploy logic living in two places to go out of sync.

### Decision 5 — `--no-deps` everywhere in the deploy script

**💡 Plain English:** Without it, Compose also evaluates the target service's `depends_on` entries — which, combined with one shared `IMAGE_TAG` variable, makes Compose try to "update" services that were never actually rebuilt. `--no-deps` scopes the command to touch *exactly*, and only, the service it's told to.

### Decision 6 — Capture-then-rollback, not blind redeploy

**💡 Plain English:** Before touching anything, the script records each service's **currently running** tag. If the new version fails its health check, it redeploys through Compose itself with `IMAGE_TAG` set back to that captured value — reconstructing the *full* correct service definition (ports, env vars, network, healthcheck), not a bare container missing all of that configuration.

**🔍 Likely follow-up: "Why not just `docker run` the old image directly for a faster rollback?"**
> "Because a bare `docker run` wouldn't recreate the networking, environment variables, or healthcheck config that Compose manages — I'd be trading a few seconds of speed for a container that's silently missing configuration. Going back through Compose costs a little more time but guarantees the rolled-back container is configured exactly the same way the original working one was."

---

<a name="part-4"></a>
## Part 4: The Real Code — Explained Line by Line

### `docker-bake.hcl` — the tagging function

```hcl
function "tags" {
  params = [service]
  result = [
    "${REGISTRY}/${service}:latest",
    "${REGISTRY}/${service}:${GIT_SHA}",
  ]
}

target "transaction-service" {
  context    = "."
  dockerfile = "transaction-service/Dockerfile"
  tags       = tags("transaction-service")
  cache-from = ["type=gha,scope=transaction-service"]
  cache-to   = ["type=gha,mode=max,scope=transaction-service"]
}
```

**Line by line, in plain English:**
- `function "tags"` — a reusable function, like in any programming language. Instead of writing both tags out by hand for all 9 services (18 lines of repetition), one function generates them from a service name.
- `params = [service]` — the function takes one input: the service's name as a string.
- `result = [...]` — returns a list with two strings: the `latest` tag and the SHA tag, both pointing at the same registry path.
- `target "transaction-service"` — this is one buildable unit — think of it like one `docker build` command's worth of settings, but declared instead of typed on a command line.
- `context = "."` — build context is the repo root (so the Dockerfile can `COPY` files from anywhere in the repo, e.g. a shared parent `pom.xml`).
- `dockerfile = "transaction-service/Dockerfile"` — but the actual Dockerfile used lives inside that service's own folder.
- `cache-from` / `cache-to` — read from and write to GitHub's cache backend, **scoped** to `transaction-service` specifically, so this service's cache and, say, `user-service`'s cache never collide or evict each other.

**💡 What you'd change to add a 10th service:** copy the `target` block, rename it, point `dockerfile` at the new folder. The `tags()` function needs zero changes — that's the entire point of writing it as a function.

### `detect-services.sh` — the diff logic

```bash
if ! git rev-parse last-ci-build >/dev/null 2>&1; then
  echo "$ALL_SERVICES"   # first run - no baseline yet, build everything
  exit 0
fi

CHANGED_FILES=$(git diff --name-only last-ci-build..HEAD)

if echo "$CHANGED_FILES" | grep -qx "pom.xml"; then
  CHANGED_SERVICES="$BACKEND_SERVICES"   # parent POM -> everyone affected
fi

for svc in $ALL_SERVICES; do
  if echo "$CHANGED_FILES" | grep -q "^${svc}/"; then
    CHANGED_SERVICES="$CHANGED_SERVICES $svc"
  fi
done
```

**Line by line, in plain English:**
- `git rev-parse last-ci-build` — "does this tag exist at all?" `git rev-parse` resolves a name to a commit hash; it fails (non-zero exit) if the tag doesn't exist yet.
- If it doesn't exist → this must be the very first run ever, so there's no baseline to diff against — build everything, exit early.
- `git diff --name-only last-ci-build..HEAD` — list every file path that's different between the last successful build and right now. `--name-only` means "just the filenames, not the actual diff content."
- The `pom.xml` safety net — Maven's parent POM affects every backend service's dependency versions; if it changed, you can't safely assume only the services that *look* untouched are actually unaffected, so treat it as "everyone's affected."
- The final loop — for every known service, check if any changed file path starts with that service's folder name (`^${svc}/` is a regex anchor meaning "starts with"). If so, add it to the changed list.

**💡 A subtle but important detail:** this is a **two-tier safety net**, not one check. Tier 1: pipeline/shared-config changes rebuild everything. Tier 2: per-service folder changes rebuild just that service. Good change-detection logic is defined by these safety nets as much as by the "happy path" diffing.

### `deploy-services.ps1` — capture, deploy, verify, rollback

```powershell
# Step 1: capture what's running BEFORE touching anything
$currentImage = docker inspect --format '{{.Config.Image}}' $containerName
if ($LASTEXITCODE -eq 0 -and $currentImage) {
    $PreviousTags[$svc] = $currentImage.Substring($currentImage.LastIndexOf(':') + 1)
}

# Step 2-3: pull + start ONLY this service, nothing it depends on
docker compose -f $ComposeFile up -d --no-deps --remove-orphans $ServiceList

# Step 4: health check with retries
for ($i = 1; $i -le $MaxRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5
        if ($response.StatusCode -eq 200) { $healthy = $true; break }
    } catch { }
    Start-Sleep -Seconds $RetryDelaySeconds
}

# Step 5: rollback through Compose itself - not a bare docker run
if (-not $healthy) {
    $env:IMAGE_TAG = $PreviousTags[$svc]
    docker compose -f $ComposeFile up -d --no-deps $svc
}
```

**Line by line, in plain English:**
- `docker inspect --format '{{.Config.Image}}' $containerName` — ask Docker "what image is this container actually running right now," in a specific format string instead of the full JSON dump. This is the "capture ground truth" step from Decision 6.
- `.Substring($currentImage.LastIndexOf(':') + 1)` — image strings look like `ghcr.io/you/service:a3f9c21`; this just grabs everything after the last colon — the tag itself.
- `up -d --no-deps --remove-orphans` — `-d` = detached (don't block the terminal), `--no-deps` = don't touch dependencies (Decision 5), `--remove-orphans` = clean up any containers Compose no longer recognizes from an old config.
- The retry loop — `try { } catch { }` swallows connection errors (the app might not be listening yet at all in the first second or two), and simply waits and tries again up to `$MaxRetries` times. This is a **polling with backoff** pattern — extremely common in deployment scripts, and worth recognizing by name.
- `Invoke-WebRequest ... /actuator/health` — this hits Spring Boot Actuator's health endpoint, which actually exercises the app's internal logic (DB connectivity, etc.), not just "is the process alive." See Part 10.3 for why that distinction matters.
- The rollback — sets the environment variable Compose reads for image tag back to the captured value, then runs `up` again through Compose (not `docker run`), which is exactly Decision 6's reasoning: reconstruct full configuration, not a bare container.

---

<a name="part-5"></a>
## Part 5: Real Bugs You Solved — Root Cause, Not Just Symptom

For each bug: symptom → root cause → fix → **the general lesson**, so you can talk about the *pattern*, not just memorize the specific incident.

### Bug 1 — Buildx silently merged `docker-compose.yml` into the build

**Symptom:** build failed with `env file api-gateway/.env not found`, even though the build step never touches `.env` files.

**Root cause:** `docker buildx bake` with no `-f` flag **auto-discovers every bake-compatible file** in the directory — and a Compose file is a valid Bake source too. It silently merged both files and fully validated `docker-compose.yml`'s `env_file:` paths — files that only get created later, at deploy time, not build time.

**Fix:** `docker buildx bake -f docker-bake.hcl --push` — explicitly restrict Buildx to exactly one file.

**🧠 General lesson:** *tools with "smart" auto-discovery will surprise you the moment your repo has more than one file matching their discovery pattern.* Whenever a tool supports auto-detection, ask "what else in this repo could it accidentally also pick up?" — that question would have caught this before it ever became a bug.

### Bug 2 — A native command's stderr crashed an intentionally-safe check

**Symptom:** the very first deployment (correctly — every service had no prior container yet) crashed on the first service, instead of logging "first deploy" and continuing.

**Root cause:** Windows PowerShell 5.1 can wrap a **native command's** stderr output into a *terminating* error when `$ErrorActionPreference` is `'Stop'` — regardless of `2>$null` redirection. The `if`-check meant to handle "container doesn't exist yet" never ran, because PowerShell threw before reaching it.

**Fix:** locally scope `$ErrorActionPreference = 'SilentlyContinue'` around just that one expected-to-sometimes-fail command, with `try/catch` as a backup.

**🧠 General lesson:** *"expected failures" need to be handled where they're expected to fail, not assumed to be swallowed by redirection.* Native commands (as opposed to PowerShell cmdlets) don't always respect PowerShell's usual error-handling assumptions — this is a genuinely obscure, very Windows-specific gotcha, and naming it precisely is a strong signal you actually debugged this yourself rather than reading about it.

### Bug 3 — One shared `IMAGE_TAG` variable leaked into services that didn't change

**Symptom:** deploying only `user-service` also tried (and failed) to pull a nonexistent tag for `eureka-server`.

**Root cause:** `docker-compose.deploy.yml` uses **one** `IMAGE_TAG` variable for all 9 services. Compose's default `up` behavior also evaluates the named service's `depends_on` entries — and since they share that same variable, Compose saw `eureka-server`'s "desired" image pointing at a SHA that was only ever built for `user-service`, and tried to pull it.

**Fix:** `docker compose up -d --no-deps`, which stops Compose from even *looking at* dependencies.

**🎤 Say this almost verbatim — it's your strongest bug story:**
> "The most interesting bug was a shared-variable issue: my `docker-compose.deploy.yml` uses one `IMAGE_TAG` for all 9 services, and Compose's default behavior evaluates a service's dependencies even when you only name one service to update. That combination meant updating one service could try to pull a tag that never existed for a completely unrelated service. The fix was `--no-deps`, which scopes Compose to touch exactly the service you name. It taught me that 'only pass the service you want' isn't enough on its own — you have to also tell the tool not to reason about everything connected to it."

**🔍 Likely follow-up: "Why not just use a separate variable per service instead?"**
> "That would also fix it, and it's arguably more explicit. I went with `--no-deps` because it's a one-line fix to the actual root cause — Compose reasoning about dependencies I don't want it to touch — rather than working around it by proliferating variables. Nine `IMAGE_TAG_<SERVICE>` variables would work, but it's more to maintain and easier to get out of sync than one flag that says exactly what I mean: 'just this service.'"

---

<a name="part-6"></a>
## Part 6: Configuration Walkthrough — "How Would You Set This Up?"

This is the part your original guide didn't have. If an interviewer says **"walk me through configuring this CI/CD pipeline into a project from scratch,"** this is your answer — a real, ordered setup process, not just a description of the finished result.

### Prerequisites

- A GitHub repo (public, if you want free Buildx cache + free GHCR storage/bandwidth).
- Docker Desktop (or Docker Engine + the Buildx plugin) on the machine that will run deploys.
- A machine to act as the self-hosted runner — can be the same machine that runs the services.
- Each service already has its own `Dockerfile` and exposes a health endpoint (e.g. Spring Boot Actuator's `/actuator/health`).

### Step 1 — Decide the repo layout

```
repo-root/
├── pom.xml                     # shared parent POM (if Maven multi-module)
├── docker-bake.hcl
├── docker-compose.deploy.yml
├── scripts/
│   ├── detect-services.sh
│   └── deploy-services.ps1
├── .github/workflows/
│   ├── docker-ci.yml
│   └── deploy.yml
├── transaction-service/
│   ├── Dockerfile
│   └── ...
├── user-service/
│   ├── Dockerfile
│   └── ...
└── ... (7 more services)
```

**💡 Why this matters:** the detect script's regex (`^${svc}/`) *depends* on each service living in a top-level folder named exactly like the service. Get this layout right first — everything downstream assumes it.

### Step 2 — Enable registry permissions (GHCR)

GHCR authenticates using the built-in `GITHUB_TOKEN` — no separate account or PAT needed for same-repo pushes. But the workflow needs explicit permission:

```yaml
permissions:
  contents: read
  packages: write   # <-- required to push images to GHCR
```

**🔍 Common pitfall:** forgetting `packages: write` gives a `403 Forbidden` on push that has nothing to do with your Docker config — it's a GitHub Actions permissions issue, not a Docker one. Also check repo-level default: **Settings → Actions → General → Workflow permissions** must allow read/write, not just read.

### Step 3 — Write `docker-bake.hcl`

Start with the `tags()` function (Part 4), then add one `target` block per service. Define shared variables at the top:

```hcl
variable "REGISTRY" { default = "ghcr.io/your-username/your-repo" }
variable "GIT_SHA"   { default = "local" }
```

`GIT_SHA` gets overridden at build time from CI (`--set *.args.GIT_SHA=$(git rev-parse HEAD)` or as an env var Bake reads), so locally it just falls back to `"local"` — you can still `docker buildx bake` on your own machine without CI involved.

### Step 4 — Write `detect-services.sh`

Copy the logic from Part 4. Two things to configure for *your* project specifically:
- `$ALL_SERVICES` — a space-separated list of every service folder name.
- `$BACKEND_SERVICES` — usually the same list, unless some services (e.g. a frontend) don't share the parent POM.

### Step 5 — Set up the `last-ci-build` tag mechanism

This isn't a one-time setup step — it's a step *inside the workflow*, run only after a successful build:

```yaml
- name: Move last-ci-build tag
  run: |
    git tag -f last-ci-build
    git push origin last-ci-build --force
```

**🔍 Why `-f` (force)?** because the tag needs to *move* forward each time, not be created fresh — a normal `git tag` would fail on the second run with "tag already exists."

### Step 6 — Register the self-hosted runner

1. On GitHub: **Settings → Actions → Runners → New self-hosted runner.**
2. GitHub gives you a `config.cmd` (Windows) or `config.sh` (Linux/macOS) command with a one-time registration token.
3. Run it on your machine, give it a label (e.g. `self-hosted, fraudshield-deploy`) so your workflow can target it specifically.
4. Install it as a **service** so it survives reboots and keeps listening for jobs even when you're not logged in.

**🔐 Security note worth knowing (and worth raising unprompted if asked "any concerns with self-hosted runners"):**
> "Self-hosted runners on a public repo carry real risk — anyone who opens a pull request could potentially get code execution on that machine if a workflow runs against their fork automatically. GitHub's default protection requires manual approval before a first-time contributor's workflow runs on a self-hosted runner, and I'd never disable that default for this kind of setup."

### Step 7 — Write `docker-ci.yml` (the orchestrator)

```yaml
name: docker-ci
on: workflow_dispatch   # manual trigger only

jobs:
  detect:
    runs-on: ubuntu-latest
    outputs:
      services: ${{ steps.diff.outputs.services }}
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 0 }   # full history needed for tag diffing
      - id: diff
        run: ./scripts/detect-services.sh

  build:
    needs: detect
    runs-on: ubuntu-latest
    permissions: { packages: write, contents: read }
    steps:
      - uses: actions/checkout@v4
      - uses: docker/setup-buildx-action@v3
      - run: docker buildx bake -f docker-bake.hcl --push
        env:
          GIT_SHA: ${{ github.sha }}

  deploy:
    needs: [detect, build]
    uses: ./.github/workflows/deploy.yml
    with:
      services: ${{ needs.detect.outputs.services }}
    secrets: inherit
```

**Line by line, plain English:**
- `on: workflow_dispatch` — this is what makes the trigger manual: a "Run workflow" button appears in the Actions tab instead of firing automatically on push.
- `needs: detect` — the `build` job won't start until `detect` finishes, and can read its outputs.
- `fetch-depth: 0` — by default, GitHub Actions does a *shallow* checkout (just the latest commit) for speed; diffing against a tag needs full history, so this is a required, easy-to-forget setting.
- `deploy: uses: ./.github/workflows/deploy.yml` — this is the reusable-workflow call from Decision 4; `with:` passes inputs, `secrets: inherit` passes the calling workflow's secrets down without re-declaring them.

### Step 8 — Write `deploy.yml` (the reusable workflow)

```yaml
name: deploy
on:
  workflow_call:
    inputs:
      services:
        required: true
        type: string

jobs:
  deploy:
    runs-on: [self-hosted, fraudshield-deploy]
    steps:
      - uses: actions/checkout@v4
      - name: Run deploy script
        shell: pwsh
        run: ./scripts/deploy-services.ps1 -Services "${{ inputs.services }}"
```

**💡 Why `workflow_call` instead of `workflow_dispatch` here?** `workflow_dispatch` is for a *human* clicking a button. `workflow_call` is for *another workflow* invoking this one programmatically — that's the mechanism that makes this file reusable by both `docker-ci.yml` today and a future `rollback.yml`.

### Step 9 — Write `docker-compose.deploy.yml`

```yaml
services:
  transaction-service:
    image: ${REGISTRY}/transaction-service:${IMAGE_TAG}
    ports: ["8081:8080"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    depends_on: [eureka-server]
  # ... 8 more services, same IMAGE_TAG variable
```

**🔍 This is the exact configuration choice behind Bug 3** — one `IMAGE_TAG` variable, reused across every service, is what makes `--no-deps` non-optional. Configure it this way *deliberately*, understanding the tradeoff, not by accident.

### Step 10 — Write `deploy-services.ps1`

Build it in the order it needs to run, matching Part 4's walkthrough: capture current tag → `docker compose up -d --no-deps` → poll health endpoint with retries → on failure, reset `$env:IMAGE_TAG` and redeploy the previous tag through Compose.

### Step 11 — Test end to end

1. Push a small change to exactly one service.
2. Manually trigger the workflow.
3. Confirm in the Actions log: `detect` lists only that one service.
4. Confirm in GHCR (repo → Packages): a new image with both `latest` and a SHA tag.
5. Confirm on your machine: `docker ps` shows only that one container restarted, others untouched.

### Step 12 — Deliberately test the rollback path (do this before your interview)

Your original guide flagged this honestly: rollback is "built — not yet deliberately tested." Fix that before an interview if you can — it upgrades your strongest answer from theoretical to proven.

**How to force it:** temporarily point one service's healthcheck at a path that 404s (or add a `sleep 999` before the app binds its port), push, trigger a deploy, and confirm you actually watch the script capture the old tag, detect the failed health check, and redeploy the previous version — reading it from the Actions log, live.

**🎤 Once you've done this, upgrade your answer to:**
> "I've verified this rollback path directly — I intentionally broke a health check and watched the script capture the previous tag, detect the failure after the retry window, and redeploy through Compose with the old tag. It correctly restored the working version without me touching anything."

### Common configuration pitfalls (a checklist)

| Symptom | Root cause | Fix |
|---|---|---|
| `403` pushing to GHCR | Missing `packages: write` permission | Add to workflow `permissions:` block |
| Detect script always says "build everything" | Shallow checkout (`fetch-depth` not set to `0`) | Full checkout needed for tag diffing |
| Bake pulls in unrelated `.env` errors | No `-f` flag, auto-discovered `docker-compose.yml` too | Always pass `-f docker-bake.hcl` explicitly |
| First deploy crashes on "container not found" | PowerShell native-command stderr handling (Bug 2) | Scope `$ErrorActionPreference` locally, add `try/catch` |
| Updating one service tries to pull a tag for another | Shared `IMAGE_TAG` + Compose's default dependency evaluation (Bug 3) | `--no-deps` on every deploy command |
| Runner never picks up jobs | Runner registered but not running as a service, or wrong label in `runs-on:` | Confirm service is running; match labels exactly |

---

<a name="part-7"></a>
## Part 7: Senior Interview Q&A

*(All of your original Q&A, kept — plus new ones added at the end.)*

**Q: How does changed-service detection actually work?**
> "A lightweight git tag, `last-ci-build`, marks the last commit that was successfully built. Every run diffs HEAD against that tag, maps changed file paths to service folders, and after a successful build, moves the tag forward to HEAD. Since my pipeline is manually triggered rather than running on every push, this is more correct than diffing against the previous commit — several commits can pile up between manual runs, and the tag-based baseline always reflects what was actually last built, not just what was last committed."

**Q: Why tag with both 'latest' and the git SHA?**
> "`latest` is convenient for routine deploys, but it's a moving target — you can't look at a running container and know exactly what commit it's running. The SHA tag is permanent and traceable: I capture it before touching a container specifically so I can roll back to that exact previous build if the new one fails health checks."

**Q: Why a self-hosted runner instead of a cloud deploy target?**
> "Cost. This is a portfolio project, and a self-hosted runner on my own machine gives me a completely real, working push-to-deploy loop at zero cloud spend. The mechanics — pulling versioned images, health-checking, rolling back — are identical to what I'd do against a cloud target; only the compute location changes."

**Q: Walk me through what happens if a deploy fails.**
> "Before pulling anything new, the script captures the currently running image tag for each service being deployed. It pulls and starts the new version scoped with `--no-deps`, then polls each service's health endpoint with retries. If a service never becomes healthy, the script sets `IMAGE_TAG` back to the captured previous tag and runs `docker compose up` again for just that service — which reconstructs the full correct configuration from the compose file, not just a bare container. The workflow still fails loudly even after a successful rollback, so a silent partial failure never looks like success."

**Q: What's the one manual step left in this pipeline?**
> "Triggering it. I chose manual dispatch deliberately — I have other workflows in this repo that already own the push trigger, and I didn't want to compete with them. Everything after that click is fully automatic: detect, build, push, deploy, health check, rollback if needed."

**Q: Is this a complete CI/CD pipeline?**
> "In the practical sense, yes — automated build, automated delivery to a registry, and automated, health-checked, rollback-capable deployment, all from one trigger. What's intentionally not built is manual version-controlled rollback and semantic versioning releases — those are deliberate human checkpoints, not gaps I forgot. I also haven't yet deliberately tested the automatic rollback path by forcing a real failure, which I'd want to do before calling the safety net fully proven." *(— or, if you've done Part 6 Step 12: "I have since deliberately tested it.")*

### New questions (not in your original guide)

**Q: What's the single biggest weakness or risk in this pipeline right now?**
> "The `last-ci-build` tag is effectively a single point of truth for change detection. If that tag-push step ever silently failed while the rest of the run succeeded, the next run's baseline would be stale — not catastrophically, since it'd just rebuild a superset of what actually changed, but it's a piece of hidden state I'd want to make more visible, maybe by logging the tag's commit hash explicitly at both the start and end of every run so a mismatch is obvious in the logs."

**Q: How would you add a canary or blue-green style rollout to this?**
> "That needs at least two running instances per service behind something that can shift traffic — a reverse proxy or load balancer. I'd deploy the new version alongside the old one under a different container name, health-check it in isolation, then flip the proxy's upstream, and only then tear down the old container. It's a meaningful step up in infrastructure from a single-container-per-service model, but the health-check-then-decide logic I already have is the right foundation for it."

**Q: How do you handle secrets in this pipeline?**
> "GHCR authentication uses the built-in `GITHUB_TOKEN`, scoped automatically to the repo, so there's no long-lived credential to manage for pushes. Anything the self-hosted runner needs — database credentials for the services it deploys, for example — lives in GitHub Actions repository secrets, injected as environment variables at deploy time, never committed to the repo or baked into an image layer."

**Q: What would you monitor or alert on if this were a real production pipeline?**
> "Right now, a failed health check triggers rollback, but the *notification* is just the workflow going red in GitHub's UI. In a real setting I'd add a Slack or email notification specifically on rollback events — since a rollback means something briefly broke — plus basic deployment frequency and rollback-rate metrics over time, since a rising rollback rate is an early signal of declining code quality before it becomes an incident."

**Q: What if two people trigger a deploy at the same time?**
> "That's a real gap today — nothing currently prevents two concurrent runs from racing on the same `last-ci-build` tag or the same containers. GitHub Actions has a `concurrency:` key I'd add to the workflow to queue or cancel overlapping runs, so only one deploy is ever in flight at a time."

---

<a name="part-8"></a>
## Part 8: What Is Deliberately Not Built Yet

Naming what you *haven't* built, and why, is a mark of seniority — it shows you understand the full picture rather than pretending everything is finished.

| Not built | What it would add | Why it was cut for now |
|---|---|---|
| `release.yml` + `version.sh` | Human-triggered semantic version tags (`v1.0.0`) | A version bump is a deliberate decision, not something that should be automatic |
| `rollback.yml` + `rollback.ps1` | Pick a service + a past version, redeploy on demand | Automatic rollback-on-failure already covers the main safety net |
| `cleanup.yml` | Weekly prune of old images/cache/volumes | Disk usage isn't yet a real problem worth automating away |

**None of these are embarrassing gaps** — they're correct, deliberate scoping for a portfolio project under a real job-search timeline. If asked "what would you add next," say this plainly — it demonstrates judgment about priorities, which matters more than having built literally everything.

---

<a name="part-9"></a>
## Part 9: 90-Second Pitch + Resume Bullets

### Your 90-Second Pitch

> "I built a zero-cost, changed-service-only CI/CD pipeline for a 9-service microservices project. On a manual trigger, GitHub Actions diffs the repo against a moving baseline tag to figure out exactly which services changed, builds only those with Docker Buildx — layer-cached through GitHub's free cache backend — and tags each image with both `latest` and its exact commit SHA for traceability. As soon as the build succeeds, the same workflow run calls a reusable deploy workflow that runs on a self-hosted runner on my own machine: it pulls and restarts only the changed services, scoped with `--no-deps` so it never touches anything unrelated, health-checks each one with retries, and automatically rolls back to the previously running image if anything fails to come up healthy. The whole thing — build, push, deploy, verify, rollback — runs from one click, entirely on GitHub's free tier and my own hardware, no cloud spend at all."

### Resume Bullets

- Built a changed-service-detection CI pipeline (GitHub Actions + Docker Buildx Bake) for a 9-service microservices project, using git-diff analysis against a moving baseline tag to build and push only services that actually changed.
- Implemented per-service GitHub Actions layer caching (scoped `cache-from`/`cache-to`), reducing redundant dependency downloads across a multi-module Maven build.
- Designed a reusable GitHub Actions workflow (`workflow_call`) for deployment, shared between the primary CI/CD chain and (planned) manual rollback tooling.
- Built a PowerShell deployment script with per-service targeting (`--no-deps` scoping), automated health checks with retry logic, and automatic rollback to the previously running image tag on failed health checks.
- Diagnosed and fixed three distinct real-world CI/CD bugs: Docker Buildx's default multi-file discovery merging unintended build sources, a Windows PowerShell native-command error-handling edge case, and a shared-variable Compose dependency bug.

**Pick 3–4, not all five** — too many CI/CD bullets on a backend-focused resume can crowd out the application work itself. The pipeline supports the story; it shouldn't become the whole story.

---

<a name="part-10"></a>
## Part 10: Bonus — General CI/CD Questions Beyond This Project

Interviewers often test general CI/CD understanding separately from your specific project, to check the knowledge generalizes. These are common, and your Fraud Shield experience gives you real examples for every one.

**Q: What's the difference between a build artifact and a container image?**
> A build artifact is the compiled output of your code — a `.jar`, a `.war`, a binary. A container image packages that artifact *together with* an OS layer, runtime, and filesystem needed to run it anywhere consistently. In your pipeline: the Maven build inside the Dockerfile produces the `.jar` artifact; Buildx packages it into the image that actually gets pushed and deployed.

**Q: What does "shift left" mean in a CI/CD context?**
> Catching problems as early as possible in the pipeline — ideally at commit or PR time — instead of discovering them in production. Automated tests in CI are the classic example: a broken build fails in minutes, not after a customer hits it.

**Q: What's the difference between a liveness check and a readiness check?** *(Kubernetes terminology, but the concept applies everywhere, including your Actuator health check)*
> A **liveness** check asks "is this process still alive, or should it be restarted?" A **readiness** check asks "is this instance ready to actually receive traffic right now?" A service can be alive but not ready (e.g., still connecting to its database on startup). Your deploy script's `/actuator/health` poll is functioning as a readiness check — it's gating *when the deploy is considered successful*, not just whether the process exists.

**Q: What's a "cache hit rate," and why would a pipeline's build time suddenly get slower?**
> The percentage of build steps that could reuse a cached layer instead of rebuilding from scratch. Build times spike when cache scopes are misconfigured (one service's writes evicting another's, per Decision 3), when a frequently-changed file sits early in a Dockerfile (invalidating every layer after it), or when the cache backend itself hits a size limit and starts evicting old entries.

**Q: What's GitOps, and does this pipeline use it?**
> GitOps means the desired state of your infrastructure/deployment lives declaratively in a git repo, and an automated process reconciles reality to match it — git is the single source of truth, not a person running commands by hand. This pipeline is *adjacent* to GitOps in spirit (declarative Compose files, git-tag-based state tracking) but isn't strictly GitOps, since there's no continuous reconciliation loop — it's trigger-based, not watching-and-syncing.

**Q: Why do people avoid running tests and builds directly on the deploy target machine?**
> Contamination risk (a bad build script could leave stray files or processes on a machine serving real traffic) and lack of reproducibility (a "clean" ephemeral GitHub-hosted runner guarantees the same starting conditions every time; a long-lived machine accumulates drift). This is exactly why the *build* half of this pipeline runs on a disposable GitHub-hosted runner, and only the *deploy* half — which genuinely needs a persistent target — runs on the self-hosted machine.

---

<a name="part-11"></a>
## Part 11: Glossary — Every Term, Defined Simply

| Term | Plain-English definition |
|---|---|
| **CI (Continuous Integration)** | Automatically build and test code every time it changes |
| **Continuous Delivery** | Code is always release-ready; a human still clicks "deploy" |
| **Continuous Deployment** | Every passing change deploys itself, no human click |
| **Idempotent** | Doing an operation once or five times leaves the exact same result |
| **Immutable tag** | A tag that always points to the same exact image forever (e.g. a git SHA) |
| **Moving/mutable tag** | A tag that gets reassigned to new images over time (e.g. `latest`) |
| **Buildx** | Docker's CLI plugin for running builds through BuildKit — multi-platform, better caching |
| **Bake** | A Buildx feature for declaring many build "targets" in one file, built together |
| **Cache scope** | A named boundary so one thing's cache writes can't evict another's |
| **GHCR** | GitHub Container Registry — free image hosting tied to your GitHub repo |
| **Self-hosted runner** | A machine you own that runs GitHub Actions jobs, as opposed to GitHub's own VMs |
| **`workflow_dispatch`** | A GitHub Actions trigger that adds a manual "Run workflow" button |
| **`workflow_call`** | A trigger that lets *other workflows* invoke this one as a reusable unit |
| **`--no-deps`** | A Compose flag: touch only the named service, ignore its `depends_on` entries |
| **Health check** | An active check that an app is actually working, not just that its process exists |
| **Readiness vs. liveness** | Readiness = "can it serve traffic right now?" Liveness = "is it alive at all?" |
| **Rolling deploy** | Replace instances of a service one at a time |
| **Blue-green deploy** | Run two full environments, switch all traffic over at once |
| **Canary deploy** | Send a small percentage of traffic to the new version first |
| **GitOps** | Git as the single source of truth; automation reconciles reality to match it |
| **SPOF** | Single point of failure — one thing whose failure breaks the whole system |

---

### How to use this before an interview

1. **Read Parts 0–5 once, slowly**, out loud if it helps — the goal is understanding, not memorization.
2. **The night before:** skim Part 2's diagram, Part 6's setup steps, and Part 7's Q&A.
3. **In the interview:** lead with Part 2's diagram if asked to "walk me through it," use Part 9's pitch nearly verbatim if it helps you start strong, and let Part 5's real bugs carry the conversation deeper whenever they ask a follow-up — bugs you personally debugged are the most convincing evidence you actually built this yourself.

You built and personally debugged a real CI/CD pipeline — not a copied tutorial, but one with your own specific bugs, your own specific fixes, and your own specific design decisions you can defend. That's a genuinely complete, verifiable story most candidates at your experience level cannot tell. Good luck.