# Shared Memories Starter

Starter FE + BE per un'app che permette agli ospiti di un evento di caricare foto e video tramite QR code.

## Stack

- Backend: Spring Boot 3, Java 21, JPA, H2 dev/PostgreSQL prod
- Frontend: React + Vite + Axios + React Router
- Storage: modalità `local` per sviluppo, predisposizione Cloudflare R2/S3 compatible per produzione

## Avvio locale

### Backend

```bash
cd backend
mvn spring-boot:run
```

Backend su:

```text
http://localhost:8080
```

Console H2:

```text
http://localhost:8080/h2-console
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend su:

```text
http://localhost:5173
```

## Flusso MVP

1. Vai su `/admin/events`
2. Crea un evento
3. Apri il dettaglio evento
4. Scansiona il QR code oppure apri `/e/{slug}`
5. Carica foto/video
6. Vedi i contenuti nella galleria o nel pannello admin

## Endpoint principali

### Admin

```text
POST   /api/admin/events
GET    /api/admin/events
GET    /api/admin/events/{id}/media
GET    /api/admin/events/{id}/qr
```

### Pubblici

```text
GET  /api/public/events/{slug}
POST /api/public/events/{slug}/upload-url
POST /api/public/events/{slug}/media/complete
GET  /api/public/events/{slug}/gallery
```

## Storage locale

Di default il progetto salva i file in:

```text
backend/uploads
```

## Cloudflare R2

Per usare R2 modifica `application.yml`:

```yaml
app:
  storage:
    mode: r2
```

e imposta le variabili ambiente:

```bash
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_ACCESS_KEY=...
R2_SECRET_KEY=...
R2_BUCKET=shared-memories
R2_PUBLIC_BASE_URL=https://media.tuodominio.it
```

## Prossimi step consigliati

- login admin vero con JWT
- password/PIN evento
- moderazione contenuti
- download ZIP
- compressione immagini lato client
- multipart upload per video grandi
- thumbnail asincrone
