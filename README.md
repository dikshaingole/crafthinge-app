# crafthinge-app

Source code for **CraftHinge**, a fictional US food ordering & delivery platform built as an end-to-end Azure DevOps / GitOps portfolio project.

Customers browse restaurants, fill a cart, pay and track their order. Restaurant owners manage menus and move orders through the kitchen. Drivers claim and deliver orders. Everything runs on your laptop with Docker Compose; the same images later run on AKS.

---

## 1. Run it locally (no Azure needed)

### Prerequisites
| Tool | Why | Check |
|---|---|---|
| Docker Desktop (or Docker Engine + Compose v2) | Builds and runs all containers | `docker compose version` |
| **6 GB+ memory for Docker** | 8 JVMs + Postgres + nginx | Docker Desktop → Settings → Resources |
| Python 3 | Smoke test | `python3 --version` |
| *Optional:* Java 21 + Maven 3.9 | Run unit tests outside Docker | `java -version`, `mvn -v` |
| *Optional:* Node 20+ | Frontend hot-reload dev server | `node -v` |

On Windows, run everything from **WSL 2 (Ubuntu)** with Docker Desktop's WSL integration turned on.

### Start
```bash
docker compose up --build -d      # first build: 5-10 min (Maven downloads); later builds are cached
docker compose ps                 # wait until all services are "running"
docker compose logs -f order-service   # watch one service (Ctrl+C to stop following)
```
Open **http://localhost:8080**.

### Demo accounts (password `Password123!`)
| Email | Role | Where to go |
|---|---|---|
| customer@crafthinge.dev | Customer | Browse, add to cart, check out, track |
| owner@crafthinge.dev | Restaurant owner (owns all 8 demo restaurants) | **Kitchen** page |
| driver@crafthinge.dev | Delivery partner | **Deliveries** page |

Test cards: `4242 4242 4242 4242` is approved, `4000 0000 0000 0002` is declined. No real payment is made.

### Try the full flow in the browser
1. **Customer:** open Tsuki Ramen Bar → add 2 ramen → Cart → enter an address → Place order. You land on the tracking page.
2. **Owner** (use a private/incognito window): Kitchen → the order appears → Accept → Start preparing → Ready for pickup.
3. **Driver** (another browser or profile): Deliveries → Accept job → I picked it up → Mark delivered.
4. **Customer:** the tracking page updates every 5 seconds and shows the notification messages.

### Or prove it automatically
```bash
python3 tests/smoke_test.py
```
It plays all three roles through a full order, checks a declined card, the "customer can't mark delivered" rule, the event-driven delivery job and notifications, then prints `SMOKE TEST PASSED`.

### Stop / reset
```bash
docker compose down        # stop, keep data
docker compose down -v     # stop and delete the database volume (fresh demo data next start)
```

### Run unit tests (optional, needs Java + Maven)
```bash
mvn -B test                       # all services
mvn -B -pl order-service -am test # one service
```

---

## 2. How it fits together

```
Browser ──► gateway :8080 (nginx) ──┬── /                  → frontend (React, nginx)
          (= Ingress/App Gateway    ├── /api/auth, /users  → user-service
             in AKS)                ├── /api/restaurants   → restaurant-service
                                    ├── /api/menu          → food-service ──────► restaurant-service (ownership)
                                    ├── /api/cart          → cart-service ──────► food-service (real price)
                                    ├── /api/orders        → order-service ─────► cart, restaurant, payment
                                    │                            │ publishes ORDER_PLACED / ORDER_STATUS_CHANGED
                                    │                            ├──► delivery-service  (creates delivery job)
                                    │                            └──► notification-service (customer messages)
                                    ├── /api/payments      → payment-service (mock card processor)
                                    ├── /api/deliveries    → delivery-service ───► order-service (status)
                                    └── /api/notifications → notification-service
All data services ─► PostgreSQL (one server, one database per service)
```

### The services

| Service | Owns (database) | Responsibilities | Calls |
|---|---|---|---|
| user-service | `users` | Register, login (BCrypt), issue JWT, "me" | - |
| restaurant-service | `restaurants` | Browse/search/cuisines (public), owner CRUD | - |
| food-service | `menu` | Menus, featured dishes, owner CRUD | restaurant-service |
| cart-service | `carts` | One-restaurant cart, server-side pricing | food-service |
| order-service | `orders` | Checkout orchestration, status state machine, history, events | cart, restaurant, payment |
| payment-service | `payments` | Charge (mock provider), idempotent per order, last-4 only | - |
| delivery-service | `deliveries` | Jobs from events, atomic driver claim, pickup/deliver | order-service |
| notification-service | none (in-memory) | Turns events into customer messages | - |
| frontend | - | React SPA served by unprivileged nginx | via gateway |

### Design decisions worth explaining in an interview

**Database per service.** No service reads another's tables. When food-service needs to know who owns a restaurant, it *asks* restaurant-service over HTTP. That's why there are no foreign keys across services.

**Service discovery by DNS name.** Services call `http://food-service:8080`. In Docker Compose and in Kubernetes alike, that name resolves to the service, so the code is the same in both places. A pod IP is never used.

**Authentication with JWT.** user-service signs a token containing the user id and role. Every other service verifies the signature with the shared `JWT_SECRET` and needs no call back to user-service. The user's token is forwarded on service-to-service calls, so permissions are checked consistently.

**Never trust the browser.** The cart stores the price returned by food-service, not one sent by the client. Order status changes are validated by a state machine (`OrderStatus.java`) with per-role permissions.

**Synchronous vs asynchronous.** Checkout must know immediately whether payment worked, so it calls payment-service synchronously. "Tell the customer" and "create a delivery job" don't need to block checkout, so they are **events**.
- Locally: `EVENTS_MODE=http`, where order-service POSTs events to the subscribers. Simple, but an event is lost if a subscriber is down.
- In Azure: `EVENTS_MODE=servicebus`, where events go to the Service Bus topic `order-events` with one subscription per consumer, retries, and a dead-letter queue. Authentication uses Workload Identity, with no connection strings.
- Consumers are **idempotent** (a unique `order_id` on deliveries) because Service Bus delivers at-least-once.

**Concurrency.** Two drivers tapping "Accept" at once: a single `UPDATE ... WHERE partner_id IS NULL` means only one wins, and the other gets 409. Orders use `@Version` optimistic locking.

**Schema migrations.** Flyway (`src/main/resources/db/migration`) owns every table. Hibernate only *validates* (`ddl-auto: validate`). Schema changes are versioned SQL in Git, applied automatically on startup.

**Configuration (12-factor).** Everything environment-specific is an env var (`DB_URL`, `JWT_SECRET`, `EVENTS_MODE`, service URLs). Locally they come from `docker-compose.yml`. In AKS, non-secret values come from a ConfigMap and secrets from Key Vault (Phase 6).

**Health and shutdown.** Each service exposes `/actuator/health/liveness` and `/actuator/health/readiness` (readiness includes the DB check), which Kubernetes probes will use. `server.shutdown: graceful` finishes in-flight requests when a pod is stopped during a rolling update. `/actuator/prometheus` exposes metrics, including custom ones: `crafthinge_orders_placed_total`, `crafthinge_payments_total`, `crafthinge_notifications_sent_total`.

### The Docker images

Each service has its own multi-stage `Dockerfile` (build context = repo root, because services depend on `common-lib`):
1. **Build stage:** `maven:3.9-eclipse-temurin-21`, POMs copied first so dependency layers cache, BuildKit cache mount for `~/.m2`, then `mvn package`.
2. **Layer extraction:** the Spring Boot jar is split into dependencies / loader / application layers, so a code change only re-pushes the small application layer.
3. **Runtime stage:** `gcr.io/distroless/java21-debian12:nonroot`, which has a JRE only, no shell, no package manager, and runs as a non-root user. That means a smaller attack surface and fewer CVEs for Trivy to report.
4. `-XX:MaxRAMPercentage=75` sizes the heap from the container's memory limit.

The frontend image builds with `node:22-alpine` and serves from `nginx-unprivileged` on port 8080 (non-root) with security headers and a `/healthz` endpoint.

Tags: images are never tagged `latest` for deployment. CI (Phase 4) tags them with the build ID, e.g. `crafthingeacr.azurecr.io/order-service:105`.

---

## 3. API reference

| Method & path | Who | Purpose |
|---|---|---|
| `POST /api/auth/register` | anyone | Create account (role: CUSTOMER / RESTAURANT_OWNER / DELIVERY_PARTNER) |
| `POST /api/auth/login` | anyone | Returns `{ token, user }` |
| `GET /api/users/me` | signed in | Current user |
| `GET /api/restaurants?cuisine=&q=` | anyone | Search restaurants |
| `GET /api/restaurants/cuisines` | anyone | Cuisines with counts |
| `GET /api/restaurants/{id}` | anyone | Restaurant details |
| `GET /api/restaurants/mine` · `POST` · `PUT /{id}` · `DELETE /{id}` | owner | Manage own restaurants |
| `GET /api/menu/restaurants/{id}/items` | anyone | Menu |
| `GET /api/menu/featured` | anyone | Home page picks |
| `POST /api/menu/restaurants/{id}/items` · `PUT/DELETE /api/menu/items/{id}` | owner | Manage menu |
| `GET /api/cart` · `POST /api/cart/items[?replace=true]` · `PATCH/DELETE /api/cart/items/{menuItemId}` · `DELETE /api/cart` | customer | Cart |
| `POST /api/orders` | customer | Checkout `{ deliveryAddress, cardNumber }` |
| `GET /api/orders` | customer | Order history |
| `GET /api/orders/{id}` | customer / owner / driver | Tracking incl. history |
| `GET /api/orders/restaurant/{id}` | owner | Incoming orders |
| `PATCH /api/orders/{id}/status` | per state machine | Move order |
| `GET /api/deliveries/available` · `/mine` · `POST /{id}/accept|pickup|deliver` | driver | Deliveries |
| `GET /api/notifications/me` | customer | Recent updates |

Errors use RFC 7807 `application/problem+json`, e.g. `{"status":409,"detail":"Your cart has food from another restaurant..."}`.

---

## 4. Troubleshooting (local)

| Symptom | Check / fix |
|---|---|
| A service keeps restarting | `docker compose logs <service>`. Most often Postgres wasn't ready (it retries) or memory is too low: give Docker 6 GB+ |
| `Exited (137)` | Out of memory (OOMKilled). Same as Kubernetes; raise Docker's memory |
| Login works but everything else returns 401 | Services must share the same `JWT_SECRET`. Don't override it for just one service |
| `FlywayValidateException` after editing a migration | Never edit an applied migration; add `V3__...sql`. Locally you can reset with `docker compose down -v` |
| Port 8080 or 5432 already in use | Stop the other program, or change the left side of the `ports:` mapping |
| Frontend loads but shows "Can't reach CraftHinge" | `docker compose ps`: is `gateway` running? Its logs show which upstream failed |
| Slow first build | Maven downloads dependencies once; the BuildKit cache makes later builds fast |

---

## 5. Repository layout
```
crafthinge-app/
├── pom.xml                       parent (Spring Boot 3.5, Java 21)
├── common-lib/                   JWT security, errors, REST client, events (HTTP + Service Bus)
├── user-service/ ... notification-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/{java,resources/application.yml,resources/db/migration}, src/test
├── frontend/                     React + Vite, Dockerfile, nginx.conf
├── docker/                       gateway.conf (local Ingress), postgres-init.sql
├── docker-compose.yml            whole platform locally
└── tests/smoke_test.py           end-to-end check (also used as a CI/CD gate)
```
