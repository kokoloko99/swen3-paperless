# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

## [0.1.0] - 2026-10-21

### Added
- Authentication with OAuth2/JWT: Keycloak (realm `paperless`, import from `keycloak/paperless-realm.json`, port 8082) as authorization server; the REST server is an OAuth2 resource server (Spring Security) that validates JWTs via the JWKS URI and requires authentication for all endpoints (401 without token).
- Frontend login with Authorization Code Flow + PKCE; the token is stored in `localStorage` and sent as `Authorization: Bearer` with every API call; logout button.
- Demo user: `user` / `user`.

- Deployment: `paperless-rest/Dockerfile` (multi-stage build) and a `rest` service in `docker-compose.yml`; the whole solution (database, REST server, web server) is built and started with `docker compose up -d --build`. nginx now proxies `/api/` to the `rest` container.
- Web server sub-project `paperless-ui` (nginx, Dockerfile, `nginx.conf`) serving the simple web frontend (HTML5, CSS, JavaScript) from its content path `html/` at `http://localhost:8081/`; `/api/` is proxied to the REST server on port 8080. Added as service `ui` in `docker-compose.yml`.
- Frontend features: upload documents, look up documents by ID, list/add/delete comments.
- OpenAPI specification (`openapi.yaml`) for the "Document Upload" use case.
- Test scripts for the document endpoints (`documents.http`, `documents-curl.txt`).
- JaCoCo test coverage report (XML and HTML).
- Use-case concept for document comments (`docs/use-case-comments.md`).

### Changed
- Project version in `build.gradle` set to `0.1.0` (matches this changelog and the git tag `v0.1.0`).
- Added abstract `AuditableEntity` (`createdAt`, `updatedAt`) extending `BaseEntity`; `Document` and `Comment` extend it. Flyway migration `V3__add_audit_columns.sql` adds the columns.
- All object mapping now uses MapStruct: added `DocumentDtoMapper` and `CommentDtoMapper` (model <-> DTO / upload) replacing the hand-written mapping code in the controllers.
- Added abstract `BaseEntity` (`@MappedSuperclass`, UUID id) in `common/persistence/entity`; `Document` and `Comment` extend it.
- Error responses no longer expose technical details: generic messages for invalid requests (400) and unexpected errors (500); Spring error attributes (message, exception, stacktrace) are disabled.
- `DELETE /api/v1/documents/{documentId}/comments/{commentId}` now always returns 204 (idempotent); `CommentNotFoundException` was removed.
- Removed the service interfaces; `DocumentService` and `CommentService` are now plain `@Service` classes.
- Persistence is now a clear layer per feature (`persistence` with `entity`, `repository`, `mapper`). Services depend only on the ports `DocumentPersistence` / `CommentPersistence` (anti-corruption layer); the adapters translate between model and entity via MapStruct.
- Renamed BL entities from `*Bl` to `*Model` (`DocumentModel`, `CommentModel`) and mapper methods `toBl` to `toModel`.
- Mappers (`DocumentMapper`, `CommentMapper`) are now generated with MapStruct instead of being written by hand.
- API versioning: all endpoints are now served under `/api/v1` (e.g. `/api/v1/documents`).
- Project structure changed from pure layered to feature-first, then layered (document, comment, common with controller, service,
epository, entity, mapper, models, dto, exception inside each feature).
- PostgreSQL port changed from 5433 to 5434 (`docker-compose.yml`, `application.properties`) because 5433 was occupied by another Postgres instance.
- `CommentServiceImpl` works only with model entities; `CommentMapper.toDal` no longer takes a DAL `Document`.

## [0.0.1] - Initial version

### Added
- Spring Boot REST server with document upload (`POST /api/documents`) and metadata retrieval (`GET /api/documents/{id}`).
- Comment endpoints for documents (add, list, delete).
- PostgreSQL via Docker Compose, Flyway migrations, DAL entities and repositories.
- Model entities, mappers and service layer.
- Unit and repository tests.
