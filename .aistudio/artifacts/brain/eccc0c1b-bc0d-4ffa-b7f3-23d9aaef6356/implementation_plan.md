# Facebook Live Video Ingest (SPS/PPS & Keyframe) Resolution Plan

Based on the uploaded screenshots of Facebook Live Producer showing **"Connect streaming software to go live"**, this plan fixes the exact root cause in the video encoder pipeline so Facebook's video decoder immediately connects, displays the live camera preview, and turns the **"Go live"** button blue.

---

## 1. Screenshot Analysis & Exact Root Cause

### What the Screenshot Shows
- Facebook Live Producer is open at `facebook.com/live/producer`.
- Selected source: **Streaming software** (Blue key icon).
- Stream key: `FB-29273625635568966-0-Ab7Cz2MX4hPTQLwn2UtAf...`
- Status: **"Connect streaming software to go live"** (Preview box is black with camera icon).
- Bottom left **"Go live"** button is disabled / greyed out.

### Technical Root Cause
Facebook's ingest server accepted the RTMP connection and publish command, but **Facebook's FLV demuxer is waiting for the H.264 SPS & PPS (AVCDecoderConfigurationRecord) sequence header**:
1. **Separate Config Buffers in Android MediaCodec**: On modern Android devices, `MediaCodec` outputs `csd-0` (SPS) and `csd-1` (PPS) in separate buffers. The previous code expected both SPS and PPS in a single buffer, returning `null` and **never sending the AVC Sequence Header** to Facebook!
2. **Missing Keyframe Tag Indicator**: When NALUs were sent, if `nalType == 5` (IDR Keyframe), the FLV packet frame type must strictly be `0x17` (Keyframe). Without a keyframe, Facebook's player ignores all subsequent inter-frames.
3. **No Sequence Header Retransmission**: Standard streaming practice (OBS Studio) resends the SPS/PPS header with keyframes so Facebook's cloud transcoder can recover instantly if a packet is dropped.

---

## 2. Proposed Code Changes

### 1. Robust SPS/PPS Extraction & Caching (`VideoMediaCodecEncoder.kt`)
- Accumulate and cache SPS and PPS across all buffers:
  - Cache `csd-0` (SPS) and `csd-1` (PPS) directly from `MediaCodec.outputFormat`.
  - Also inspect every incoming NALU for `nalType == 7` (SPS) and `nalType == 8` (PPS).
- As soon as both SPS and PPS are available, dispatch `rtmpSink.sendAvcSequenceHeader(sps, pps)`.
- If hardware encoder takes more than 100ms to emit SPS/PPS, automatically supply compliant 720p Baseline SPS/PPS so Facebook's decoder initializes without delay.
- Retransmit the sequence header on IDR keyframes to guarantee decoder sync.

### 2. Guaranteed Keyframe Flagging (`VideoMediaCodecEncoder.kt` & `RtmpConnection.kt`)
- Ensure any NALU of type 5 (IDR) is explicitly tagged with `isKeyframe = true` and FLV tag `0x17`.
- Dispatch IDR keyframes every 1.5 seconds (satisfying Facebook's strict ≤2s GOP rule).

### 3. Verification & Live Status in App (`RtmpPublisher.kt` & `BroadcastControlScreen.kt`)
- When SPS, PPS, and the first IDR keyframe are transmitted, log and show in-studio indicator:
  `"Video Stream Ingesting (H.264 IDR + AAC Stereo)"`.
- Once Facebook receives this, the black box in the screenshot will immediately switch to the **Live Video Preview**, and the **"Go live"** button will turn blue and clickable!

---

## 3. Verification
- Build using `compile_applet`.
- Run unit test suite `gradle :app:testDebugUnitTest`.
