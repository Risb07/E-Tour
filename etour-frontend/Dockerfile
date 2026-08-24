# =============================================================================
# eTour frontend - React 18 / Vite, served by nginx
#
# nginx also reverse-proxies the API, so the browser talks to a single origin
# and CORS never enters the picture. See nginx.conf.
# =============================================================================

# ---- Stage 1: build ---------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /build

# package*.json first so `npm ci` is cached on the lockfile, not on src changes.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite inlines every VITE_* variable into the bundle at BUILD time - there is no
# runtime lookup. So the public origin has to be known here, which is why this
# is a build arg rather than a container environment variable.
#
# It must be the origin the BROWSER uses (the nginx one), not the internal
# Docker hostname: the browser resolves it, and Docker's network is invisible
# from the host. Changing it means rebuilding this image.
ARG VITE_API_BASE_URL=http://localhost
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ---- Stage 2: runtime -------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

# Replace the stock config; ours adds the SPA history fallback and the API
# proxy rules.
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
    CMD wget -qO- http://127.0.0.1/health >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
