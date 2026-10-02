# 1-Tap Pure Mobile Facebook Live Plan (No Browser Needed)

## 1. Goal & Architecture
You want a **pure 1-tap mobile experience**:
- Open the LiveCaster app on your Android phone.
- Tap **"Start Live"**.
- The stream immediately appears live on your Facebook Profile / Timeline for all your friends and followers, **without ever opening Chrome or any web browser again**.

---

## 2. How 1-Tap Mobile Live Works with Facebook
Facebook Live provides a feature called **"Go live automatically" (Auto-start)** paired with a **Persistent Stream Key**:
1. **Persistent Stream Key**: As seen in your screenshot, **"Persistent stream key" is already turned ON** (`FB-29273625635568966-0-Ab7Cz2MX4hPTQLwn2UtAf...`). This key **never changes and never expires**.
2. **Auto-Start**: Once Auto-start is active on Facebook, Facebook's cloud will **automatically publish the live video to your profile timeline the exact second LiveCaster starts sending video**.
3. **From that point forward**: You **NEVER** need to open a browser again. You simply open LiveCaster on your phone, tap "Start Live", and you are instantly live on Facebook!

---

## 3. Plan & Changes to Implement in LiveCaster

### 1. In-App "1-Tap Facebook Setup" Wizard (`BroadcastSetupScreen.kt`)
- Add a dedicated **"1-Tap Facebook Direct Publish"** section in the setup screen.
- Provide a simple 1-step visual guide:
  - How to ensure "Go live automatically" is toggled in Facebook so future streams need zero browser interaction.
  - Pre-save and lock the **Persistent Stream Key** so you never have to re-enter or copy/paste it again.

### 2. Streamlined Instant Broadcast Mode (`BroadcastControlScreen.kt`)
- When you tap **"Start Live"** in LiveCaster:
  - The encoder immediately transmits the verified H.264 video (with SPS/PPS sequence headers and continuous keyframes) + AAC stereo audio.
  - Show a clear live indicator: **"LIVE ON FACEBOOK (Profile Feed Active)"**.
  - No prompt asking you to open a browser if Auto-start is active.
- Add an in-studio quick status pill showing **"Published to Profile"** so you have 100% confidence while streaming from your phone.

### 3. Persistent Stream Key Storage
- Ensure LiveCaster remembers your Facebook stream key permanently across app restarts so you can launch the app, tap once, and stream instantly.

---

## 4. Verification Plan
- Build and verify with `compile_applet`.
- Verify unit tests with `gradle :app:testDebugUnitTest`.
- Provide the user with exact 30-second instructions to verify the 1-tap broadcast on their phone.
