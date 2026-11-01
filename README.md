# AquaGrid – Intelligent Water Distribution Network Management System

AquaGrid is a college-level water distribution network manager. It models tanks and service areas as graph nodes and active pipelines as weighted, bidirectional connections for reachability and route planning. It records leak reports through a review lifecycle. The application does not simulate hydraulic pressure or determine instantaneous flow direction; routes show network connectivity only.

## Problem statement and objectives

Cities need a clear way to understand how water moves through a distribution network, identify underserved neighborhoods, plan around pipeline failures, and prioritize leak repairs. AquaGrid demonstrates these operations with a focused web application and a PostgreSQL-backed dataset.

Objectives include managing tanks, areas, and pipelines; recording supply and demand; detecting shortages and disconnected areas; finding efficient routes; simulating failures; and tracking leak reports through resolution.

## Features

- Operations dashboard with network totals, shortage and leak indicators, connectivity, and priority alerts.
- Network editor for adding/removing tanks, areas, and pipelines. Pipelines can be marked active or failed.
- Water balance table classifying areas as Sufficient, Shortage, or Critical; the shortage queue ranks largest deficit first.
- Route optimizer uses Dijkstra and ignores failed pipelines; it reports when no active route exists.
- Graph analysis displays DFS components and areas unreachable from every tank.
- User registration, secure password hashing, server-side sessions, resident/operator permissions, logout, leak reporting, My Reports, and resolution notes/date.
- Graph traversal, Dijkstra routing, heaps, FIFO reporting, and map-based lookups power the live application directly; there is no separate demonstration screen.
- REST API under `/api`, HTML/CSS/JavaScript frontend, PostgreSQL persistence.

## Technologies

- Frontend: HTML, CSS, vanilla JavaScript, SVG network visualization.
- Backend: Node.js and Express.js.
- Database: PostgreSQL, accessed with `pg`.

## Data structures and algorithms

Implementations live in `backend/structures.js` and are used by the API/UI:

- **Adjacency-list graph / Map:** node-to-pipeline lookup, traversable in either direction.
- **BFS:** tank reachability and disconnected area detection, using a FIFO array queue and active pipelines in either direction.
- **DFS:** graph traversal and physical connected-component reporting, ignoring pipe endpoint order.
- **Dijkstra:** minimum total distance, backed by the custom binary min-heap; active pipelines may be traversed in either direction and failed pipes are skipped.
- **Priority queue / heap:** area shortage ordering in the UI and severity ordering for leak reports.
- **Queue:** leak reports ordered by reported time for FIFO demonstration.
- **Hash lookup / Map:** leak ID index, with an API endpoint for direct lookup.

## System workflow

1. Express serves the static interface and `/api` REST endpoints.
2. PostgreSQL stores nodes, pipelines, and leaks; `database/seed.js` creates the schema and sample dataset.
3. The backend loads nodes and edges into the graph when topology or routes are requested.
4. The browser renders SVG topology and fetches dashboard, water, route, analysis, and leak information from REST APIs.
5. Operators update a leak through REPORTED → UNDER REVIEW → IN PROGRESS → RESOLVED, or REJECTED. RESOLVED requires a resolution note and records the resolution timestamp.

## Database schema

- `nodes`: tanks and areas; area demand/availability and tank capacity/current level.
- `pipelines`: source, destination, distance/cost, capacity, ACTIVE/FAILED state.
- `users`: display name, unique email, password hash, and USER/ADMIN role.
- `leaks`: unique ID, optional pipeline and reporting user, location, description, severity, estimated loss, lifecycle status, timestamps, resolution note.

Schema and immediately usable sample records are in `database/schema.sql` and `database/seed.sql`.

## Setup and run

Prerequisites: Node.js 18+, npm, and PostgreSQL 14+.

```sh
npm install
createdb aquagrid
cp .env.example .env
npm run db:seed
npm start
```

Set `DATABASE_URL` in `.env` to match your local PostgreSQL user/password. On a default Homebrew PostgreSQL installation, `postgresql://localhost:5432/aquagrid` uses your macOS username. `npm run db:seed` creates/migrates tables and adds sample network data only when the nodes table is empty; existing network data is preserved. It creates or resets the demo admin account. Open <http://localhost:3000> and sign in with `admin@aquagrid.local` / `AquaGridAdmin!23`, or create a resident account from the login page. Residents can view network/leak statuses, report leaks, and track their own reports. Admins can edit nodes/pipelines and manage leak statuses. Set `DEMO_ADMIN_PASSWORD` before seeding to choose a different demo admin password, and use a private `SESSION_SECRET` outside local development.

Useful endpoints: `GET /api/dashboard`, `GET /api/network`, `GET /api/areas`, `GET /api/analysis`, `GET /api/route?source=T-01&target=A-03`, `GET/POST /api/leaks`, `GET /api/leaks/:id`, and `PATCH /api/leaks/:id`.

## Complexity

For `V` nodes, `E` pipelines, and `L` leaks:

| Operation | Time | Space |
|---|---:|---:|
| BFS / DFS | O(V + E) | O(V) |
| Dijkstra with binary heap | O((V + E) log V) | O(V + E) |
| Graph adjacency list | O(V + E) storage | O(V + E) |
| Heap enqueue/dequeue | O(log L) | O(L) |
| Leak ID Map lookup | O(1) average | O(L) |
| FIFO queue enqueue/dequeue | O(1) enqueue, O(L) dequeue in the simple demonstration queue | O(L) |

## SDG connection

- **SDG 6 – Clean Water and Sanitation:** make supply gaps and leaks visible.
- **SDG 11 – Sustainable Cities and Communities:** support resilient urban service planning.
- **SDG 12 – Responsible Consumption and Production:** surface avoidable water loss for repair prioritization.
