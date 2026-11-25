# AHNAF MODS V9

Android floating-menu UI project built with Kotlin + WebView.

## Project status

**Demo / concept application.** The switches and settings are interface demonstrations only. The app does not modify Free Fire, inspect game memory, automate aiming or firing, reveal hidden players, or alter another app's behavior.

## Design decisions

- Vertical floating panel
- Replaceable custom A9 logo artwork
- Crimson red and black theme
- HIDE and CLOSE controls
- Native Android Toast messages through a WebView bridge

## Build with Android Studio

1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Connect an Android device or start an emulator.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

The app requires the Android **Display over other apps** permission to show its floating bubble. The user must grant this permission manually in Android Settings.

## Build APK with GitHub Actions

Push the repository to GitHub. Open the **Actions** tab and run the **Build Android APK** workflow (or push to `main`). Download the `ahnaf-mods-v9-debug-apk` artifact from the workflow run.

## Replace artwork

Replace the placeholder files in `app/src/main/assets/assets/images/` with your own PNGs, keeping the same filenames:

- `logo.png`
- `floating_logo.png`
- `header.png`
- `combat.png`
- `visual.png`
- `tuning.png`
- `helper.png`

The HTML currently uses `logo.png` in the panel header. The native floating bubble already loads `floating_logo.png`; replace that file to use your own logo. The red rounded background is defined in `app/src/main/res/drawable/bubble_bg.xml`.

## Notifications

The WebView calls `Android.showToast(message)`. Kotlin displays it with Android's native `Toast` API. If the app is running as a floating overlay, the message is shown by Android rather than an in-page toast.

## Important Android notes

- Overlay permission is special access and is granted by the user in system Settings.
- Android may restrict foreground services and overlays depending on OS version and device vendor.
- This starter uses a foreground service with the `specialUse` service type. Review current Android distribution requirements before publishing.
- The app is intended for personal testing / sideloaded demo builds.
