# Sprint 4: Workers, MinIO, OCR, Elasticsearch

## Ablauf

Upload -> Metadaten in PostgreSQL -> persistente RabbitMQ-Nachricht ->
MinIO-Original -> Tesseract-OCR -> Elasticsearch-Dokument -> RabbitMQ-ACK.

Der REST-Vertrag bleibt unveraendert. Der bestehende Binaertransport ueber RabbitMQ
bleibt erhalten. MinIO wird im Worker ueber das Interface DocumentStorage angesprochen;
TextExtractor und DocumentIndexer entkoppeln OCR und Indexierung von der Ablaufsteuerung.
Originale liegen im Bucket `documents` unter ihrer Dokument-UUID. Elasticsearch verwendet
dieselbe UUID als `_id` im Index `paperless-documents`. Wiederholte Zustellungen
ueberschreiben dieselben Eintraege. Der Index enthaelt `id` (keyword), `filename` (text),
`contentType` (keyword) und `text` (text). Er wird beim ersten Dokument angelegt.

PDFs werden mit Poppler bei 200 DPI in Bilder umgewandelt; Tesseract verarbeitet
alle Seiten in numerischer Reihenfolge. PNG, JPEG und TIFF gehen direkt an Tesseract.
Die Sprachmodelle `eng+deu` werden im Worker-Image installiert. Temporaere Dateien
werden auch bei Fehlern entfernt. Jedes externe Kommando hat ein Zeitlimit von zwei
Minuten. Tesseract wird als Prozess ohne Shell ausgefuehrt; Dateinamen aus Nachrichten
werden nicht als lokale Dateipfade verwendet. Leerer OCR-Text ist ein erlaubtes Ergebnis.

## Start und Kontrolle

```powershell
docker compose up -d --build
docker compose ps
docker compose logs --tail 50 ocr-worker
# Optional: Kibana
docker compose --profile tools up -d kibana
```

MinIO-Image: `ghcr.io/coollabsio/minio:RELEASE.2025-04-22T22-12-26Z`.
Das Image ist zusaetzlich per SHA-256-Digest in Compose fixiert.
Dieser Community-Mirror wird verwendet, weil die bisherigen Images von Docker Hub
und Quay beim Test nicht abrufbar waren. Es ist kein offizielles MinIO-Registry-Image.
Elasticsearch und Kibana sind gemeinsam auf 9.4.1 festgelegt, ebenso der Java-Client.
MinIO-Demo-Zugang: `paperless / paperless-secret`; ueberschreibbar durch
`MINIO_ROOT_USER` und `MINIO_ROOT_PASSWORD`. Die Elastic-Sicherheit ist fuer die lokale
Uebung deaktiviert.

1. Eine gut lesbare PDF oder ein PNG mit markantem Text ueber die bestehende UI hochladen.
2. Dokument-ID merken und den erfolgreichen Worker-Logeintrag kontrollieren.
3. In der MinIO-Console auf Port 9001 den Bucket `documents` oeffnen:
   Das Objekt mit dieser ID muss die Originaldatei enthalten.
4. Nach dem Elasticsearch-Refresh (normalerweise wenige Sekunden) pruefen:

```powershell
Invoke-RestMethod http://localhost:9200/paperless-documents/_doc/DOKUMENT-ID
$body = '{"query":{"match":{"text":"Rechnung"}}}'
Invoke-RestMethod -Method Post -ContentType application/json -Body $body http://localhost:9200/paperless-documents/_search
```

## Fehler und Queue-Wechsel

Neue Uploads gehen an `paperless.ocr.v4`; die vorherige Queue `paperless.ocr` bleibt
unveraendert. Vor dem Upgrade sollte sie mit dem alten Worker geleert werden;
alte Nachrichten werden nicht automatisch migriert. REST und Worker gemeinsam aktualisieren.
Die neue Queue besitzt eine Dead-Letter-Route zur dauerhaften Queue `paperless.ocr.v4.dead`.
Fehler werden hoechstens dreimal mit Wartezeiten erneut versucht. Ungueltige Nachrichten
werden verworfen und per Dead-Letter-Routing aufbewahrt. Ein ACK erfolgt erst nach
erfolgreicher Indexierung. Nach Fehlerbehebung koennen Dead-Letter-Nachrichten ueber
die RabbitMQ-Verwaltung kontrolliert erneut publiziert werden; sie werden nicht automatisch
endlos wiederholt. Originaldateien bleiben bei OCR-/Indexfehlern in MinIO erhalten.

## Tests und Coverage

Vom Projektverzeichnis:

```powershell
.\paperless-rest\gradlew.bat -p paperless-ocr-worker clean check bootJar
.\paperless-rest\gradlew.bat -p paperless-rest check
```

JUnit und Mockito testen Ablaufreihenfolge, Validierung, Abbruch bei Speicher-/OCR-
und Indexfehlern, stabile IDs bei Wiederholung, MinIO-Upload, Index-Mapping,
PDF-Seitenreihenfolge, temporaere Dateien sowie Prozessfehler und Timeouts.
`check` scheitert in beiden Java-Modulen unter 71 % Zeilenabdeckung. Fuer das gesamte Worker-Modul,
einschliesslich Konfiguration und Einstiegspunkt, sind keine Klassen ausgeschlossen.
Berichte: `paperless-ocr-worker/build/reports/tests/test/index.html` und
`paperless-ocr-worker/build/reports/jacoco/test/html/index.html`.
Mocks ersetzen keine reale OCR- oder Docker-Pruefung.

## Bewusste Grenzen

- Keine neue Suchoberflaeche oder Such-REST-API; Indexabfrage ueber Elasticsearch/Kibana.
- Dokumentloeschung entfernt weiterhin Metadaten und Kommentare. MinIO-Original und
  Elasticsearch-Eintrag werden noch nicht geloescht; wartende Nachrichten werden nicht storniert.
- Datenbank-Commit und Queue-Versand sind nicht atomar. Ein Outbox-Verfahren fehlt.
- Zwei Minuten gelten pro Kommando, nicht pro Gesamtdokument. Sehr grosse oder
  beschaedigte PDFs koennen Ressourcen benoetigen. Die bisherigen Upload-Limits bleiben.
- Elasticsearch und MinIO haben eigene persistente Volumes; `down -v` entfernt auch diese.
- Logstash ist fuer die direkte OCR-Indexierung nicht erforderlich und nicht eingerichtet.

## Referenzen

- Elasticsearch-Java-Client: https://www.elastic.co/docs/reference/elasticsearch/clients/java/setup/installation
- Verwendetes MinIO-Mirror-Paket: https://github.com/coollabsio/minio/pkgs/container/minio
- MinIO-Projekt: https://github.com/minio/minio

## Verifiziert am 18.11.2026

- Vollstaendiger Docker-Compose-Build und Start erfolgreich (acht Dienste, ohne optionales Kibana).
- 70 Backend-Tests und 24 Worker-Tests erfolgreich.
- JaCoCo-Zeilenabdeckung: Backend 270/311 = 86,8 %, Worker 100/111 = 90,1 %.
- Coverage-Pruefung `check` in beiden Modulen erfolgreich.
- Authentifizierter PNG-Upload ueber REST mit Keycloak-PKCE: OCR erkennt
  `Sprint Four Invoice 48271`, Elasticsearch-Volltextsuche findet das Dokument.
- MinIO-Original nach Download bytegenau mit dem Upload verglichen.
- PDF mit zwei Seiten: beide Texte vollstaendig und in korrekter Reihenfolge indexiert.
- Erneute Zustellung derselben PNG-Dokument-ID: derselbe Indexeintrag wird auf Version 2
  aktualisiert. Queue anschliessend leer.
- Die beiden eigens erzeugten Testdokumente anschliessend aus PostgreSQL, MinIO und
  Elasticsearch entfernt. Eine automatisierte koordinierte Loeschfunktion entsteht dadurch nicht.

Die Docker-Builds selbst fuehren keine Tests aus. Kibana wurde nicht gestartet oder getestet.

Aktuelle Wiederholungspruefung: [25.11.2026](verification-2026-11-25.md).
