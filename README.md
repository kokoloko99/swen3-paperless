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
This builds and starts PostgreSQL (port 5434), Keycloak (port 8082), RabbitMQ
(ports 5672 and 15672), the REST server (port 8080), the OCR worker and the web
server (port 8081).
Open http://localhost:8081/ . You are redirected to the Keycloak login; log in with `user` / `user`.
Keycloak admin console: http://localhost:8082/ (`admin` / `admin`). Stop with `docker compose down`.

For development, start PostgreSQL and RabbitMQ (`docker compose up -d postgres rabbitmq`) and run the REST server from the IDE (database on port 5434). Keycloak is also needed for authenticated requests.

## OCR queue (Sprint 3)

For backend development, start both dependencies with `docker compose up -d postgres rabbitmq`.
RabbitMQ uses AMQP port 5672. Its management UI is at http://localhost:15672/
(login: `paperless` / `paperless`). Docker connects the REST service to host
`rabbitmq`; running from the IDE uses `localhost`.

Every successful document upload publishes the original file bytes to the durable
queue `paperless.ocr`, using the default exchange. Messages are persistent and
include `documentId` and `filename` headers, the MIME content type, and the document
ID as message ID. The publisher waits up to five seconds for broker confirmation.
The separate Spring Boot project `paperless-ocr-worker` consumes the binary body
directly; it is not JSON or Base64. For Sprint 3 this is an "empty" worker: it
logs the document ID, filename, byte count and content type without performing
OCR. Spring acknowledges each message after the listener returns successfully.
Acknowledged files are removed from the queue; the placeholder does not store
their contents or produce extracted text.

To check manually: start the stack, upload a PDF through the existing UI, then
run `docker compose logs -f ocr-worker`. A log entry beginning with
`Empty OCR worker processed document` should appear for the uploaded document.
In RabbitMQ's **Queues and Streams** page, `paperless.ocr` should have a consumer;
its ready-message count normally returns to zero after processing.

To demonstrate buffering, run `docker compose stop ocr-worker`, upload another
file and check that the ready-message count increases. Run
`docker compose start ocr-worker` and check its logs and the queue count again.
Both applications declare the same durable queue, so either can start first.

Publishing failures fail the upload and roll back its database transaction.
Database commits and RabbitMQ publishing are not atomic: a commit failure or a
lost confirmation can still leave a queued message. Consumers should handle
duplicate document IDs; an outbox would be needed for stronger delivery guarantees.

## Failure handling

The REST API translates exceptions at its boundaries and keeps their original
causes for server-side logging. Repository `DataAccessException`s are wrapped in
`PersistenceOperationException`. RabbitMQ failures become `OcrMessagingException`
(an `OcrPublishException` from the publishing port); the business service wraps
these in `OcrSubmissionException`. The service does not depend on RabbitMQ classes.
Reading an uploaded file can raise the controller's `DocumentReadException`.

`GlobalExceptionHandler` maps these failures to HTTP responses without exposing
SQL, connection details or stack traces:

| Failure | HTTP status |
| --- | --- |
| Invalid document, comment or request | 400 |
| Document not found | 404 |
| Upload exceeds configured multipart limit | 413 |
| Database access/transaction failure | 503 |
| OCR message submission failure | 503 |
| File read failure or unexpected server error | 500 |

An OCR submission exception propagates out of the transactional upload service,
so the database transaction is rolled back. Broker confirmation and database
commit still have the delivery limitations described above.

## Logging

Both applications use SLF4J with Spring Boot's console logging. View the upload
and worker flow with `docker compose logs -f rest ocr-worker`.

- `INFO`: upload received, RabbitMQ publisher confirmation, upload completed
  after database commit, and the empty worker's processing step.
- `DEBUG`: database operations, publishing start, transaction preparation, and
  worker receipt with message ID and redelivery flag.
- `WARN`: invalid requests, missing documents, and publishing failure with
  document ID and queue name.
- `ERROR`: database, OCR submission, file-read and unexpected failures, including
  their exception causes in the REST server's central handler. Spring AMQP logs
  listener and connection failures in the worker.

Use the same `documentId` in the publisher and worker logs to follow a document.
The worker can process a message before the REST request finishes; broker
confirmation and worker completion are separate events. Logs do not include
document bodies, comment text, tokens or passwords as application log fields.
To enable detailed logs locally, set `logging.level.at.fhtw.swen3=DEBUG` in the
application properties, or the environment variable
`LOGGING_LEVEL_AT_FHTW_SWEN3=DEBUG` for the relevant Docker service.
