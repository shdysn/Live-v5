# LiveCaster Setup & Quick Start Guide

## Android Application Setup
1. Open the project in Android Studio or AI Studio.
2. Build the project:
   ```bash
   gradle assembleDebug
   ```
3. Run on a physical Android device or streaming emulator.
4. When prompted, grant Camera and Audio recording permissions.

## Setting Up Broadcast Destinations
- **Facebook Live**:
  1. Open the "Facebook" tab.
  2. Tap "+ Link Facebook Page" and provide Page ID and token (or default stream key).
  3. Select your page and tap "Stream to this Page".
- **YouTube Live**:
  1. Open the "YouTube" tab.
  2. Tap "+ Link YouTube Channel".
  3. Enter channel title and stream key.
- **Custom RTMP / Relay**:
  1. In "New Stream", select "Custom RTMP".
  2. Enter the server endpoint (e.g., `rtmp://your-media-server/live`) and key.
  3. Tap "Initialize Live Studio" and then "Start Live".

## Backend Relay Server (Optional)
```bash
cd backend
npm install
npm start
```
Default port is `8080`.
