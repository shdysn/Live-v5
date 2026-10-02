# In-App Floating "Go Live" Sheet & Persistent Session Plan

## 1. Goal
Provide a seamless, in-app mobile experience to publish your Facebook Live stream to your profile timeline without switching to an external browser app.

---

## 2. Architecture & How It Works

### 1. In-App Floating Bottom Sheet (`FacebookGoLiveSheet.kt`)
- Integrated directly inside the Studio screen (`BroadcastControlScreen.kt`).
- Contains a native Android `WebView` configured with:
  - Persistent cookie and DOM storage (`CookieManager.getInstance().setAcceptCookie(true)`).
  - Desktop/Mobile adaptive viewport for Facebook Live Producer (`https://www.facebook.com/live/producer/v2/`).
- **Once you log in, your Facebook session is saved permanently**.

### 2. Streamlined 1-Tap Broadcast Flow
1. **Tap "Start Live" in LiveCaster**:
   - Camera and microphone encoders start transmitting 720p H.264 video + AAC audio to Facebook's RTMP server.
2. **Instant Feed Connection**:
   - Facebook immediately receives the video signal.
   - The in-app floating sheet shows your video preview ready.
3. **1-Tap "Go Live"**:
   - Tap the blue **"Go live"** button directly inside the sheet.
   - LiveCaster detects that the broadcast is published and minimizes the sheet back into the studio.
   - You are now live on your Facebook Profile timeline!
4. **Studio Control**:
   - All studio controls (camera flip, mic mute, flashlight, live duration, bitrate) remain active and responsive on screen.

---

## 3. Implementation Steps
1. **Build `FacebookGoLiveSheet.kt`**:
   - Embedded Android `WebView` composable inside a draggable bottom sheet with cookie persistence and reload/minimize controls.
2. **Integrate with `BroadcastControlScreen.kt`**:
   - Add a "Publish to Feed" action that automatically presents the sheet as soon as the RTMP ingest is active.
   - Add a quick toggle so you can re-open or minimize the sheet at any time during the broadcast.
3. **Session Persistence**:
   - Ensure `CookieManager` syncs all session cookies across app restarts so you only log in once.
4. **Verification**:
   - Verify compilation with `compile_applet`.
   - Run unit tests with `gradle :app:testDebugUnitTest`.
