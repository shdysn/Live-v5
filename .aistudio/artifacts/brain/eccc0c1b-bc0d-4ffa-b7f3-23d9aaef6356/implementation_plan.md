# 100% Native No-Browser Facebook Live Publishing Plan

## 1. Problem Solved
Mobile browsers and embedded WebViews fail on Android because:
- Facebook Live Producer blocks mobile devices and throws desktop WebRTC camera/audio permission errors.
- External browsers disconnect or crash in the background.

## 2. The Pure Native Solution (0 Browser, 0 Permission Issues)
We will use **Facebook's Official Graph API (`/me/live_videos` & `/{page_id}/live_videos`)**:
- When you tap **"Start Live"**, LiveCaster makes a direct background API call to Facebook.
- Facebook instantly generates a live post on your **Profile feed** or **Page feed** with `status: LIVE_NOW`.
- LiveCaster streams your phone camera and microphone directly to that stream.
- **Your live video appears on your Facebook mobile app feed immediately without touching any browser!**

---

## 3. Architecture & Features

### 1. Destination Selector: Profile vs Page
- In `BroadcastSetupScreen.kt` and `ConnectAccountsScreen.kt`:
  - Choose destination: **👤 My Profile Feed** OR **📄 Facebook Page**.
  - No stream keys to manually copy/paste! LiveCaster fetches and binds the key automatically via API.

### 2. Direct Facebook Token Integration
- In `ConnectAccountsScreen.kt`:
  - Enter or paste your Facebook Access Token once.
  - LiveCaster securely saves it in `SecureTokenStorage`.
  - Automatically loads your Profile info and all your Facebook Pages.

### 3. Native Live Lifecycle (Create & End)
- **On Start**: LiveCaster calls `POST /me/live_videos` (or `/{page_id}/live_videos`) with `status=LIVE_NOW`.
- **On Broadcast**: Video & audio stream directly to Facebook's RTMP ingest server.
- **On End**: LiveCaster calls `POST /{live_video_id}?end_live_video=true` to save the live replay video on Facebook.

### 4. Complete Removal of Browser/WebView
- Completely remove `FacebookGoLiveSheet.kt` and all WebView/browser buttons from the Live Studio.
- The Live Studio remains 100% focused on your camera, audio, filters, and live telemetry.

---

## 4. Implementation Steps
1. **Extend `FacebookApiService.kt`**:
   - Add `createProfileLiveVideo` (`POST me/live_videos`) with `status=LIVE_NOW`.
   - Add `createPageLiveVideo` (`POST {page_id}/live_videos`) with `status=LIVE_NOW`.
   - Add `endLiveVideo` (`POST {live_video_id}?end_live_video=true`).
2. **Update `FacebookRepositoryImpl.kt`**:
   - Implement native live stream creation for both Profile and Pages.
   - Automatically retrieve and return the `secure_stream_url` and stream key.
3. **Update `BroadcastSetupScreen.kt` & `BroadcastSetupViewModel.kt`**:
   - Add a toggle: **"Stream to: My Profile | Facebook Page"**.
   - If token is present, automatically initialize the live video without manual keys.
4. **Clean up `BroadcastControlScreen.kt`**:
   - Remove `FacebookGoLiveSheet` and all browser buttons.
5. **Verification**:
   - Compile with `compile_applet` and run unit tests.
