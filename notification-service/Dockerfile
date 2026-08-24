# =============================================================================
# eTour notification service - Spring Boot 3.3 / Java 17
#
# Same multi-stage shape as Backend/Dockerfile: the Maven toolchain and the
# ~/.m2 cache stay in the build stage, so the shipped image is a JRE and one jar.
# =============================================================================

# ---- Stage 1: build ---------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# POM first so Docker caches the dependency layer on its checksum - editing a
# .java file then reuses the cache instead of re-resolving everything.
COPY pom.xml .
RUN mvn -B dependency:go-offline \
    || echo "pre-fetch incomplete; the package step will resolve the rest"

COPY src ./src

# Tests run against in-memory H2 and need nothing external, so unlike the main
# backend image this one CAN run them - the build fails if the retry policy or
# the template rendering regressed.
RUN mvn -B clean package

# ---- Stage 2: runtime -------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

# curl exists purely to give compose a real HTTP health check (see HEALTHCHECK).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Unprivileged user: a compromise here does not land as root.
RUN groupadd --system notify && useradd --system --gid notify --home /app notify

WORKDIR /app
RUN chown -R notify:notify /app

COPY --from=build --chown=notify:notify /build/target/*.jar app.jar

USER notify

EXPOSE 8085

# Container memory limits are invisible to the JVM's default heap sizing unless
# it is told to look.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

# /svc/health is unauthenticated and only answers once the Spring context is
# fully up, so this reports readiness rather than just an open socket.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
    CMD curl -fsS http://localhost:8085/svc/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
