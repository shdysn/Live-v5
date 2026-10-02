# Fix Facebook Live Producer Preview Loading & Video Feed Delivery

Comprehensive solution plan to resolve the Facebook Live Producer preview stall where Facebook connects to the stream, displays "Loading..." or "Connecting video...", and then resets without showing the live camera feed.

---

## User Review & Critical Findings

> [!IMPORTANT]
> **Root Cause Identified**: When you start streaming, Facebook Live Producer **successfully connects** to LiveCaster's RTMPS socket, which is why Facebook displays "Loading...". However, Facebook Live Producer strictly requires **synchronized, continuous AAC audio frames alongside H.264 video frames**. Because Android's runtime `RECORD_AUDIO` permission was never prompted, the microphone recording failed silently, starving Facebook of audio packets. Without an audio track, Facebook's WebRTC/DASH player cannot generate the preview and times out after 10–15 seconds.

- **Dual Permission Request**: Request both `CAMERA` and `RECORD_AUDIO` simultaneously in `BroadcastControlScreen.kt` using `ActivityResultContracts.RequestMultiplePermissions()`.
- **Silent Audio Fallback Generator**: When the microphone is initializing, muted, or temporarily unavailable, continuously feed silent PCM frames to `AudioMediaCodecEncoder` so Facebook always receives 44.1kHz AAC packets without dropping the connection.
- **Forced IDR Keyframe on Start & Background**: Force `MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME` immediately on stream start and keep transmitting valid standby video/audio frames when the streamer switches to Chrome to view Facebook Live Producer.
- **Instant Connection Diagnostics**: Display real-time sent packet counts for both Video and Audio directly on the screen so the user can verify that both video and audio are actively pumping into Facebook.

---

## 1. Overview & Core Concept

### What Happens
1. Streamer pastes the stream key from Facebook Live Producer and taps **"Start Live"**.
2. LiveCaster prompts for both Camera and Microphone permissions if not already granted.
3. RTMPS connection is established with TLS SNI.
4. AVC Sequence Header (SPS/PPS) and AAC Sequence Header (44.1kHz Stereo) are immediately dispatched.
5. MediaCodec generates an immediate IDR keyframe and AAC audio frames.
6. If the streamer switches to Chrome to view `facebook.com/live/producer`, the background keep-alive loop continues feeding encoded frames without timing out.
7. Facebook Live Producer receives both video and audio tracks, generates the video preview immediately, and enables the blue **"Go Live"** button.

---

## 2. Technical Architecture & Ingest Pipeline

```
┌────────────────────────────────────────────────────────┐
│                   LiveCaster App                       │
│                                                        │
│  [CameraX YUV Frames]      [AudioRecord PCM / Silence] │
│           │                               │            │
│           ▼                               ▼            │
│  VideoMediaCodecEncoder         AudioMediaCodecEncoder │
│  - Forced IDR on start          - Always active 44.1k  │
│  - AVC Sequence Header          - AAC Sequence Header  │
│  - Continuous Keep-Alive        - Continuous AAC frames│
│           │                               │            │
│           └───────────────┬───────────────┘            │
│                           │                            │
│                           ▼                            │
│                  MultiRtmpDispatcher                   │
│                           │                            │
│                           ▼                            │
│                 RtmpConnection (RTMPS)                 │
│                 - TLS SNI Handshake                    │
│                 - Interleaved Video + Audio Tags       │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│            Facebook Live Producer (Edge CDN)           │
│                                                        │
│  1. Socket Connected (Handshake OK)                    │
│  2. onMetaData (Width, Height, AAC, AVC OK)            │
│  3. Video Header + Audio Header received               │
│  4. Video + Audio packets synchronized                 │
│  5. Preview appears with live video & sound!           │
│  6. Broadcaster clicks "Go Live"                       │
└────────────────────────────────────────────────────────┘
```

---

## 3. Step-by-Step Implementation Worklist

1. **`BroadcastControlScreen.kt`**:
   - Replace single camera permission launcher with `RequestMultiplePermissions()` for both `Manifest.permission.CAMERA` and `Manifest.permission.RECORD_AUDIO`.
   - Show helpful status chips: `Video: Streaming (30fps)` and `Audio: Streaming (AAC 44.1kHz)`.

2. **`AudioMediaCodecEncoder.kt`**:
   - Implement graceful fallback: if `AudioRecord` fails, lacks permission, or is in an emulator, generate silent 16-bit PCM frames to ensure the AAC encoder continuously outputs valid audio frames to Facebook.
   - Prevent any silent exceptions from stopping the audio pipeline.

3. **`VideoMediaCodecEncoder.kt`**:
   - Issue `PARAMETER_KEY_REQUEST_SYNC_FRAME` on startup and every 2 seconds to adhere to Facebook's required 2-second GOP (Group of Pictures) rule.
   - Ensure the keep-alive loop maintains seamless PTS (presentation timestamps) when switching to Chrome.

4. **`RtmpConnection.kt`**:
   - Verify that Audio (csid 4) and Video (csid 6) packets are properly flushed without socket buffer starvation.
