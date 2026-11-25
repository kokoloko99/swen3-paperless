# Verwendet NGINX als Webserver für die Oberfläche.
FROM nginx:1.27-alpine
# Übernimmt die Weiterleitung der API-Anfragen.
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Kopiert HTML, CSS und JavaScript in den Webserver.
COPY html /usr/share/nginx/html