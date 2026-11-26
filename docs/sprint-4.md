# Sprint 4: Workers, MinIO, OCR und ELK

## Anforderungen

| Nr. | Vorgabe | Umsetzung |
| --- | --- | --- |
| 1 | Eigene Worker-Anwendungen | paperless-ocr-worker und paperless-indexing-worker als separate Spring-Boot-Anwendungen |
| 2 | Tesseract und PDF-Konverter | Tesseract mit eng/deu; Poppler rendert PDF-Seiten bei 200 DPI |
| 3 | REST speichert PDFs in MinIO | Speicherung im Upload-Service vor dem Queue-Versand |
| 4 | OCR lädt Original aus MinIO | Referenznachricht lesen, Original laden, Text erkennen |
| 5 | Elasticsearch im Worker | Offizieller Java-Client im Indexing-Worker, per Dependency Injection |
| 6 | Eigener Indexing-Worker | OCR-Ergebnisse aus separater Queue in Elasticsearch speichern |
| 7 | Container für MinIO, Worker und ELK | Alle Dienste einschließlich Logstash und Kibana in Docker Compose |

## Verarbeitung

1. REST validiert den Upload und speichert die Metadaten in PostgreSQL.
2. Das Original wird im MinIO-Bucket `documents` unter seiner Dokument-UUID gespeichert.
3. Erst danach publiziert REST eine persistente Referenz nach `paperless.ocr.v5`.
4. OCR lädt das Original aus MinIO. Poppler bereitet PDF-Seiten auf;
   Tesseract erkennt den Text aller Seiten in der richtigen Reihenfolge.
5. Der Text wird persistent nach `paperless.indexing.v1` publiziert.
6. Der Indexing-Worker validiert das Ergebnis und schreibt es nach Elasticsearch.

Die UUID dient auch als MinIO-Objektschlüssel und Elasticsearch-ID.
Wiederholte Verarbeitung aktualisiert denselben Eintrag. Der OCR-Worker kennt
Elasticsearch nicht; der Indexing-Worker benötigt weder MinIO noch Tesseract.

## Nachrichtenverträge

Beide Nachrichten verwenden UTF-8, AMQP-Content-Type `text/plain` und `schemaVersion=1`.
Header: `documentId`, `filename`, `originalContentType`, `schemaVersion`.

| Queue | Body |
| --- | --- |
| paperless.ocr.v5 | Kanonische Dokument-UUID; kein PDF-/Bildinhalt |
| paperless.indexing.v1 | OCR-Text; darf bei leeren Seiten leer sein |

Queues und Nachrichten sind dauerhaft. Der ursprüngliche MIME-Typ steht in
`originalContentType`. Vor einem Upgrade müssen alte `paperless.ocr.v4`-Aufträge
mit dem bisherigen Worker abgearbeitet werden; der neue Consumer erwartet Referenzen.

## Elasticsearch

Index: `paperless-documents`, angelegt beim ersten Ergebnis.

| Feld | Typ | Zweck |
| --- | --- | --- |
| id | keyword | Exakte Dokumentzuordnung |
| filename | text | Suche im Dateinamen |
| contentType | keyword | Exakter Dateityp |
| text | text | Volltextsuche im OCR-Ergebnis |

Ergebnis nach dem Upload mit der Dokument-ID abrufen:

```text
http://localhost:9200/paperless-documents/_doc/DOKUMENT-ID
```

Beispiel für eine Volltextsuche in PowerShell:

```powershell
$body = '{"query":{"match":{"text":"Rechnung"}}}'
Invoke-RestMethod -Method Post -ContentType application/json -Body $body `
  http://localhost:9200/paperless-documents/_search
```

## ELK

Elasticsearch, Logstash und Kibana verwenden Version 9.4.1. Spring Boot schreibt
ECS-JSON-Logs für REST und beide Worker in das Volume `application-logs`.
Logstash liest sie mit `logstash/pipeline/paperless.conf` und schreibt sie nach
`paperless-logs-*`. Lesepositionen liegen im Volume `logstash-data`.

In Kibana unter Discover eine Data View für `paperless-documents` ohne Zeitfeld
oder für `paperless-logs-*` mit Zeitfeld `@timestamp` anlegen.
MinIO verwendet einen per Digest fixierten Community-Mirror.

## Fehlerbehandlung

- REST meldet MinIO-Ausfälle mit HTTP 503; keine OCR-Nachricht bei fehlgeschlagener Speicherung.
- OCR bestätigt erst nach der Broker-Bestätigung des Ergebnisses.
- Indexing bestätigt erst nach erfolgreicher Elasticsearch-Indexierung.
- Beide Consumer halten höchstens eine unbestätigte Nachricht bereit und versuchen
  Fehler maximal dreimal mit wachsender Wartezeit erneut.
- Nicht verarbeitbare Nachrichten landen in `<queue>.dead`.
- Ungültige Referenzen, Metadaten und fehlerhafter UTF-8-Ergebnistext werden abgelehnt.
- OCR-Kommandos haben je zwei Minuten Zeitlimit; temporäre Dateien werden entfernt.

## Prüfung

Automatisierte Tests prüfen Reihenfolge und Abbruch bei Fehlern, Nachrichteninhalt,
MinIO-Zugriffe, OCR-Seitenreihenfolge, Mapping, stabile IDs und Timeouts.
Alle Module erzwingen mindestens 71 % Zeilenabdeckung; Befehle und Berichtspfade
stehen in der [README](../README.md).

Für eine manuelle Prüfung OCR vor einem Upload stoppen: Das Original muss bereits
in MinIO liegen und die OCR-Queue nur eine Referenz enthalten. Bei gestopptem
Indexing-Worker müssen nach dem OCR-Start die Texte in der zweiten Queue warten.
Erst der Start des Indexing-Workers erzeugt den Suchindex-Eintrag.
Die Anwendungslogs lassen sich parallel in Kibana kontrollieren.
