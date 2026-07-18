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
adb -H 100.74.73.114 -P 5037 install -r app/build/outputs/apk/debugNoMinify/HeliBoard_0.1.8-debugNoMinify.apk
```

After installation, open setup, grant microphone permission, enter a fresh one-time pairing code, enable the IME, and select it. Validate typing and dictation in a normal text field and confirm that voice input is unavailable in password fields.

The suggestion strip must consume no height while idle. A short Enter/action-key press must keep its normal behavior. Holding it for the Android system long-press interval must start dictation with haptic feedback: Enter grows by 25%, floats with a shadow, pulses in blue, and the other keys dim slightly. A slim translucent status bar appears only while recording or transcribing. While recording, one short Enter tap must stop dictation without inserting Enter; all temporary UI retracts after completion.

In portrait, verify that no bar or empty band remains above the keys while idle. The Enter/action key must retain its normal icon with a small microphone badge, and the badge must be absent in password fields. The original Enter key must not remain visible behind the floating STT key. Comma must show a small settings gear; long-pressing it must open the original shortcuts panel, which includes Settings and access to the bundled light-to-dark themes.

## Distribution

Publish the APK only from a clean `main` commit and create a matching Git tag and GitHub release.
The release page and source archive provide the GPLv3 corresponding source. Never upload
`local.properties`, signing material, server environment files, pairing codes, or OpenAI credentials.

Every recipient needs a separately generated ten-minute pairing code. Installing the APK alone
enables normal typing, but not server-backed dictation. For public or untrusted distribution,
confirm that the STT gateway's per-device quotas and revocation commands are active before sharing.
