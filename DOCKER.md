# Running eTour with Docker

```bash
copy .env.example .env      # Windows   (macOS/Linux: cp .env.example .env)
docker compose up --build
```

Then open **<http://localhost>**.

First run takes a few minutes — Maven downloads dependencies, npm installs, and
MySQL imports the seed dump. Later runs start in seconds.

Seeded admin login: `admin.seed@etour.com` / `Admin@123`

---

## What runs

| Service | Image | Published | Purpose |
|---|---|---|---|
| `db` | `mysql:8.0` | *(none)* | Data, seeded from `db/*.sql` on first start |
| `backend` | built from `Backend/` | *(none)* | Spring Boot API on :8080, internal only |
| `frontend` | built from `etour-frontend/` | **`:80`** | nginx: React build + reverse proxy |

**Only nginx is exposed.** It serves the SPA and proxies `/api`, `/oauth2`,
`/login/oauth2` and `/uploads` to the backend, so the browser talks to exactly
one origin. That means no CORS, no second port to publish, and no database
reachable from outside the Docker network.

```
browser ──▶ :80 nginx ──┬──▶ / …………………… React bundle (SPA fallback)
                        ├──▶ /api/ ……………▶ backend:8080
                        ├──▶ /oauth2/ ……▶ backend:8080   (Google sign-in)
                        ├──▶ /login/oauth2/ ▶ backend:8080 (Google callback)
                        └──▶ /uploads/ ……▶ backend:8080   (tour media)
                                              │
                                              └──▶ db:3306
```

---

## Everyday commands

```bash
docker compose up --build          # start (rebuild changed images)
docker compose up -d               # start detached
docker compose logs -f backend     # follow one service
docker compose ps                  # status + health
docker compose down                # stop, keep data
docker compose down -v             # stop and WIPE the database
docker compose up --build backend  # rebuild just the API after a code change
```

---

## Configuration

All of it lives in `.env` at the project root; `.env.example` documents every
key. The three that matter most:

| Variable | Default | Notes |
|---|---|---|
| `APP_PUBLIC_URL` | `http://localhost` | The origin the **browser** uses |
| `APP_PORT` | `80` | Host port nginx binds |
| `JWT_SECRET` | *(empty)* | Set it, or restarts log everyone out |

### Changing the port

`APP_PUBLIC_URL` is compiled *into* the JavaScript bundle — Vite has no runtime
environment lookup — so it is a build argument, not a container variable.
Change it and you must rebuild the frontend:

```bash
# .env
APP_PORT=8081
APP_PUBLIC_URL=http://localhost:8081
```

```bash
docker compose up --build frontend
```

Leaving `APP_PUBLIC_URL` at the default while moving `APP_PORT` is the single
easiest mistake to make here: the page loads, but every API call goes to the old
origin and fails.

---

## The database

The dump named by `DB_SEED_FILE` is imported **once**, on first start with an
empty volume. Editing the file afterwards changes nothing, because the volume
already has data. To re-seed:

```bash
docker compose down -v
docker compose up --build
```

`docker compose down -v` deletes the `db_data` volume — every booking, user and
payment with it. Plain `down` is the safe one.

The dump opens with `DROP DATABASE` / `CREATE DATABASE`, which recreates the
schema out from under the grant MySQL's entrypoint had already issued to the app
user. `docker/mysql/20-grants.sh` runs afterwards and re-asserts it. That is why
init files are numbered — they execute in alphabetical order.

To inspect the data with a GUI client, uncomment the `ports` block on `db` in
`docker-compose.yml` (it maps host `3307`, so it will not collide with a MySQL
you already run on `3306`) and restart.

---

## Uploads

`./uploads` is bind-mounted to `/app/uploads`. Tour media and Excel batches stay
as ordinary files in the project folder, browsable from Windows, and survive
`docker compose down` and rebuilds.

---

## Google sign-in under Docker

Set the credentials in `.env`:

```dotenv
GOOGLE_CLIENT_ID=....apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=....
```

Then in the Google Cloud Console the authorised redirect URI must be:

```
http://localhost/login/oauth2/code/google
```

**Note there is no `:8080`.** Outside Docker the backend is reached directly on
port 8080, but here the browser reaches it through nginx on port 80, and Google
matches this string exactly. If you changed `APP_PORT`, include the new port.

Compose derives `GOOGLE_REDIRECT_URI` and `OAUTH2_FRONTEND_REDIRECT_URI` from
`APP_PUBLIC_URL`, and sets `SERVER_FORWARD_HEADERS_STRATEGY=framework` so Spring
trusts nginx's `X-Forwarded-*` headers and builds public URLs rather than
`http://backend:8080`.

Leave the two variables empty and Google sign-in stays off — the button never
renders and the app runs exactly as before. See `GOOGLE_OAUTH_LOGIN.md`.

---

## Notes on the images

**Backend** — multi-stage. Maven and the `~/.m2` cache stay in the build stage;
the runtime image is a JRE plus one jar. The POM is copied and resolved before
the sources, so editing Java code does not re-download dependencies. Runs as a
non-root user, with `MaxRAMPercentage` so the heap tracks the container's memory
limit instead of the host's. Health check hits `/api/auth/providers`, which only
answers once the Spring context is up — so `depends_on: service_healthy`
genuinely means ready.

**Frontend** — Node builds the bundle, nginx serves it. `node_modules` is
excluded from the build context: copying the host's copy would drag Windows
native bindings into a Linux image. Hashed assets get a one-year cache;
`index.html` gets none, so a redeploy never leaves browsers pointing at deleted
asset hashes.

Tests are skipped during the image build (`-DskipTests`) because they need a
live database that does not exist at build time. Run them against a real MySQL
with `mvn test`.

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `port is already allocated` | Something owns port 80 (IIS, Skype, another nginx). Set `APP_PORT` — and `APP_PUBLIC_URL` to match — then `docker compose up --build frontend`. |
| Page loads, every API call fails | `APP_PUBLIC_URL` does not match the URL in the address bar. Fix it and rebuild the frontend — it is baked into the bundle. |
| Backend restarts / `Access denied for user` | Credentials changed after the volume was created. `.env` is only read into MySQL on first init: `docker compose down -v` and start again. |
| Backend stuck "waiting"/unhealthy | It waits for `db` to pass its health check; importing the dump takes ~30–60s on first run. `docker compose logs -f db`. |
| Everyone logged out after every restart | `JWT_SECRET` is empty, so a random key is generated per process. `openssl rand -base64 48`. |
| `redirect_uri_mismatch` from Google | Console URI must equal `APP_PUBLIC_URL` + `/login/oauth2/code/google`, character for character. |
| Uploads fail with "permission denied" (Linux hosts) | The container runs as a non-root user and cannot write to a host folder owned by you. `sudo chown -R 999:999 ./uploads`, or switch that mount to a named volume. Docker Desktop on Windows and macOS is unaffected. |
| Rebuild seems to ignore changes | `docker compose build --no-cache <service>`. |

---

## Not covered here

This setup is aimed at running the whole stack locally with one command. Before
putting it on a public host you would still want: HTTPS termination (real
certificates, and `APP_PUBLIC_URL` switched to `https://`), non-default database
passwords, `JPA_DDL_AUTO=validate` with proper migrations instead of `update`,
resource limits, and a backup strategy for the `db_data` volume.
