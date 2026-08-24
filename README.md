<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/c4220ff5-98b6-40e4-9072-fe59d1a3ab92

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio or use command-line tools.
2. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example`).
3. To automatically start the emulator, build the APK, and install & launch the app, simply run:
   - Double click `run_emulator.bat` or run `.\run_emulator.ps1` in PowerShell / terminal.
   - Or run `./gradlew assembleDebug` and install manually via Android Studio.
