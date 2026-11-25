"""Exercise both queues, both workers, MinIO and ELK in the local demo stack.

Run after docker compose up -d --build. Only self-created test documents are deleted.
Requirements: pip install -r scripts/requirements-smoke.txt
"""
from pathlib import Path
import base64
import hashlib
import html
import io
import json
import os
import re
import secrets
import subprocess
import time
from datetime import datetime, timezone
from urllib.parse import parse_qs, urlparse

import requests
from minio import Minio
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / "tmp/full-pipeline-smoke.json"
BROKER = "http://localhost:15672/api"
OCR_QUEUE = "paperless.ocr.v5"
INDEX_QUEUE = "paperless.indexing.v1"
AUTH = ("paperless", "paperless")


def compose(*args):
    subprocess.run(["docker", "compose", *args], cwd=ROOT, check=True)


def wait_for(description, check, seconds=120):
    deadline = time.monotonic() + seconds
    last_error = None
    while time.monotonic() < deadline:
        try:
            result = check()
            if result:
                print("PASS:", description, flush=True)
                return result
        except (requests.RequestException, KeyError, AssertionError) as error:
            last_error = error
        time.sleep(1)
    raise AssertionError(f"Timed out: {description}; last error: {last_error}")


def broker(path, body=None):
    if body is None:
        response = requests.get(BROKER + path, auth=AUTH, timeout=10)
    else:
        response = requests.post(BROKER + path, auth=AUTH, json=body, timeout=10)
    response.raise_for_status()
    return response.json()


def login():
    session = requests.Session()
    verifier = secrets.token_urlsafe(48)
    challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).decode().rstrip("=")
    base = "http://localhost:8082/realms/paperless/protocol/openid-connect"
    response = session.get(base + "/auth", params={
        "client_id": "paperless-ui", "redirect_uri": "http://localhost:8081/",
        "response_type": "code", "scope": "openid", "code_challenge": challenge,
        "code_challenge_method": "S256",
    }, timeout=20)
    response.raise_for_status()
    action = html.unescape(re.search(r'<form[^>]+action="([^"]+)"', response.text).group(1))
    # Browsers treat localhost as secure; requests does not implement this exception.
    # This adjustment is only for this local HTTP test session, not the application.
    for cookie in session.cookies:
        cookie.secure = False
    response = session.post(action, data={"username": "user", "password": "user", "credentialId": ""},
                            allow_redirects=False, timeout=20)
    assert response.status_code == 302, "Local demo login failed"
    code = parse_qs(urlparse(response.headers["Location"]).query)["code"][0]
    response = session.post(base + "/token", data={
        "grant_type": "authorization_code", "client_id": "paperless-ui", "code": code,
        "redirect_uri": "http://localhost:8081/", "code_verifier": verifier,
    }, timeout=20)
    response.raise_for_status()
    session.headers["Authorization"] = "Bearer " + response.json()["access_token"]
    return session


def image(text):
    candidates = [os.getenv("SMOKE_FONT", ""), "C:/Windows/Fonts/arial.ttf",
                  "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]
    font = next((ImageFont.truetype(path, 64) for path in candidates if path and Path(path).is_file()), None)
    if font is None:
        raise RuntimeError("Set SMOKE_FONT to a TrueType font path")
    result = Image.new("RGB", (1500, 500), "white")
    ImageDraw.Draw(result).text((70, 100), text, font=font, fill="black")
    return result


def peek(queue, count):
    # Requeue the messages: inspecting a queue must not consume the processing jobs.
    return broker(f"/queues/%2F/{queue}/get", {
        "count": count, "ackmode": "ack_requeue_true", "encoding": "auto", "truncate": 100000,
    })


def main():
    originals = Minio("localhost:9000", access_key=os.getenv("MINIO_ROOT_USER", "paperless"),
                      secret_key=os.getenv("MINIO_ROOT_PASSWORD", "paperless-secret"), secure=False)
    session = login()
    wait_for("REST server is ready", lambda: session.get(
        "http://localhost:8080/api/v1/documents/00000000-0000-0000-0000-000000000000",
        timeout=10).status_code == 404)
    for queue in (OCR_QUEUE, INDEX_QUEUE):
        assert broker(f"/queues/%2F/{queue}")["messages"] == 0, "Drain pending work before this smoke test"
    documents = []
    result = {"tested_at": datetime.now(timezone.utc).isoformat(), "documents": documents}
    REPORT.parent.mkdir(exist_ok=True)
    compose("stop", "ocr-worker", "indexing-worker")
    try:
        pdf = io.BytesIO()
        image("First page Alpha 48271").save(pdf, format="PDF", save_all=True,
            append_images=[image("Second page Beta 59382")], resolution=150)
        png = io.BytesIO()
        image("Pipeline Image 76493").save(png, format="PNG")
        cases = [("pipeline-two-pages.pdf", "application/pdf", pdf.getvalue(),
                  ["First page Alpha 48271", "Second page Beta 59382"]),
                 ("pipeline-image.png", "image/png", png.getvalue(), ["Pipeline Image 76493"])]
        for filename, mime, data, expected in cases:
            response = session.post("http://localhost:8080/api/v1/documents",
                files={"file": (filename, data, mime)}, timeout=20)
            assert response.status_code == 201, response.text
            doc = {"id": response.json()["id"], "filename": filename, "expected": expected}
            documents.append(doc)
            REPORT.write_text(json.dumps(result, indent=2), encoding="utf-8")
            original = originals.get_object("documents", doc["id"])
            try:
                assert original.read() == data
            finally:
                original.close()
                original.release_conn()
            assert requests.get("http://localhost:9200/paperless-documents/_doc/" + doc["id"], timeout=10).status_code == 404
        print("PASS: REST stored both originals while OCR and indexing were stopped", flush=True)

        jobs = peek(OCR_QUEUE, 2)
        assert {job["payload"] for job in jobs} == {doc["id"] for doc in documents}
        assert all(job["properties"]["headers"]["schemaVersion"] == "1" for job in jobs)
        result["reference_queue_verified"] = True
        print("PASS: OCR queue contains UUID references, not PDF/image bytes", flush=True)
        compose("start", "ocr-worker")
        wait_for("OCR produced two queued results with indexing stopped",
            lambda: broker(f"/queues/%2F/{INDEX_QUEUE}")["messages_ready"] == 2)
        results = {job["properties"]["headers"]["documentId"]: job["payload"] for job in peek(INDEX_QUEUE, 2)}
        for doc in documents:
            text = results[doc["id"]]
            assert all(expected in text for expected in doc["expected"]), text
            assert [text.index(expected) for expected in doc["expected"]] == sorted(text.index(expected) for expected in doc["expected"])
            doc["recognized_text"] = text
            assert requests.get("http://localhost:9200/paperless-documents/_doc/" + doc["id"], timeout=10).status_code == 404
        result["separate_result_queue_verified"] = True
        compose("start", "indexing-worker")
        for doc in documents:
            def indexed():
                response = requests.get("http://localhost:9200/paperless-documents/_doc/" + doc["id"], timeout=10)
                response.raise_for_status()
                return response.json()["_source"]["text"] == doc["recognized_text"]
            wait_for("indexing worker stored " + doc["filename"], indexed)
            def searchable():
                response = requests.post("http://localhost:9200/paperless-documents/_search", json={
                    "query": {"bool": {"must": [{"match": {"text": doc["expected"][0]}}],
                                         "filter": [{"ids": {"values": [doc["id"]]}}]}}}, timeout=10)
                response.raise_for_status()
                return len(response.json()["hits"]["hits"]) == 1
            wait_for("full-text search finds " + doc["filename"], searchable)
        wait_for("Kibana status is available", lambda: requests.get("http://localhost:5601/api/status", timeout=10).ok, 180)
        def logs_indexed():
            response = requests.post("http://localhost:9200/paperless-logs-*/_search", json={
                "query": {"match_phrase": {"message": documents[0]["id"]}}, "size": 100}, timeout=10)
            response.raise_for_status()
            names = {hit["_source"].get("service", {}).get("name") for hit in response.json()["hits"]["hits"]}
            return {"paperless-rest", "paperless-ocr-worker", "paperless-indexing-worker"}.issubset(names)
        wait_for("Logstash indexed logs from REST and both workers", logs_indexed, 180)
        stats = requests.get("http://localhost:9600/_node/stats/pipelines/main", timeout=10)
        stats.raise_for_status()
        result["logstash_events_out"] = stats.json()["pipelines"]["main"]["events"]["out"]
        assert result["logstash_events_out"] > 0
        result["elk_verified"] = True
        result["success"] = True
    finally:
        compose("start", "ocr-worker", "indexing-worker")
        # On failure retain inputs for diagnosis; never delete originals of queued work.
        if result.get("success"):
            for doc in documents:
                response = session.delete("http://localhost:8080/api/v1/documents/" + doc["id"], timeout=10)
                assert response.status_code == 204
                originals.remove_object("documents", doc["id"])
                response = requests.delete("http://localhost:9200/paperless-documents/_doc/" + doc["id"], timeout=10)
                response.raise_for_status()
            result["test_documents_cleaned_up"] = True
        REPORT.write_text(json.dumps(result, indent=2), encoding="utf-8")
    print("PASS: complete pipeline and ELK verified; report:", REPORT, flush=True)


if __name__ == "__main__":
    main()
