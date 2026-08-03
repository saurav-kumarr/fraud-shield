# =============================================================================
#  report-service - Dockerfile (multi-module Maven aware)
#  IMPORTANT: build with context = REPO ROOT (not this folder), because this
#  service's pom.xml has a <parent> pointing at the root pom.xml one level up.
#  In docker-compose.yml: context: .  /  dockerfile: report-service/Dockerfile
# =============================================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy the ROOT (parent) POM first - shared dependency/plugin versions
COPY pom.xml .

# 2. Copy every module's pom.xml (not source yet) so Maven can read the full
#    reactor and resolve the parent + dependency versions. This layer stays
#    cached even when only one service's source code changes.
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY fraud-detection-service/pom.xml fraud-detection-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml

# -pl = only build THIS module   -am = also build whatever it depends on
RUN mvn -B -pl report-service -am dependency:go-offline

# 3. Now copy the real source for just this module
COPY report-service/src report-service/src
RUN mvn -B -pl report-service -am clean package -DskipTests

# ---- Stage 2: minimal runtime image ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Path includes the module folder, since we built from the reactor root
COPY --from=build /app/report-service/target/*.jar app.jar

ENV JAVA_OPTS="-Duser.timezone=UTC"
EXPOSE 8085
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -q --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]