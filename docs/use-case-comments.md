# Use-Case 2: Document Comments

Users can attach comments to an uploaded document, list them and delete them.

## Entities

| Entity | Fields | Relation |
|--------|--------|----------|
| `Document` | id, filename, contentType, size, uploadedAt | 1 Document : N Comments |
| `Comment` (new) | id, document (FK), author, text, createdAt | N Comments : 1 Document |

Structure: feature first, then layer (`comment/controller`, `comment/service`, `comment/persistence/...`, `comment/models`, `comment/dto`).
- Persistence entities: `comment/persistence/entity/Comment`, `document/persistence/entity/Document`.
- Models used by the service layer: `CommentModel`, `DocumentModel`.
- `CommentMapper` (MapStruct) converts between `CommentModel` and `Comment`.
- The table `comment` is created by the Flyway migration `V2__create_comment_table.sql` (FK `document_id` -> `document`, index on `document_id`).

## Business component concept (`CommentService`)

Works only with models. Persistence is accessed through the ports `CommentPersistence` and `DocumentPersistence` (anti-corruption layer, implemented by the `*PersistenceAdapter` classes using the JPA repositories and mappers).

**Validation** (violation -> `InvalidCommentException` -> HTTP 400)
- author must not be null or blank
- text must not be null or blank
- text must not exceed 1000 characters

**Rules**
- A comment can only be added to an existing document (otherwise `DocumentNotFoundException` -> 404).
- Comments can only be listed for an existing document (otherwise 404).
- A comment is only deleted via the document it belongs to. Delete is idempotent and always answers 204; an unknown comment or a comment of another document is ignored.

**Logic**
- `createdAt` is set by the server when a comment is added; the id is generated on persist.

**Persistence**
- `CommentPersistence` (save, findById, findByDocumentId, deleteById), `DocumentPersistence` (existsById).

## REST endpoints

| Method | Path | Success | Errors |
|--------|------|---------|--------|
| POST | `/api/v1/documents/{documentId}/comments` | 201 + comment | 400 invalid, 404 document unknown |
| GET | `/api/v1/documents/{documentId}/comments` | 200 + list | 404 document unknown |
| DELETE | `/api/v1/documents/{documentId}/comments/{commentId}` | 204 (always) | - |

## Business workflow

1. User uploads a document (Use-Case 1) and receives its id.
2. User sends `POST /api/v1/documents/{id}/comments` with `author` and `text`.
3. Controller maps the request to `CommentModel` and calls `CommentService.addComment`.
4. Service validates the comment, checks that the document exists, sets `createdAt`.
5. `CommentPersistence` maps the model to an entity, saves it and maps the result back.
6. Controller returns `201 Created` with the `CommentDto`.
7. User lists the comments (`GET`) or removes one (`DELETE`); the service only deletes if the comment belongs to the document.
