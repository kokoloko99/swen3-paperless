# Sprint 4: Abgabepruefung am 25.11.2026

## Automatisierte lokale Pruefung

Aus einem frischen Checkout mit dem lokalen Abgabestand ausgefuehrt:

```powershell
.\paperless-rest\gradlew.bat -p paperless-ocr-worker clean check bootJar
.\paperless-rest\gradlew.bat -p paperless-rest clean check bootJar
```

| Modul | Tests | Fehler | JaCoCo-Zeilenabdeckung |
| --- | ---: | ---: | ---: |
| Backend | 70 | 0 | 270/311 = 86.82% |
| OCR-Worker | 24 | 0 | 100/111 = 90.09% |

Beide `check`-Tasks und beide `bootJar`-Builds erfolgreich. `check` erzwingt
mindestens 71 % Zeilenabdeckung je Modul. Keine Coverage-Ausnahmen im Worker.
JUnit-/JaCoCo-Berichte werden durch die GitHub-Actions-Pipeline auch als Artefakte bereitgestellt.
Die Pipeline prueft Worker und Backend einschliesslich PostgreSQL-Testcontainers.

## Praktischer Test mit laufendem Docker Compose

Am selben Tag vor der Release-Vorbereitung authentifizierte Uploads ueber die REST-API
mit dem bestehenden Keycloak-PKCE-Ablauf ausgefuehrt:

- PNG: `Sprint Four Invoice 48271` korrekt erkannt und indexiert.
- Zweiseitige PDF: `First page Alpha 48271` und `Second page Beta 59382`
  korrekt erkannt und in dieser Reihenfolge indexiert.
- Beide Originaldateien aus MinIO gelesen und bytegenau mit dem jeweiligen Upload verglichen.
- Beide Dokumente mit einer Elasticsearch-Volltextsuche nach `48271` gefunden.
- Worker-Logs bestaetigten fuer beide IDs `stored and indexed`.
- Testdaten anschliessend aus PostgreSQL, MinIO und Elasticsearch entfernt.

Docker war zu Beginn ausgeschaltet und wurde fuer diese Pruefung gestartet.
Kibana ist optional und war nicht Bestandteil dieses Tests.

## Umfang und Grenzen

Die vier Exercise-Punkte sind ueber Issues #20 bis #23 nachverfolgbar.
Die Abgabevorbereitung ist in #24 erfasst. Noch offene Folgeaufgaben:

- #25: koordiniertes Loeschen aus PostgreSQL, MinIO und Elasticsearch.
- #26: Such-REST-API und Suchfeld in der Weboberflaeche.

Die heutige Veroeffentlichung bildet fruehere lokale Implementierungsarbeit ab;
sie behauptet keine rueckwirkenden Feature-Branches oder menschlichen Peer-Reviews.
Ein menschliches Peer-Review wurde fuer die hier vorbereitete Abgabe nicht nachgewiesen.

## GitHub-Nachweise

- [Feature-PR #27](https://github.com/kokoloko99/swen3-paperless/pull/27): nach develop uebernommen.
- [Erfolgreicher PR-CI-Lauf](https://github.com/kokoloko99/swen3-paperless/actions/runs/45609801290): Backend und Worker erfolgreich.
- [Erfolgreicher Branch-CI-Lauf](https://github.com/kokoloko99/swen3-paperless/actions/runs/45609768445): Backend und Worker erfolgreich.
- Beide ausfuehrbaren JARs nach der Versionsanpassung auf 0.3.0 erneut erfolgreich gebaut.
