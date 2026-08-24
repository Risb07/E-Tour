# Running eTour on Docker — step by step

A first-run walkthrough. Follow it top to bottom; each step says what you should
see before moving on.

Written for **Windows + PowerShell**, since that is where this project lives.
macOS/Linux differences are noted where they exist (there are only two).

Total time: about 5–10 minutes, nearly all of it the one-off first build.

---

## Step 1 — Install Docker Desktop

Skip if `docker` already works.

1. Download Docker Desktop: <https://www.docker.com/products/docker-desktop/>
2. Run the installer, leaving **"Use WSL 2 instead of Hyper-V"** ticked.
3. Reboot if it asks.
4. Launch Docker Desktop and wait for the whale icon in the system tray to stop
   animating. It must say **"Engine running"**.

> Docker Desktop has to be running before any command below. If it is not, every
> `docker` command fails with *"cannot connect to the Docker daemon"*. That
> error means "Docker Desktop is not started", not "something is broken".

---

## Step 2 — Check Docker works

Open **PowerShell** and run:

```powershell
docker --version
docker compose version
```

Expected — versions, any recent ones are fine:

```
Docker version 27.3.1, build ce12230
Docker Compose version v2.29.7
```

> Note it is `docker compose` (a space), not the older `docker-compose`
> (a hyphen). If only the hyphenated one exists, your Docker is old — update
> Docker Desktop. Every command in this document uses the space form.

---

## Step 3 — Go to the project folder

```powershell
cd D:\ETOUR-PROD
```

Confirm you are in the right place:

```powershell
dir
```

You should see `docker-compose.yml`, `Backend`, `etour-frontend`, `db` and
`uploads`. If `docker-compose.yml` is missing, you are in the wrong folder — the
next steps will not work.

---

## Step 4 — Create your `.env` file

The project ships `.env.example` as a template. Copy it:

```powershell
copy .env.example .env
```

*(macOS/Linux: `cp .env.example .env`)*

Verify it exists:

```powershell
type .env
```

**You can leave every value as-is for a first run.** The defaults work. Step 8
covers the two settings worth changing once it is running.

> `.env` holds passwords and is git-ignored on purpose. `.env.example` is the
> template that gets committed. Never rename one to the other.

---

## Step 5 — Start everything

```powershell
docker compose up --build
```

Leave this window open — it streams the logs of all three containers, and
closing it stops them.

**This first run takes 3–8 minutes.** It is downloading a Maven toolchain,
resolving Java dependencies, running `npm ci`, compiling the React bundle, and
importing your database dump. Later runs take seconds, because Docker caches all
of it.

You will see, roughly in order:

1. `[+] Building ...` — the two images being built. Long pauses here are normal.
2. `etour-db      | ... ready for connections` — MySQL is up.
3. `etour-db      | [20-grants] ensuring 'etour' can reach 'etour'` — the seed
   dump finished importing and database permissions were re-applied.
4. `etour-backend | Started BackendApplication in 12.345 seconds` — the API is
   live.
5. `etour-frontend| ... start worker process` — nginx is serving.

### What "finished" looks like

The log stream goes quiet. That is success — these are long-running servers, so
they do not print a "done" message and do not return you to a prompt.

> **Expected warning, not an error.** You will see:
>
> ```
> jwt.secret is not set - generated a TEMPORARY random signing key.
> ```
>
> Harmless for now — it just means restarting the backend logs everyone out.
> Step 8 turns it off.

---

## Step 6 — Confirm all three containers are healthy

Open a **second** PowerShell window (leave the first one streaming logs):

```powershell
cd D:\ETOUR-PROD
docker compose ps
```

Expected:

```
NAME             SERVICE    STATUS                 PORTS
etour-db         db         Up 2 minutes (healthy)
etour-backend    backend    Up 1 minute (healthy)
etour-frontend   frontend   Up 1 minute (healthy)   0.0.0.0:80->80/tcp
```

All three must say **`(healthy)`**.

- Still `(starting)` → wait 30 seconds and re-run. The backend waits for MySQL
  to finish importing the dump before it even begins booting.
- Any container `Restarting` or `Exited` → jump to
  [Troubleshooting](#troubleshooting) below.

Only `frontend` publishes a port. That is intentional: the API and the database
are reachable only from inside Docker's private network, so nothing else on your
machine — or your network — can touch them.

---

## Step 7 — Open the app and log in

Go to:

```
http://localhost
```

**Just `http://localhost`** — no port number, no `:5173`, no `:8080`.

You should land on the eTour showcase page. Click through to Home and confirm
tours and images load; that proves the browser → nginx → backend → MySQL chain
works end to end.

Now log in with the seeded admin account, which is already in your database dump:

| Email | Password |
|---|---|
| `admin.seed@etour.com` | `Admin@123` |

A successful login lands you on the admin dashboard. **If that works, you are
done** — the whole stack is running.

You can also register a fresh customer account through the UI to check the
customer-side flow.

---

## Step 8 — Two settings worth changing (optional)

Everything already works. These two are quality-of-life.

### 8a. Stop being logged out on every restart

Without a fixed signing key the backend generates a random one each time it
starts, which invalidates all existing tokens.

Generate a key:

```powershell
docker run --rm alpine sh -c "head -c 48 /dev/urandom | base64 -w0"
```

*(Uses Docker so you do not need OpenSSL installed. On macOS/Linux:
`openssl rand -base64 48`.)*

Paste the output into `.env`:

```dotenv
JWT_SECRET=<paste the generated string here>
```

Apply it:

```powershell
docker compose up -d --build backend
```

The startup warning is now gone.

### 8b. Port 80 is taken

If Step 5 failed with `port is already allocated`, something else owns port 80
(IIS, Skype, another nginx). Pick a different port in `.env` — **and change both
lines**:

```dotenv
APP_PORT=8081
APP_PUBLIC_URL=http://localhost:8081
```

Then rebuild the frontend:

```powershell
docker compose up -d --build frontend
```

The app is now at `http://localhost:8081`.

> **Why both lines?** `APP_PORT` is which port Docker publishes. `APP_PUBLIC_URL`
> is compiled *into* the JavaScript bundle, because Vite resolves environment
> variables at build time and cannot read them at runtime. Change only
> `APP_PORT` and the page will load but every API call will still be aimed at
> port 80 and fail. This is the single most common mistake with this setup, and
> it is also why this step needs `--build` rather than a plain restart.

---

## Everyday commands

Run all of these from `D:\ETOUR-PROD`.

### Start and stop

```powershell
docker compose up -d          # start in the background (no log stream)
docker compose stop           # stop containers, keep everything
docker compose start          # start them again
docker compose down           # stop and remove containers — DATA IS KEPT
```

After the first build, `docker compose up -d` starts the stack in a few seconds.

### View logs

```powershell
docker compose logs -f                # all services
docker compose logs -f backend        # just the API — start here when debugging
docker compose logs -f db
docker compose logs --tail=100 backend
```

`Ctrl+C` stops following the logs. It does **not** stop the containers.

### After changing code

```powershell
docker compose up -d --build backend    # changed Java
docker compose up -d --build frontend   # changed React
docker compose up -d --build            # changed both
```

Only the changed image rebuilds, and only from the changed layer onward, so
these are much faster than the first run.

### Reset the database

```powershell
docker compose down -v
docker compose up --build
```

> **`-v` deletes the database volume.** Every booking, user and payment created
> since you started is gone, and the original dump is re-imported from scratch.
> Plain `docker compose down` (no `-v`) is the safe one — use that unless you
> specifically want a clean slate.
>
> This is also the *only* way to re-import an edited dump: the seed file is read
> once, when the database volume is first created, and ignored ever after.

### Free up disk space

```powershell
docker system df        # see what is using space
docker system prune     # remove unused images, containers, networks
```

`docker system prune` does not touch named volumes, so your database survives.

---

## Troubleshooting

### `cannot connect to the Docker daemon`

Docker Desktop is not running. Start it, wait for "Engine running", retry.

### `port is already allocated`

Something owns port 80. See [Step 8b](#8b-port-80-is-taken).

To find the culprit:

```powershell
netstat -ano | findstr :80
```

### The page loads but nothing works — no tours, login fails

`APP_PUBLIC_URL` does not match the address bar. Make them agree in `.env`, then:

```powershell
docker compose up -d --build frontend
```

The rebuild is required — the URL is baked into the bundle.

### Backend keeps restarting

Look at the actual error:

```powershell
docker compose logs --tail=50 backend
```

- **`Access denied for user 'etour'`** — you changed the database credentials in
  `.env` *after* the volume was created. MySQL only reads those on first
  creation, so the container still has the old password. Fix:
  `docker compose down -v` then `docker compose up --build`.
- **`Communications link failure`** — the backend started before MySQL was
  ready. It should not happen (it waits on a health check), but
  `docker compose restart backend` resolves it.

### Backend stuck at `(starting)` and never turns healthy

Normal for up to ~90 seconds on the very first run while the dump imports.
Watch progress with `docker compose logs -f db`. If it is still stuck after
three minutes, check `docker compose logs backend` for a stack trace.

### Everything is healthy but `http://localhost` shows nothing

- Confirm the port: `docker compose ps` shows the real mapping in the PORTS
  column.
- Try `http://127.0.0.1` instead of `localhost`.
- Hard-refresh with `Ctrl+Shift+R` to clear a cached page.

### Changes to my code are not showing up

You restarted instead of rebuilding. Use `--build`:

```powershell
docker compose up -d --build backend
```

If it still looks stale, force a full rebuild:

```powershell
docker compose build --no-cache backend
docker compose up -d backend
```

### The first build fails partway through

Almost always a network hiccup while pulling Maven or npm packages. Just run
`docker compose up --build` again — completed layers are cached, so it resumes
rather than restarting.

---

## Where things live

| What | Where |
|---|---|
| Uploaded tour media, Excel batches | `D:\ETOUR-PROD\uploads` — a real folder, open it in Explorer |
| Database files | Docker volume `etour-prod_db_data`, not a folder on disk |
| Your configuration | `D:\ETOUR-PROD\.env` |
| Seed dump | `D:\ETOUR-PROD\db\etour_20260804_165623.sql` |

Because `uploads` is a bind mount, files you drop there appear inside the
container immediately, and files the app writes appear in Explorer immediately.

To browse the database with MySQL Workbench or DBeaver, uncomment the `ports`
block under the `db` service in `docker-compose.yml` (it maps host port **3307**,
so it will not clash with any MySQL already installed on 3306), then
`docker compose up -d db`.

---

## Enabling Google sign-in (optional)

Only if you want the "Continue with Google" button. Left blank, the feature
stays off and the button never appears.

1. Go to <https://console.cloud.google.com/> → **APIs & Services** →
   **Credentials**.
2. **Create Credentials** → **OAuth client ID** → type **Web application**.
3. Under **Authorised redirect URIs**, add exactly:

   ```
   http://localhost/login/oauth2/code/google
   ```

   **No `:8080`.** Under Docker the browser reaches the backend through nginx on
   port 80. If you changed `APP_PORT`, use that port here instead. Google matches
   this string character for character — a mismatch gives
   `redirect_uri_mismatch`.
4. Copy the Client ID and Client secret into `.env`:

   ```dotenv
   GOOGLE_CLIENT_ID=....apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=....
   ```
5. Restart the backend:

   ```powershell
   docker compose up -d backend
   ```

Reload `http://localhost/login` and the Google button will be there.

Full details in `GOOGLE_OAUTH_LOGIN.md`; the Docker reference is in `DOCKER.md`.

---

## Quick reference

```powershell
cd D:\ETOUR-PROD

copy .env.example .env             # once
docker compose up --build          # first run  → http://localhost
docker compose up -d               # every run after
docker compose logs -f backend     # watch the API
docker compose ps                  # health check
docker compose down                # stop, keep data
docker compose down -v             # stop, WIPE data
```

Login: `admin.seed@etour.com` / `Admin@123`
