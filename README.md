# swen3-paperless
Semester project for SWEN3 - Document Management System

## Architecture

The backend uses a three-layer, feature-first structure (controller, business, and
persistence). The persistence ports and adapters keep JPA and repository details out
of the business layer. See [the architecture documentation](docs/architecture.md).

## Build, deploy and run

```sh
docker compose up -d --build
```
This builds and starts PostgreSQL (port 5434), Keycloak (port 8082), the REST server (port 8080) and the web server (port 8081).
Open http://localhost:8081/ . You are redirected to the Keycloak login; log in with `user` / `user`.
Keycloak admin console: http://localhost:8082/ (`admin` / `admin`). Stop with `docker compose down`.

For development, only start the database (`docker compose up -d postgres`) and run the REST server from the IDE (database on port 5434).
