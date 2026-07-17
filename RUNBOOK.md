# Voice Keyboard Android runbook

## Build

```bash
./gradlew testRunTestsUnitTest
./gradlew assembleDebugNoMinify
```

The local Android SDK path is stored in ignored `local.properties`. No server credential belongs in the APK.

## Physical-device install

Gaming exposes its authorized ADB server through Tailscale:

```bash
adb -H 100.74.73.114 -P 5037 devices -l
adb -H 100.74.73.114 -P 5037 install -r app/build/outputs/apk/debugNoMinify/HeliBoard_0.1.3-debugNoMinify.apk
```

After installation, open setup, grant microphone permission, enter a fresh one-time pairing code, enable the IME, and select it. Validate typing and dictation in a normal text field and confirm that voice input is unavailable in password fields.

During dictation, the transparent top plane must keep a floating microphone at the right. The first tap must animate a red status pill toward the left with `● Înregistrare — apasă din nou pentru Stop`. After the second tap, the pill must contract to `Se transcrie…`; it must fully retract after the final text is inserted or dictation is cancelled.

In portrait, verify that the row gaps and bottom padding are compact without reducing the key touch surfaces. The top plane must have no solid bar background and must retain its height through every dictation state. The microphone must have a blue circular idle state, a red recording state, remain slightly inward from the right edge, and keep a reliable touch target throughout both animations.

## Distribution

Publish the APK only from a clean `main` commit and create a matching Git tag and GitHub release.
The release page and source archive provide the GPLv3 corresponding source. Never upload
`local.properties`, signing material, server environment files, pairing codes, or OpenAI credentials.

Every recipient needs a separately generated ten-minute pairing code. Installing the APK alone
enables normal typing, but not server-backed dictation. For public or untrusted distribution,
confirm that the STT gateway's per-device quotas and revocation commands are active before sharing.
