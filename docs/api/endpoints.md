# LiveCaster REST & RTMP API Reference

## Authentication Endpoints
- `POST /v1/auth/login`: Authenticate producer and retrieve session JWT.
- `GET /v1/auth/me`: Validate token and fetch producer profile.

## Broadcast Management
- `GET /v1/broadcasts`: Fetch all recorded broadcast sessions and active live sessions.
- `POST /v1/broadcasts`: Initialize a new broadcast ingest pipeline with target RTMP parameters.
- `PATCH /v1/broadcasts/:id/status`: Update stream state (`DRAFT`, `LIVE`, `ENDED`, `FAILED`).
- `DELETE /v1/broadcasts/:id`: Archive and remove broadcast history log.

## Platform Ingest Integrations
- `GET /v1/facebook/pages`: Fetch Facebook pages connected under Graph API.
- `POST /v1/facebook/live_videos`: Request live video ingest URL and stream key from Facebook.
- `GET /v1/youtube/channels`: Fetch YouTube Live channels.
- `POST /v1/youtube/live_broadcasts`: Request YouTube Live RTMP stream ingestion point.

## Webhooks
- `POST /v1/webhooks/rtmp`: Notified by RTMP media server on connect, publish, and disconnect events.
