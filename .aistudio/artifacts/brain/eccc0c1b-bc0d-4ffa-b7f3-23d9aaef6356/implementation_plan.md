# Verified Live Ingest & 15-Second Connection Confirmation Plan

Plan to ensure the app **never shows LIVE** until Facebook or YouTube has actually received, accepted, and verified the live stream feed.

---

## User Review & Clarifications Addressed

> [!IMPORTANT]
> **Core Rule Enforced**:
> The app will **NO LONGER** transition to "LIVE" (green badge + duration timer) when the user taps "Start Live".
> Instead:
> 1. It enters **`CONNECTING (15s...)`** with an active verification countdown spinner.
> 2. The app negotiates the RTMPS connection with Facebook/YouTube:
>    - Uses the exact valid `tcUrl = "rtmps://live-api-s.facebook.com:443/rtmp"` (guaranteeing the `/rtmp` app path is never stripped).
>    - Sends `@setDataFrame onMetaData` formatted as an **AMF0 ECMA Array (0x08)**.
>    - Synchronously handles `createStream` and `publish`.
>    - Reads incoming server control packets looking for `NetStream.Publish.Start`.
> 3. **Server Verification**:
>    - **Only when the server confirms `NetStream.Publish.Start`** does the app switch to **`LIVE`** (Green badge, duration timer starts counting up from 00:00).
> 4. **Failure / Timeout Protection**:
>    - If the 15-second countdown finishes or if Facebook rejects the stream key (`NetStream.Publish.BadName`), the app aborts the stream, sets status to `ERROR`, and tells the user:
>      *"Stream could not be verified on Facebook. Please check that your Facebook Live Producer Stream Key is active and has not expired."*
>    - The app will **never** falsely report that it is Live when it is not running on Facebook.

---

## 1. Visual & State Lifecycle

```
[User taps "Start Live"]
         │
         ▼
[Status: CONNECTING]
- Amber Badge: "CONNECTING (15s)"
- Countdown decreases: 15s... 14s... 13s...
- Video and Audio pipelines initialized
- RTMPS Handshake & publish sent to Facebook
         │
         ├──────────────────────────────────────────────┐
         │                                              │
         ▼ (Server responds NetStream.Publish.Start)    ▼ (Timeout at 0s OR Server rejects key)
[Status: LIVE]                                 [Status: ERROR / STOPPED]
- Green Badge: "LIVE"                          - Red Alert: "Facebook verification failed"
- Duration Timer: 00:01, 00:02...              - Clear guidance to verify stream key in FB
- Real-time bitrates & FPS active              - Button resets to "Start Live"
- Broadcast running on Facebook Live Producer
```

---

## 2. Technical Modifications

### 1. `RtmpConnection.kt`
- **Fix `tcUrl`**:
  Format `tcUrl` as `rtmps://live-api-s.facebook.com:443/rtmp` without stripping `/rtmp`.
- **AMF0 ECMA Array**:
  Format `@setDataFrame onMetaData` with `writeEcmaArray(meta)` on `streamId = 1`.
- **Server Confirmation Listener**:
  Parse incoming server RTMP messages (`_result`, `onStatus`, `NetStream.Publish.Start`, `NetStream.Publish.BadName`).
  Notify the publisher immediately when `NetStream.Publish.Start` is received.

### 2. `RtmpPublisher.kt`
- Refactor status transition:
  - When publishing starts, set `status = StreamStatus.CONNECTING`.
  - Start a 15-second countdown timer.
  - When `RtmpConnection` reports verified `Publish.Start` from Facebook, switch `status = StreamStatus.LIVE` and begin duration counting.
  - If 15 seconds elapse without server confirmation or if an error is returned, set `status = StreamStatus.ERROR` with a helpful explanation.

### 3. `BroadcastControlScreen.kt`
- Update HUD badge to display:
  - In `CONNECTING`: Amber pill with animated spinner and `CONNECTING (Xs)`.
  - In `LIVE`: Red/Green pill with `LIVE` and timer.
- Display a prominent error banner if verification fails so the user can easily re-check their Facebook Live Producer tab.

---

## 3. Verification Plan
- Build and compile using `compile_applet`.
- Execute local unit tests to ensure all state machines and timers pass.
