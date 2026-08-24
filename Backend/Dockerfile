# =============================================================================
# eTour backend - Spring Boot 4 / Java 17
#
# Multi-stage: the Maven toolchain and the whole ~/.m2 cache stay in the build
# stage, so the shipped image contains only a JRE and one jar.
# =============================================================================

# ---- Stage 1: build ---------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy the POM alone first. Docker caches this layer on the POM's checksum, so
# dependencies are re-downloaded only when the POM actually changes - editing a
# .java file reuses the cache and the build takes seconds instead of minutes.
COPY pom.xml .
# Non-fatal on purpose: this is only a cache warm-up. go-offline can trip over
# optional or provided-scope artifacts, and that must not fail the build -
# whatever it misses, `mvn package` below resolves normally.
RUN mvn -B dependency:go-offline \
    || echo "pre-fetch incomplete; the package step will resolve the rest"

COPY src ./src

# Tests need a live MySQL, which does not exist at image-build time.
RUN mvn -B clean package -DskipTests

# ---- Stage 2: runtime -------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

# curl is only here to give compose a real HTTP health check (see HEALTHCHECK
# below and depends_on/service_healthy in docker-compose.yml).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user. If this container is ever compromised, the
# attacker does not land as root.
RUN groupadd --system etour && useradd --system --gid etour --home /app etour

WORKDIR /app

# The mount point for tour media and Excel batches. Created and owned up front,
# because a bind-mounted host directory inherits this ownership only if the
# directory already exists with the right owner.
RUN mkdir -p /app/uploads && chown -R etour:etour /app

COPY --from=build --chown=etour:etour /build/target/*.jar app.jar

USER etour

EXPOSE 8080

# Container memory limits are invisible to the JVM's default heap sizing unless
# it is told to look; MaxRAMPercentage makes the heap track whatever the
# container is actually given.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

# /api/auth/providers is public, cheap, and only answers once the Spring
# context is fully up - so it reports readiness, not just an open port.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
    CMD curl -fsS http://localhost:8080/api/auth/providers || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
