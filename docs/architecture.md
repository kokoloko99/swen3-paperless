# Architecture

The REST application follows a feature-first three-layer architecture. Each feature
(`document`, `comment`) contains its own controller, business, and persistence code.

```text
HTTP request/response
        |
Controller + DTO mapper
        |
Business service
        |
Persistence port (interface)
        |
Persistence adapter + entity mapper
        |
Spring Data repository / PostgreSQL
```

## Layer responsibilities

| Layer | Packages | Responsibility |
| --- | --- | --- |
| Controller | `*/controller`, `*/dto`, `*/mapper` | Exposes the versioned REST API under `/api/v1`, maps HTTP data to models, and returns HTTP responses. |
| Business | `*/service`, `*/models`, `*/exception` | Applies use-case rules, validation, timestamps, and domain-specific errors. It depends on persistence interfaces only. |
| Persistence | `*/persistence` | Implements persistence ports, maps models to JPA entities with MapStruct, and accesses Spring Data repositories. |
| Cross-cutting | `common/*` | Provides shared technical concerns such as exception handling, security, and auditable base entities. |

## Persistence boundary

`DocumentPersistence` and `CommentPersistence` are ports used by the business
services. `DocumentPersistenceAdapter` and `CommentPersistenceAdapter` implement
those ports and keep JPA repositories and entities outside the business layer.
`DocumentMapper` and `CommentMapper` are MapStruct mappers at this boundary.

This is an adapter/port boundary often described as an anti-corruption layer: the
business models do not expose JPA entity relationships or repository APIs. A separate
ACL for an external system is not needed because the application currently has no
external domain system to translate.

## Auditing and API conventions

- `BaseEntity` provides a generated UUID identifier.
- `AuditableEntity` provides `createdAt` and `updatedAt`; persistence entities extend it.
- All public endpoints use URL versioning (`/api/v1`).
- Delete operations are idempotent and return `204 No Content`.
