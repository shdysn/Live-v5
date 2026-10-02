# LiveCaster: Multi-Platform Live Streaming Studio for Android 📡🇵🇰

LiveCaster is an enterprise-grade live broadcasting and studio management mobile application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room Database**, **CameraX**, and **Clean Architecture (MVVM)**.

---

## 🌟 Key Capabilities

- **Multi-Platform Live Streaming**: Direct broadcast ingest to Facebook Live Pages, YouTube Live Studio, Custom RTMP servers, and simultaneous multi-destination relays.
- **Hardware-Accelerated Camera Studio**: Live CameraX preview with front/back camera lens hot-switching, flashlight torch toggle, stereo audio VU meter, and lower-third graphics overlay.
- **Real-Time Stream Telemetry**: Live bitrates (Mbps), real-time FPS counter, dropped frame detection, network health status (Excellent/Good/Poor), active viewer counters, and stream duration timer.
- **Background Foreground Service**: Uninterrupted live broadcasting via Android Foreground Service (`camera|microphone`) with wake lock and system status notification.
- **Clean Architecture & Room DB**: Offline-capable local persistence for broadcasts, destinations, and telemetry logs.
- **Bilingual Support**: Fully localized in English and Urdu (`values-ur`) for Pakistani broadcasters and media creators.
- **Backend & Docs Suite**: Complete mock relay Node.js architecture in `backend/`, comprehensive architectural documentation in `docs/`, and CI/CD GitHub Actions workflow.

---

## 📱 Architecture Diagram

```
LiveCaster App
├── core/
│   ├── common/         -> Resource<T>, Result state
│   ├── constants/      -> StreamConstants, AppConstants
│   ├── di/             -> AppContainer manual dependency injection
│   ├── network/        -> Retrofit, OkHttp, AuthInterceptor, DTOs
│   ├── security/       -> SecureTokenStorage
│   ├── storage/        -> Room Database, BroadcastDao, DestinationDao, Entities
│   └── util/           -> Formatters, PermissionHelper
├── auth/               -> Producer Login, Session management
├── dashboard/          -> Stream metrics, session history, quick actions
├── facebook/           -> Facebook Graph API, Page management, live stream creation
├── youtube/            -> YouTube v3 API, Channel ingest, live broadcasts
├── broadcast/          -> Session setup, live studio controls, chat simulation
├── streaming/          -> CameraCaptureManager, FLV Muxer, RTMP publisher, Foreground Service
└── settings/           -> Codec profile (1080p/720p/480p, 60fps), ABR, Urdu localization
```

---

## 🛠️ Tech Stack & Dependencies

- **Android SDK**: Min SDK 24, Target SDK 36
- **UI Framework**: Jetpack Compose with Material 3 Dark Studio Theme
- **Camera & Media**: CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`), MediaCodec
- **Database**: Android Jetpack Room with KSP
- **Networking**: Retrofit 2, OkHttp 3, Moshi Converter
- **Image Loading**: Coil Compose
- **Concurrency**: Kotlin Coroutines & Reactive Flow

---

## 🚀 Getting Started

1. Clone or open the repository.
2. Build the project using Gradle:
   ```bash
   gradle assembleDebug
   ```
3. Launch the app and grant Camera & Microphone permissions.
4. Go to **New Stream**, choose your platform (Facebook, YouTube, or Custom RTMP), and tap **Initialize Live Studio** to go On Air!
