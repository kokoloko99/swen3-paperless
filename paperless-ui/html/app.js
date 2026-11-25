// Die relative API-Adresse wird von NGINX an das Backend weitergeleitet.
const API = '/api/v1/documents';
const STORAGE_KEY = 'paperless-document-ids';

// Legt die Adresse und die Browser-Anwendung für den Keycloak-Login fest.
const AUTH = 'http://localhost:8082/realms/paperless/protocol/openid-connect';
const CLIENT_ID = 'paperless-ui';
const REDIRECT_URI = window.location.origin + '/';
const TOKEN_KEY = 'paperless-token';

// Wandelt Bytes in eine Zeichenfolge um, die sicher in eine URL passt.
function base64Url(bytes) {
    return btoa(String.fromCharCode(...new Uint8Array(bytes)))
        .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

// Erzeugt einen zufälligen Wert für die Absicherung des Logins.
function randomString() {
    return base64Url(crypto.getRandomValues(new Uint8Array(32)));
}

// Liest den gespeicherten Token nur dann aus, wenn er noch gültig ist.
function getToken() {
    const stored = JSON.parse(localStorage.getItem(TOKEN_KEY) || 'null');
    return stored && stored.expiresAt > Date.now() ? stored : null;
}

// Bereitet den geschützten Login mit PKCE vor und öffnet die Keycloak-Anmeldeseite.
async function login() {
    const verifier = randomString();
    const state = randomString();
    sessionStorage.setItem('pkce-verifier', verifier);
    sessionStorage.setItem('pkce-state', state);
    const challenge = base64Url(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier)));
    const params = new URLSearchParams({
        client_id: CLIENT_ID,
        response_type: 'code',
        scope: 'openid',
        redirect_uri: REDIRECT_URI,
        code_challenge: challenge,
        code_challenge_method: 'S256',
        state,
    });
    window.location.assign(`${AUTH}/auth?${params}`);
}

// Prüft die Login-Rückkehr und tauscht den Anmeldecode gegen Tokens aus.
async function handleLoginCallback() {
    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    if (!code) {
        return;
    }
    const valid = params.get('state') === sessionStorage.getItem('pkce-state');
    const verifier = sessionStorage.getItem('pkce-verifier');
    window.history.replaceState({}, '', REDIRECT_URI);
    if (!valid || !verifier) {
        return;
    }
    const response = await fetch(`${AUTH}/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            client_id: CLIENT_ID,
            code,
            redirect_uri: REDIRECT_URI,
            code_verifier: verifier,
        }),
    });
    if (response.ok) {
        const data = await response.json();
        localStorage.setItem(TOKEN_KEY, JSON.stringify({
            accessToken: data.access_token,
            idToken: data.id_token,
            expiresAt: Date.now() + (data.expires_in - 10) * 1000,
        }));
    }
}

// Entfernt den lokalen Token und meldet den Benutzer bei Keycloak ab.
function logout() {
    const token = JSON.parse(localStorage.getItem(TOKEN_KEY) || 'null');
    localStorage.removeItem(TOKEN_KEY);
    const params = new URLSearchParams({ client_id: CLIENT_ID, post_logout_redirect_uri: REDIRECT_URI });
    if (token && token.idToken) {
        params.set('id_token_hint', token.idToken);
    }
    window.location.assign(`${AUTH.replace('/protocol/openid-connect', '')}/protocol/openid-connect/logout?${params}`);
}
// Sucht ein HTML-Element über seine ID.
const $ = (id) => document.getElementById(id);
let currentId = null;

// Zeigt eine Erfolgs- oder Fehlermeldung auf der Oberfläche an.
function showMessage(text, isError) {
    const el = $('message');
    el.textContent = text;
    el.className = isError ? 'error' : 'ok';
}

// Ruft die API mit dem Bearer-Token auf und meldet fehlgeschlagene Anfragen.
async function request(url, options = {}) {
    const token = getToken();
    if (!token) {
        await login();
        throw new Error('Bitte anmelden.');
    }
    const headers = { ...options.headers, Authorization: 'Bearer ' + token.accessToken };
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        localStorage.removeItem(TOKEN_KEY);
        await login();
        throw new Error('Sitzung abgelaufen.');
    }
    if (!response.ok) {
        const body = await response.text();
        throw new Error(body || `Fehler ${response.status}`);
    }
    return response;
}

// Liest die gemerkten Dokumente aus dem Speicher dieses Browsers.
function loadIds() {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
        .map((entry) => typeof entry === 'string' ? { id: entry, filename: '' } : entry)
        .filter((document) => document && typeof document.id === 'string');
}

// Merkt sich ID und Dateiname eines Dokuments und aktualisiert die Liste.
function rememberId(id, filename) {
    const ids = loadIds().filter((document) => document.id !== id);
    ids.unshift({ id, filename });
    localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
    renderList();
}

// Erstellt die sichtbare Dokumentliste mit anklickbaren Einträgen.
function renderList() {
    const list = $('document-list');
    list.replaceChildren();
    for (const { id, filename } of loadIds()) {
        const li = document.createElement('li');
        const link = document.createElement('a');
        link.textContent = filename || `Dokument ${id.slice(0, 8)}…`;
        link.title = `ID: ${id}`;
        link.addEventListener('click', () => openDocument(id));
        li.append(link);
        list.append(li);
    }
}

// Lädt fehlende Dateinamen für bereits gemerkte Dokumente nach.
async function loadDocumentNames() {
    for (const { id } of loadIds().filter((document) => !document.filename)) {
        try {
            const doc = await (await request(`${API}/${encodeURIComponent(id)}`)).json();
            rememberId(doc.id, doc.filename);
        } catch (e) {
            console.warn(`Could not load document ${id}`, e);
        }
    }
}

// Lädt Dokumentdaten und Kommentare und zeigt sie oder eine Fehlermeldung an.
async function openDocument(id) {
    try {
        const doc = await (await request(`${API}/${encodeURIComponent(id)}`)).json();
        currentId = doc.id;
        rememberId(doc.id, doc.filename);
        renderMeta(doc);
        $('detail').hidden = false;
        await loadComments();
        showMessage('', false);
    } catch (e) {
        $('detail').hidden = true;
        showMessage(e.message, true);
    }
}

// Schreibt die Metadaten des Dokuments in den Detailbereich.
function renderMeta(doc) {
    const meta = $('meta');
    meta.replaceChildren();
    const rows = [
        ['ID', doc.id],
        ['Dateiname', doc.filename],
        ['Typ', doc.contentType],
        ['Größe', `${doc.size} Bytes`],
        ['Hochgeladen', new Date(doc.uploadedAt).toLocaleString()],
    ];
    for (const [label, value] of rows) {
        const dt = document.createElement('dt');
        dt.textContent = label;
        const dd = document.createElement('dd');
        dd.textContent = value;
        meta.append(dt, dd);
    }
}

// Lädt die Kommentare und zeigt sie mit ihren Löschbuttons an.
async function loadComments() {
    const comments = await (await request(`${API}/${currentId}/comments`)).json();
    const list = $('comments');
    list.replaceChildren();
    for (const c of comments) {
        const li = document.createElement('li');
        const span = document.createElement('span');
        span.textContent = `${c.author}: ${c.text}`;
        const del = document.createElement('button');
        del.textContent = 'Löschen';
        del.className = 'danger';
        del.addEventListener('click', () => deleteComment(c.id));
        li.append(span, del);
        list.append(li);
    }
}

// Löscht den Kommentar über die API und lädt danach die Liste neu.
async function deleteComment(commentId) {
    try {
        await request(`${API}/${currentId}/comments/${commentId}`, { method: 'DELETE' });
        await loadComments();
    } catch (e) {
        showMessage(e.message, true);
    }
}

// Löscht das Dokument samt Kommentaren und entfernt es aus der gemerkten Liste.
$('delete-document').addEventListener('click', async () => {
    const id = currentId;
    if (!id || !window.confirm('Dokument und alle zugehörigen Kommentare wirklich löschen?')) {
        return;
    }
    const button = $('delete-document');
    button.disabled = true;
    try {
        await request(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
        localStorage.setItem(STORAGE_KEY, JSON.stringify(loadIds().filter((doc) => doc.id !== id)));
        if (currentId === id) {
            currentId = null;
            $('detail').hidden = true;
            $('comments').replaceChildren();
            $('meta').replaceChildren();
        }
        renderList();
        showMessage('Dokument und zugehörige Kommentare gelöscht.', false);
    } catch (e) {
        showMessage(e.message, true);
    } finally {
        button.disabled = false;
    }
});

// Sendet die ausgewählte Datei als FormData an die Upload-API.
$('upload-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData();
    formData.append('file', $('file').files[0]);
    try {
        const doc = await (await request(API, { method: 'POST', body: formData })).json();
        event.target.reset();
        showMessage('Dokument hochgeladen.', false);
        await openDocument(doc.id);
    } catch (e) {
        showMessage(e.message, true);
    }
});

// Öffnet das Dokument mit der eingegebenen ID.
$('lookup-form').addEventListener('submit', (event) => {
    event.preventDefault();
    openDocument($('lookup-id').value.trim());
});

// Sendet Autor und Kommentartext als JSON an die API.
$('comment-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        await request(`${API}/${currentId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ author: $('author').value, text: $('text').value }),
        });
        $('text').value = '';
        await loadComments();
        showMessage('Kommentar hinzugefügt.', false);
    } catch (e) {
        showMessage(e.message, true);
    }
});

// Verbindet den Abmeldebutton mit der Logout-Funktion.
$('logout').addEventListener('click', logout);

// Verarbeitet beim Start den Login und lädt danach die Oberfläche.
(async () => {
    await handleLoginCallback();
    if (!getToken()) {
        await login();
        return;
    }
    $('app').hidden = false;
    renderList();
    await loadDocumentNames();
})();
