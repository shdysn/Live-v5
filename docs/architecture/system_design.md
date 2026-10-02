# LiveCaster Architecture & System Design

## Overview
LiveCaster is a production-grade live streaming studio application built using Android Jetpack Compose, Kotlin Coroutines & Flow, and Clean Architecture (MVVM).

```
+-----------------------------------------------------------+
|                   Jetpack Compose UI                     |
|  (Dashboard, BroadcastSetup, BroadcastControl, Settings)  |
+-----------------------------------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                    ViewModel Layer                        |
|    (StateFlow, UI State, Event Handling, Coroutines)      |
+-----------------------------------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                      Domain Layer                         |
|        (Use Cases, Domain Models, Repository Intfs)       |
+-----------------------------------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                       Data Layer                          |
|  - Room Local DB (Broadcasts, Destinations, Logs)         |
|  - Retrofit + OkHttp Network Remote Data Sources          |
|  - Secure Token Storage (Encrypted Preferences)           |
+-----------------------------------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                   Streaming Engine                        |
|  - CameraX Capture Manager (PreviewView, Torch, Lenses)    |
|  - MediaCodec Hardware Encoder (H.264 / AAC)              |
|  - FlvMuxer (Tag packetization, FLV Header generation)    |
|  - RtmpPublisher (Socket connection & throughput meter)   |
|  - LiveStreamingService (Foreground service + wake lock)  |
+-----------------------------------------------------------+
```

## Key Architectural Principles
1. **Clean Separation of Concerns**: Presentation (Compose), Domain (Pure Kotlin Use Cases), Data (Room & Retrofit).
2. **Foreground Streaming Resiliency**: Foreground Service with `camera|microphone` types ensures the broadcast never cuts off when the user switches apps or turns off the screen.
3. **Adaptive Ingest & Telemetry**: Dynamic FPS and bitrate monitoring with real-time health indicator.
4. **Bilingual Localization**: Native support for English and Urdu (`values-ur`).
