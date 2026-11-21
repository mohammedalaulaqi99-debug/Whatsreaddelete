# ReadDelete Android starter (v0.1)

## Included
- Kotlin + Jetpack Compose UI
- Room local database for selected-message tracking
- Custom delay field (1–86,400 seconds)
- Individual/group flag
- Two registration paths:
  - Send through ReadDelete: opens WhatsApp share composer with prefilled text
  - Register existing message: manually records recipient and exact message
- Accessibility service feasibility-test scaffold restricted to WhatsApp packages

## Important limitation
This starter does **not** claim to detect read receipts and does **not** automatically delete messages.
WhatsApp does not expose a supported public API for read receipts or Delete for everyone in ordinary personal chats.
The accessibility service currently records only diagnostic event types, not message contents.
The next milestone is to test whether the installed WhatsApp version exposes a stable accessible read-state description.
If it does not, reliable read-triggered deletion cannot be implemented through this approach.

## Build
1. Open this folder in Android Studio (current stable).
2. Allow Gradle sync and install Android SDK 35 if prompted.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

## Device test
- Install on an Android 10+ device.
- Register a test message to yourself or a consenting test contact.
- For the send path, select WhatsApp and manually send the message.
- Open Accessibility settings only if you want to test the monitor scaffold.
- Check Logcat for `ReadDeleteMonitor`.
- Do not enable any automatic deletion until read-state detection and exact-message matching are independently validated.

## Privacy
Tracking records are stored locally in the app's Room database. Avoid using real sensitive messages during prototype testing.
