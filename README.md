<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/8122e287-f43d-4689-bc2e-eb3dcd473ff1

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## add release workflow

Github Copilot: I couldn’t write this directly because the GitHub repo permissions in this session don’t allow changes to `fernke/xray-lite-android`. Here’s the exact workflow file to add under `.github/workflows/release.yml` in that repo.

```yaml name=.github/workflows/release.yml
name: Release

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Release tag/version (for example v1.0.0)"
        required: false
        default: "v1.0.0"
  push:
    tags:
      - "v*"

jobs:
  build-release:
    runs-on: ubuntu-latest
    permissions:
      contents: write

    env:
      KEYSTORE_PATH: ${{ github.workspace }}/my-upload-key.jks
      STORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
      KEY_PASSWORD: ${{ secrets.KEYSTORE_KEY_PASSWORD }}
      GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: gradle

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Accept Android SDK licenses
        run: yes | "$ANDROID_HOME"/cmdline-tools/latest/bin/sdkmanager --licenses || true

      - name: Create .env for app secrets
        run: |
          printf '%s\n' "GEMINI_API_KEY=${GEMINI_API_KEY}" > .env

      - name: Decode release keystore
        if: ${{ secrets.KEYSTORE_BASE64 != '' }}
        shell: bash
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > "$KEYSTORE_PATH"
          ls -l "$KEYSTORE_PATH"

      - name: Generate fallback keystore if no secret is provided
        if: ${{ secrets.KEYSTORE_BASE64 == '' }}
        shell: bash
        run: |
          keytool -genkeypair \
            -v \
            -keystore "$KEYSTORE_PATH" \
            -alias upload \
            -storepass android \
            -keypass android \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -dname "CN=GitHub Actions, OU=Release, O=Fernke, L=Unknown, S=Unknown, C=US"
          echo "Fallback keystore generated for CI release build"

      - name: Make Gradle wrapper executable
        run: chmod +x ./gradlew

      - name: Build release APK
        run: ./gradlew assembleRelease

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: xray-lite-android-release-apk
          path: app/build/outputs/apk/release/*.apk
          if-no-files-found: error

      - name: Upload app bundle artifact
        uses: actions/upload-artifact@v4
        with:
          name: xray-lite-android-release-aab
          path: app/build/outputs/bundle/release/*.aab
          if-no-files-found: ignore

      - name: Create GitHub release
        if: startsWith(github.ref, 'refs/tags/')
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
          files: |
            app/build/outputs/apk/release/*.apk
            app/build/outputs/bundle/release/*.aab
```

Required repo secrets:
- `GEMINI_API_KEY`
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEYSTORE_KEY_PASSWORD`

Notes:
- The app uses a release signing config in `app/build.gradle.kts`, so the workflow needs a keystore.
- If you want a stricter release flow, I can also add:
  - build only on tags
  - signing verification
  - Play Store upload step
  - automatic version bumping from tags
