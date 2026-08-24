# =============================================================================
# eTour .NET backend - ASP.NET Core 10
#
# Drop-in alternative to Backend/Dockerfile. It listens on the SAME port (8080)
# and registers under the SAME compose service name, so nginx and the frontend
# bundle need no change when you switch backends.
#
# Multi-stage: the SDK and the NuGet cache stay in the build stage; the runtime
# image carries only the ASP.NET runtime and the published output.
# =============================================================================

# ---- Stage 1: build ---------------------------------------------------------
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /src

# Copy the project files alone first. Docker caches this layer on their
# checksums, so `dotnet restore` is re-run only when a dependency actually
# changes - editing a .cs file reuses the cache.
# The .slnx is deliberately NOT copied: it references tests/eTour.Tests, which
# .dockerignore excludes, so any command that resolved the solution would fail
# looking for a project that is not in the build context.
COPY src/eTour.Domain/eTour.Domain.csproj          src/eTour.Domain/
COPY src/eTour.Application/eTour.Application.csproj src/eTour.Application/
COPY src/eTour.Infrastructure/eTour.Infrastructure.csproj src/eTour.Infrastructure/
COPY src/eTour.Api/eTour.Api.csproj                 src/eTour.Api/

# Restore the API project rather than the solution: the solution also contains
# the test project, whose dependencies the runtime image has no use for.
RUN dotnet restore src/eTour.Api/eTour.Api.csproj

COPY src/ src/

RUN dotnet publish src/eTour.Api/eTour.Api.csproj \
        -c Release \
        -o /app/publish \
        --no-restore \
        /p:UseAppHost=false

# ---- Stage 2: runtime -------------------------------------------------------
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime

# curl gives compose a real HTTP health check (see HEALTHCHECK below and
# depends_on/service_healthy in docker-compose.yml).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Unprivileged user, matching the Java image's posture.
RUN groupadd --system etour && useradd --system --gid etour --home /app etour

WORKDIR /app

# Created and owned up front: a bind-mounted host directory inherits this
# ownership only if the directory already exists with the right owner.
RUN mkdir -p /app/uploads /app/logs && chown -R etour:etour /app

COPY --from=build --chown=etour:etour /app/publish ./

USER etour

EXPOSE 8080

# Bind to all interfaces on 8080. Without this Kestrel would listen on its own
# default port and nothing would reach it from the compose network.
ENV ASPNETCORE_URLS=http://+:8080 \
    ASPNETCORE_ENVIRONMENT=Production \
    DOTNET_RUNNING_IN_CONTAINER=true

# /api/auth/providers is public, cheap, and only answers once the app has fully
# started - so it reports readiness rather than just an open port. It is also
# the endpoint the frontend uses, which makes this a genuine end-to-end check.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fsS http://localhost:8080/api/auth/providers || exit 1

ENTRYPOINT ["dotnet", "eTour.Api.dll"]
