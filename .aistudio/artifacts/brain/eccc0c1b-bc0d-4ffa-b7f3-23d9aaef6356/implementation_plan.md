# Facebook & YouTube Live Stream Ingest & Transmission Fix (LiveCaster)

Comprehensive revision and execution plan to ensure the Stream Key system reliably delivers video and audio to Facebook Live and YouTube Live, resolving the issue where the app indicated "Live" but video was not appearing on Facebook.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Stream Key System Validation**: Yes, the Stream Key (RTMP/RTMPS) system is the **correct, official, and most reliable method** used worldwide (OBS Studio, Streamlabs, and Prism Live). It does not require Meta App review or developer account verification. The reason the app showed "Live" while Facebook was not displaying the broadcast was technical issues in the RTMP socket layer (TLS SNI handshake, AVC SPS/PPS sequence headers, and unverified chunk delivery).

- **Primary Broadcast Architecture**: Retain and strengthen the Stream Key / Direct RTMP Ingest pipeline for Facebook and YouTube, while keeping the Chrome OAuth option for users who want automatic channel discovery.
- **RTMPS TLS SNI Fix**: Configure SSLSocket with `SNIHostName("live-api-s.facebook.com")` so Facebook's cloud edge servers accept and route the stream instead of silently terminating the TLS session.
- **Video Packet Encoding (SPS/PPS)**: Ensure MediaCodec extracts and sends the H.264 SPS/PPS (AVCDecoderConfigurationRecord) sequence header before any video frame is pushed, which Facebook Live Producer requires to initialize its video player.
- **Real-Time Transmission Status**: Provide verified ingest telemetry (actual bytes pushed, socket state, and stream health) rather than a simulated duration counter.

---

## 1. Overview & Core Concept

### What This Solves
When a broadcaster pastes a Facebook stream key (from `facebook.com/live/producer`) and starts streaming:
1. LiveCaster establishes an authenticated, SNI-compliant TLS connection to `rtmps://live-api-s.facebook.com:443/rtmp/`.
2. It sends the RTMP handshake, chunk size, `connect`, `createStream`, and `publish` commands, waiting for server acknowledgment.
3. It sends the AVC video sequence header (SPS + PPS) and AAC audio header, followed by camera frames.
4. Facebook Live Producer immediately registers the stream, displays the incoming video preview, and activates the live broadcast.
5. In LiveCaster, a **Transmission Status Card** shows real-time ping, packets sent, and a button to view/confirm the live feed in Facebook Live Producer.

---

## 2. User Experience & Visual Design

### Broadcast Setup & Live Transmission Flow

```
┌────────────────────────────────────────────────────────┐
│              LiveCaster Setup Screen                   │
│                                                        │
│  [FB Stream Key: FB-192837465...                     ] │
│  [YT Stream Key: 1a2b-3c4d-5e6f...                   ] │
│                                                        │
│  [ Initialize Live Studio ]                            │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│              Live Broadcasting Studio                  │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Full Camera Preview                 │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  ┌─ Transmission Status ─────────────────────────────┐ │
│  │ ● Facebook: Connected (RTMPS 443) • 3.5 Mbps      │ │
│  │   [Open FB Live Producer to confirm preview ↗]    │ │
│  │ ● YouTube: Connected (RTMP 1935)  • 3.5 Mbps      │ │
│  └───────────────────────────────────────────────────┘ │
│                                                        │
│  [ Mute Mic ]  [ Flip Camera ]  [ Flash ]  [ End Live ]│
└────────────────────────────────────────────────────────┘
```

1. **Setup Screen**:
   - Facebook and YouTube stream keys can be pasted or retrieved from Connect Accounts.
   - An instant **"Test Connection"** ping button verifies that `live-api-s.facebook.com:443` is reachable.
2. **Live Studio**:
   - The status changes to **"Connecting..."** while the RTMP handshake and stream negotiation take place.
   - Once Facebook and YouTube acknowledge the `publish` command and receive the SPS/PPS headers, the status switches to **"● Live Transmission Verified"**.
   - If Facebook closes the socket or rejects the key, a clear error banner appears: *"Facebook rejected stream key. Please verify key in FB Live Producer"*.
   - A convenient button **"Open FB Live Producer in Chrome ↗"** allows the user to see their live preview on Facebook and ensure it is broadcasting to viewers.

---

## 3. Key Technical Fixes & Decisions

### Fix 1: TLS SNI (Server Name Indication) on Android SSLSocket
- **Problem**: Meta's servers host thousands of domains behind shared cloud IPs. When `SSLSocket` connects without SNI, the server does not know which TLS certificate to present and terminates the connection.
- **Solution**:
  ```kotlin
  val ssl = factory.createSocket(host, port) as SSLSocket
  val params = ssl.sslParameters
  params.serverNames = listOf(SNIHostName(host))
  ssl.sslParameters = params
  ssl.startHandshake()
  ```

### Fix 2: Proper H.264 SPS / PPS Header Transmission
- **Problem**: Facebook's ingest servers drop streams that send raw NALUs without an AVC Decoder Configuration Record (Type 0 packet containing SPS & PPS).
- **Solution**:
  In `VideoMediaCodecEncoder`, extract `csd-0` (SPS) and `csd-1` (PPS) from MediaCodec's `INFO_OUTPUT_FORMAT_CHANGED` and send a Type 0 AVC packet (`AVCDecoderConfigurationRecord`) before transmitting IDR keyframes.

### Fix 3: Verified Telemetry vs. Blind Timer
- **Problem**: The app's timer started counting duration even if the socket was dropping packets or waiting for reconnection.
- **Solution**:
  Tie `StreamStatus.LIVE` and duration incrementing strictly to active socket writes and acknowledgment from the RTMP server. If writes fail, transition to `StreamStatus.RECONNECTING` or `StreamStatus.ERROR` with actionable error messages.

---

## 4. Technical Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       LiveCaster Studio                         │
│                                                                 │
│   CameraX Video Frames                 AudioRecord PCM Bytes    │
│           │                                      │              │
│           ▼                                      ▼              │
│  VideoMediaCodecEncoder                AudioMediaCodecEncoder   │
│  - Extracts SPS/PPS (csd-0, csd-1)     - AAC-LC format          │
│  - H.264 NALUs (IDR & Non-IDR)         - AAC sequence header    │
│           │                                      │              │
│           └──────────────────┬───────────────────┘              │
│                              │                                  │
│                              ▼                                  │
│                     MultiRtmpDispatcher                         │
│                              │                                  │
│               ┌──────────────┴──────────────┐                   │
│               ▼                             ▼                   │
│       RtmpConnection (FB)           RtmpConnection (YT)         │
│       - TLS 1.3 with SNI            - TCP Socket                │
│       - Port 443 (RTMPS)            - Port 1935 (RTMP)          │
│       - Handshake C0/C1/C2          - Handshake C0/C1/C2        │
│       - Send AVC SPS/PPS            - Send AVC SPS/PPS          │
└───────────────┬─────────────────────────────┬───────────────────┘
                │                             │
                ▼                             ▼
┌──────────────────────────────┐┌─────────────────────────────────┐
│     Facebook Live Ingest     ││       YouTube Live Ingest       │
│ rtmps://live-api-s.facebook  ││ rtmp://a.rtmp.youtube.com/live2 │
│   .com:443/rtmp/{FB_KEY}     ││   /{YT_KEY}                     │
│                              ││                                 │
│  - Video Preview Shows UP    ││  - Video Preview Shows UP       │
│  - Live Broadcast to Viewers ││  - Live Broadcast to Viewers    │
└──────────────────────────────┘└─────────────────────────────────┘
```

---

## 5. Step-by-Step Implementation Worklist

1. **`RtmpConnection.kt`**:
   - Update `connect()` to properly configure `SNIHostName` on `SSLSocket` for all `rtmps://` endpoints.
   - Refactor `sendConnect()`, `sendCreateStream()`, and `sendPublish()` to handle server `_result` responses.
   - Support `sendAvcSequenceHeader(sps: ByteArray, pps: ByteArray)` before IDR keyframes.
2. **`VideoMediaCodecEncoder.kt`**:
   - Capture `csd-0` (SPS) and `csd-1` (PPS) from `MediaFormat` on codec startup and pass them to `rtmpSink.sendAvcSequenceHeader(...)`.
3. **`RtmpPublisher.kt`**:
   - Verify active socket status before marking stream as `LIVE`.
   - Update `StreamTelemetry` with real byte count and ping.
4. **`BroadcastSetupScreen.kt` & `BroadcastControlScreen.kt`**:
   - Add explicit guidance for Facebook Live Producer: reminder to check video preview in Facebook and click "Go Live" if not set to automatic.
   - Add a quick shortcut in the studio control bar: **"Check FB Live Preview ↗"**.
