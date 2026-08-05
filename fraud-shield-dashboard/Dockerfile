# =============================================================================
#  fraud-shield-dashboard - Dockerfile
#  Different shape from the 8 backend Dockerfiles: there's no JVM process to
#  run here. Stage 1 compiles the React app into static files. Stage 2 hands
#  those files out with nginx - tiny, fast, and does double duty as a reverse
#  proxy for API + WebSocket calls (see nginx.conf).
#
#  Build context for this one is JUST this folder (context: ./fraud-shield-
#  dashboard in docker-compose.yml) - no parent-POM problem here, this is a
#  standalone npm project, not part of the Maven reactor.
# =============================================================================

# ---- Stage 1: build the static files ----
FROM node:20-alpine AS build
WORKDIR /app

# Copy only the dependency manifests first - Docker caches this layer, so
# `npm ci` only re-runs when package.json/package-lock.json actually change,
# not on every source code edit (same caching principle as the Maven builds).
COPY package*.json ./
RUN npm ci

# Now copy the rest of the source and build
COPY . .
RUN npm run build
# Vite's default output folder is "dist" - if your vite.config.js sets a
# different build.outDir, update the COPY path in Stage 2 below to match.

# ---- Stage 2: serve the built files with nginx ----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]