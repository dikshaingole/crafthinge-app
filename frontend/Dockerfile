# syntax=docker/dockerfile:1.7
# Build context: this folder.   docker build -t frontend:local frontend/

# ---------- stage 1: build the static site ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN --mount=type=cache,target=/root/.npm npm ci || npm install
COPY . .
RUN npm run build

# ---------- stage 2: serve with unprivileged nginx (runs as uid 101, port 8080) ----------
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
