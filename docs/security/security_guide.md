# LiveCaster Security & Compliance Guide

## Credential Protection
- Stream keys and OAuth tokens are stored inside `SecureTokenStorage` with private Android context protection.
- In-flight communication uses TLS 1.3 for API endpoints and RTMPS (port 443) for Facebook and secure ingest endpoints.

## Permissions & Google Play Policy
- **Camera & Microphone**: Only requested when the user navigates to broadcasting workflows.
- **Foreground Service**: Declared with explicit `camera` and `microphone` types in compliance with Android 14+ requirements.
- **Wakelock**: Controlled strictly during live stream sessions and released immediately on stream teardown.
