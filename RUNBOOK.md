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
adb -H 100.74.73.114 -P 5037 install -r app/build/outputs/apk/debugNoMinify/HeliBoard_0.1.2-debugNoMinify.apk
```

After installation, open setup, grant microphone permission, enter a fresh one-time pairing code, enable the IME, and select it. Validate typing and dictation in a normal text field and confirm that voice input is unavailable in password fields.

During dictation, the suggestion strip must show `● Înregistrare — apasă din nou pentru Stop`. After the second tap it must show `Se transcrie…` until the final text is inserted.

In portrait, verify that the row gaps and bottom padding are compact without reducing the key touch surfaces. The microphone must have a blue circular idle state, a red recording state, and sit slightly inward from the right edge.

## Distribution

Publish the APK only from a clean `main` commit and create a matching Git tag and GitHub release.
The release page and source archive provide the GPLv3 corresponding source. Never upload
`local.properties`, signing material, server environment files, pairing codes, or OpenAI credentials.

Every recipient needs a separately generated ten-minute pairing code. Installing the APK alone
enables normal typing, but not server-backed dictation. For public or untrusted distribution,
confirm that the STT gateway's per-device quotas and revocation commands are active before sharing.
