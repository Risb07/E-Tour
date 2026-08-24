# Dockerizing and running the .NET backend

Two halves: **how the container was built** (part 1, so you can explain it), and
**how to run it** (part 2, so you can demo it).

The .NET backend is already dockerized — `dotnet-backend/Dockerfile` and the
`backend-dotnet` service exist. Nothing below needs writing again; part 1
explains what each piece does and why.

---

# Part 1 — How the container was built

## Step 1. Understand what has to go in

`dotnet-backend` is a four-project solution:

```
src/eTour.Domain/          entities, enums          (no dependencies)
src/eTour.Application/     DTOs, services, mapping
src/eTour.Infrastructure/  EF Core, JWT, mail, PDF
src/eTour.Api/             controllers, Program.cs  <- the entry point
tests/eTour.Tests/                                  <- NOT shipped
```

Only `eTour.Api` is published; it pulls the other three in as project
references. The test project is deliberately excluded — it would drag NUnit,
Moq and an in-memory database provider into a production image for no reason.

## Step 2. Choose a multi-stage build

A .NET **SDK** image is roughly 800 MB; the **runtime** image is about 220 MB.
Building in one stage and running in another means the compiler, NuGet cache and
source code never reach the shipped image:

```dockerfile
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build      # compiles
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime # runs
```

The tag must be `10.0` because every `.csproj` targets `net10.0`. Using
`8.0` would fail with *"The current .NET SDK does not support targeting
.NET 10.0"*.

## Step 3. Copy the project files before the source

This is the one decision that most affects rebuild speed:

```dockerfile
COPY src/eTour.Domain/eTour.Domain.csproj          src/eTour.Domain/
COPY src/eTour.Application/eTour.Application.csproj src/eTour.Application/
COPY src/eTour.Infrastructure/eTour.Infrastructure.csproj src/eTour.Infrastructure/
COPY src/eTour.Api/eTour.Api.csproj                 src/eTour.Api/
RUN dotnet restore src/eTour.Api/eTour.Api.csproj

COPY src/ src/          # only now the actual code
RUN dotnet publish ...
```

Docker caches each layer against the checksum of what it copied. Because the
`.csproj` files come first, `dotnet restore` re-runs **only when a dependency
changes**. Editing a `.cs` file invalidates only the later layers, so a rebuild
takes seconds instead of re-downloading every NuGet package.

Copying `src/` first and restoring afterwards would work, but every code change
would re-download the entire package graph.

> The `.slnx` is deliberately **not** copied: it references `tests/eTour.Tests`,
> which `.dockerignore` excludes, so any command that resolved the solution
> would fail looking for a project that is not in the build context.

## Step 4. Publish, not build

```dockerfile
RUN dotnet publish src/eTour.Api/eTour.Api.csproj \
        -c Release -o /app/publish --no-restore /p:UseAppHost=false
```

- `publish` produces a self-contained output folder with all dependencies;
  `build` leaves them scattered across `bin/`.
- `-c Release` enables optimisations and strips debug symbols.
- `--no-restore` skips a redundant restore — step 3 already did it.
- `/p:UseAppHost=false` skips generating a native `eTour.Api` executable. The
  container starts it with `dotnet eTour.Api.dll`, so the launcher is dead
  weight.

## Step 5. Harden the runtime stage

```dockerfile
RUN groupadd --system etour && useradd --system --gid etour --home /app etour
RUN mkdir -p /app/uploads /app/logs && chown -R etour:etour /app
COPY --from=build --chown=etour:etour /app/publish ./
USER etour
```

The process runs as an unprivileged user, matching the Java image. The
directories are created and given the right owner **before** anything mounts
over them — a bind-mounted host folder inherits this ownership only if the
directory already exists correctly.

## Step 6. Bind to the right address and port

```dockerfile
ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080
```

This is the single most common .NET-in-Docker mistake. By default Kestrel
listens on `localhost:5000`, which inside a container means *the container's own
loopback* — unreachable from anywhere else, so nginx would get connection
refused. `http://+:8080` binds all interfaces on the port the rest of the stack
expects.

`EXPOSE` is documentation only; it publishes nothing.

## Step 7. Add a health check that means "ready"

```dockerfile
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fsS http://localhost:8080/api/auth/providers || exit 1
```

`/api/auth/providers` is public, cheap, and only answers once the app has fully
started — so it reports readiness rather than just an open TCP port. It is also
the endpoint the frontend actually calls, which makes this a genuine end-to-end
check. `curl` is installed in the runtime stage purely for this.

`start-period=60s` gives EF Core time to connect and the seeder time to run
before a failure counts against the retry budget.

## Step 8. Keep the build context small

`.dockerignore` excludes `bin/`, `obj/`, `.vs/`, `tests/`, logs and uploads.
`bin/` and `obj/` matter most: they hold Windows-built artefacts that will not
run in the Linux build stage, and copying them in can shadow the fresh build.

## Step 9. Wire it into Compose

The service is a sibling of the Java one, distinguished by a profile:

```yaml
backend-dotnet:
  profiles: ["dotnet"]
  build: { context: ./dotnet-backend }
  networks:
    etour:
      aliases: ["backend"]        # <- the whole switching mechanism
  volumes:
    - ./uploads:/app/uploads      # same folder the Java backend uses
```

The **network alias** is what makes the swap invisible: `nginx.conf` proxies to
`backend:8080`, and this container answers to that name whenever its profile is
active. Neither nginx nor the compiled React bundle knows the difference.

Configuration comes from the same `.env` keys as the Java backend, translated to
ASP.NET Core's convention — a double underscore is a section separator, so
`ConnectionStrings__Default` overrides `ConnectionStrings:Default` in
`appsettings.json`.

---

# Part 2 — Running it

## Before anything: which URL are you serving?

Your `etour-frontend/nginx.conf` now terminates TLS for **etourvita.shop** and
redirects all HTTP to HTTPS. That gives you two valid setups, and mixing them is
the main thing that will waste your time:

| | Server (EC2) | Local (Windows) |
|---|---|---|
| `APP_PUBLIC_URL` | `https://etourvita.shop` | `http://localhost` |
| nginx config | the shipped HTTPS one | `docker/nginx/local.conf` |
| Certificates | `/etc/letsencrypt` (certbot) | none needed |
| Compose files | `docker-compose.yml` | `+ docker-compose.local.yml` |

> **Your `.env` currently says `APP_PUBLIC_URL=http://localhost`.** That is
> correct for local, but wrong for the server — deployed, the page would load
> over HTTPS and then call the API at `http://localhost`, which fails. Set it to
> `https://etourvita.shop` before deploying, and rebuild the frontend, because
> Vite bakes that value into the bundle.

---

## Running locally (Windows)

### Step 1 — Add three lines to `.env`

```dotenv
COMPOSE_FILE=docker-compose.yml;docker-compose.local.yml
CERTS_DIR=./docker/certs
APP_PUBLIC_URL=http://localhost
```

`COMPOSE_FILE` is the important one. It makes **every** `docker compose` command
in this directory load the local override automatically, so you can never
accidentally run the production HTTPS config on a machine with no certificates.
Doing it this way rather than typing `-f` flags removes a whole class of
mistake — forget the flags once and nginx crash-loops with
`cannot load certificate`.

On Windows the separator is `;`. On macOS/Linux use `:`.

`CERTS_DIR` points at an empty folder that already exists in the repo. The base
compose file mounts it at `/etc/letsencrypt`; the local nginx config never reads
a certificate, so it is simply an unused mount. Without it, Docker is asked to
bind `/etc/letsencrypt`, which does not exist on Windows.

### Step 2 — Start the .NET backend

```powershell
cd D:\ETOUR-PROD
docker compose --profile dotnet up --build
```

No `-f` flags needed — `COMPOSE_FILE` supplies them. Confirm the override is
actually in effect before you go further:

```powershell
docker compose --profile dotnet config | findstr local.conf
```

That must print a line mounting `docker/nginx/local.conf` onto
`/etc/nginx/conf.d/default.conf`. If it prints nothing, `COMPOSE_FILE` is not
being read — check `.env` is in `D:\ETOUR-PROD` and the line is not commented
out.

**First run takes 3–6 minutes** — restoring NuGet packages and compiling four
projects. Later runs are seconds.

### Step 3 — Watch for these three lines

```
etour-db              | ... ready for connections
etour-backend-dotnet  | Now listening on: http://[::]:8080
etour-frontend        | ... start worker process
```

If Google credentials are set you will also see the startup line telling you the
exact redirect URI to register.

### Step 4 — Check health

```powershell
docker compose ps
```

All three must say `(healthy)`. `etour-backend-dotnet` is the .NET container —
if you see `etour-backend` instead, you started the wrong profile.

### Step 5 — Open it

```
http://localhost
```

Log in with `admin.seed@etour.com` / `Admin@123` — the account is in the
restored dump, and BCrypt hashes work identically under both backends.

---

## Running on the server (EC2)

Certificates already exist there, so the defaults are correct.

```bash
cd ~/ETOUR-PROD

# .env
APP_PUBLIC_URL=https://etourvita.shop
APP_PORT=80
APP_TLS_PORT=443
CERTS_DIR=/etc/letsencrypt
```

```bash
docker compose --profile dotnet up --build -d
```

No override file: the shipped HTTPS `nginx.conf` is what you want. Open
<https://etourvita.shop>.

---

## Switching between the two backends

```powershell
# stop whichever is running - WITHOUT -v, so the database survives
docker compose --profile java --profile dotnet down

# start the other one
docker compose --profile dotnet up -d
```

Locally, the `COMPOSE_FILE` line in `.env` (Step 1) applies the override to both
commands automatically — there is nothing extra to type.

Only the backend image changes. The database, the uploads folder and the
frontend bundle are shared, so a booking made under Java is still there under
.NET.

---

## Everyday commands

```powershell
docker compose --profile dotnet up -d              # start detached
docker compose logs -f backend-dotnet              # follow the .NET logs
docker compose --profile dotnet up -d --build backend-dotnet   # rebuild after a code change
docker compose --profile dotnet down               # stop, keep data
docker compose --profile dotnet down -v            # stop and WIPE the database
docker compose exec backend-dotnet printenv | findstr ConnectionStrings   # check config reached it
```

`down -v` deletes the data volume and re-seeds from the dump on next start. Use
plain `down` unless you want that.

---

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `cannot load certificate`, nginx exits 1 and restarts forever | The production HTTPS config is being used on a machine with no certificates — the override is not applied. Check with `docker compose config \| findstr local.conf`; if that prints nothing, add the `COMPOSE_FILE` line to `.env` (Step 1). The tell is the error citing **line 36**, which only exists in the production config. |
| `host not found in upstream backend` | No profile passed, so no backend exists. Add `--profile dotnet`. |
| `manifest for mcr.microsoft.com/dotnet/sdk:10.0 not found` | Docker cannot reach Microsoft's registry, or .NET 10 images are unavailable in your region. `docker pull mcr.microsoft.com/dotnet/sdk:10.0` to see the real error. |
| Page loads, every API call fails | `APP_PUBLIC_URL` does not match the address bar. Fix it and **rebuild the frontend** — the value is compiled into the bundle. |
| `.NET` container unhealthy, logs show a MySQL error | Check the connection string resolved: `docker compose exec backend-dotnet printenv ConnectionStrings__Default`. It is built from `MYSQL_USER` / `MYSQL_PASSWORD`, which must match what MySQL was **first initialised** with — changing them later needs `down -v`. |
| Google button missing | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` blank, so `/api/auth/providers` reports `google:false` and the button hides itself. Same behaviour as Java. |
| `redirect_uri_mismatch` | The console URI must be `<APP_PUBLIC_URL>/login/oauth2/code/google`, byte for byte. Both backends use that same path now. |
| Build succeeds but the app exits immediately | Almost always a configuration binding error. `docker compose logs backend-dotnet` shows the exception; check for a stray quote in `.env`. |

---

## What I could not verify

The .NET image has **not been built** — this environment has no .NET SDK and
Microsoft's container registry is blocked from it, so
`docker compose --profile dotnet build` is untested. The Dockerfile follows the
standard multi-stage pattern and every path it references was checked against
the real solution layout, but the first build is the real test. Run it once
before you present.

Everything else here was verified: the Compose file resolves to exactly one
backend under each profile, the alias makes both answer to `backend`, and the
local nginx config parses cleanly.
