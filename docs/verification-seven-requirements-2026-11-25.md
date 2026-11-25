# Pruefung aller sieben Sprint-4-Anforderungen am 25.11.2026

Dieser Bericht bewertet die vervollstaendigte Abgabe. Der fruehere Bericht
`verification-2026-11-25.md` beschreibt den zuerst veroeffentlichten Vier-Punkte-Stand.
Auf ausdruecklichen Wunsch wird derselbe Tag/Release **v0.3.0** aktualisiert;
es wird kein neuer Tag erstellt. Der vorherige Ziel-Commit
`e8683a3fa015598e0982b85d255340202627323b` bleibt in der Historie.

## Automatisierte Pruefung

Alle drei lokalen `check`-Tasks erfolgreich. Insgesamt **110 Tests**, keine Fehler.
Jedes gesamte Java-Modul erzwingt mindestens 71 % JaCoCo-Zeilenabdeckung.
In den Worker-Modulen sind keine Klassen ausgeschlossen.

| Modul | Tests | Abgedeckte Zeilen |
| --- | ---: | ---: |
| REST | 75 | 291/334 = 87,13 % |
| OCR-Worker | 24 | 99/105 = 94,29 % |
| Indexing-Worker | 11 | 37/42 = 88,10 % |

Geprueft werden Speicherreihenfolge und HTTP 503 bei MinIO-Fehlern,
Referenznachrichten, MinIO-Download und Stream-Schliessung, OCR-Reihenfolge,
Ergebnispublikation mit Broker-Bestaetigung, separate Indexierung, explizites
Mapping, stabile IDs, ungueltige Metadaten/UTF-8, Timeouts und temporaere Dateien.

GitHub Actions prueft ebenfalls alle drei Module. Der erste Feature-PR-Lauf
[45654198341](https://github.com/kokoloko99/swen3-paperless/actions/runs/45654198341)
war erfolgreich. Weitere CI-Laeufe und Berichte sind am aktualisierten Release verlinkt.

## Echter Docker-Test mit getrennten Verarbeitungsschritten

Ausgefuehrt: `python scripts/smoke_pipeline.py` nach Build und Start der Container.
Das Skript kontrolliert leere Verarbeitungsqueues und legt nur eigene Testdokumente an.

1. OCR- und Indexing-Worker gestoppt.
2. Authentifizierter PDF-/PNG-Upload ueber REST und Keycloak-PKCE: jeweils HTTP 201.
3. Beide Originale bereits jetzt aus MinIO heruntergeladen und bytegenau verglichen.
4. Erste Queue kontrolliert: UUID-Referenzen und Metadaten, keine PDF-/Bildbytes.
5. Nur OCR gestartet: Originale aus MinIO geladen, beide Texte in der Ergebnisqueue.
   Der Indexing-Worker bleibt gestoppt; noch keine Elasticsearch-Eintraege vorhanden.
6. Erst jetzt Indexing gestartet: Beide Ergebnisse gespeichert und per Volltextsuche
   mit Filter auf ihre jeweilige UUID gefunden.
7. Kibana-Status API verfuegbar. Logs zur PDF-Dokument-ID von REST, OCR und Indexing
   im Logindex gefunden; Logstash-Ausgangszaehler bestaetigt uebertragene Ereignisse.
8. Beide Testdokumente aus PostgreSQL, MinIO und Elasticsearch entfernt.
   Beide Worker abschliessend wieder gestartet.

Erkannte Texte:

```text
PDF Seite 1: First page Alpha 48271
PDF Seite 2: Second page Beta 59382
PNG: Pipeline Image 76493
```

Testbeginn: `2026-11-25T16:37:41.902565+00:00` (UTC).
Logstash-Ausgangszaehler bei der Pruefung: **138 Events**.
Eigene Test-IDs: `bcc62649-3eda-4c3f-83cd-ab57d2c6ae7d` (PDF),
`a8284514-1c25-4dfb-b90a-191df41abffc` (PNG).
Das maschinenlesbare Ergebnis wird den Testberichten im Release beigelegt.

Alle elf Dienste laufen: PostgreSQL, RabbitMQ, MinIO, Keycloak, REST, UI,
OCR-Worker, Indexing-Worker, Elasticsearch, Logstash und Kibana.
Die alte Binaer-Queue `paperless.ocr.v4` war vor dem Upgrade leer.

## Nachweis der sieben Vorgaben

| Nr. | Nachweis |
| --- | --- |
| 1 | Zwei eigenstaendige Java-Projekte, Anwendungen und laufende Worker-Container |
| 2 | Zweiseitige PDF mit Poppler/Tesseract vollstaendig und in korrekter Reihenfolge erkannt |
| 3 | Bytegleiche MinIO-Originale bei gestoppten Workern direkt nach REST-Upload |
| 4 | UUID-Auftraege aus erster Queue; MinIO-Download und OCR bei gestopptem Indexer |
| 5 | Offizieller Elasticsearch-Java-Client im separaten Indexing-Worker |
| 6 | Wartende OCR-Texte in Ergebnisqueue; Speicherung erst nach Start des Indexing-Workers |
| 7 | Vollstaendiges ELK: Kibana verfuegbar, Logs aller Anwendungen durch Logstash in Elasticsearch |

Die technische Umsetzung und Wiederholung sind in [Sprint 4](sprint-4.md) beschrieben.
Die koordinierte MinIO-/Elasticsearch-Loeschung (#25), Suchoberflaeche (#26) und
ein Outbox-Verfahren bleiben offene Folgeaufgaben. Ein menschliches Peer-Review
wurde nicht durchgefuehrt oder behauptet.
