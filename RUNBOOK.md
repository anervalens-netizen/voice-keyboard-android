# Voice Keyboard Android runbook

## Build

```bash
./gradlew testRunTestsUnitTest
./gradlew assembleDebugNoMinify
```

The local Android SDK path is stored in ignored `local.properties`. No server credential belongs in the APK.

## Physical-device install

The permanently attached OnePlus is the primary development test path:

```bash
adb devices -l
adb -s 885ae67a install -r app/build/outputs/apk/debugNoMinify/HeliBoard_0.1.9-debugNoMinify.apk
```

Gaming remains the remote fallback through Tailscale:

```bash
adb -H 100.74.73.114 -P 5037 devices -l
adb -H 100.74.73.114 -P 5037 install -r app/build/outputs/apk/debugNoMinify/HeliBoard_0.1.9-debugNoMinify.apk
```

After installation, open setup, grant microphone permission, enter a fresh one-time pairing code, enable the IME, and select it. Validate typing and dictation in a normal text field and confirm that voice input is unavailable in password fields.

The compact 30 dp suggestion strip must remain present for the full lifetime of the keyboard. Suggestions, clipboard or inline content, toolbar actions, and recording/transcription may replace its contents without changing its height or moving the keys. Its inset drawer must touch the keyboard at the bottom while remaining visually distinct through asymmetric corner radii, a subtle outline, elevation, and tint. No ellipsis is shown; swipe-up and long-press still open additional suggestions. The full-width area behind the drawer must remain transparent, including over light and dark host apps; the solid themed background begins at the key surface. A short Enter/action-key press must keep its normal behavior. Holding it for the Android system long-press interval must start dictation with haptic feedback: Enter grows by 25%, floats with a shadow, pulses in blue, and the other keys dim slightly. Recording/transcription must reuse the attached drawer shape with a blue state and integrated red cancel control. While recording, one short Enter tap must stop and upload without inserting Enter; cancel must instead stop, delete the local audio, avoid upload, and retract all temporary UI.

In portrait, verify that the strip stays at 30 dp while idle, during typing, after committing text, and across recording/transcription transitions; the key rows must never jump vertically. Verify that the drawer meets the keyboard without a visible gap, remains visually distinct, and contains no ellipsis. Confirm that swipe-up and long-press still open additional suggestions and that a recent clipboard item appears when eligible. Check both recording and transcription drawer states plus the cancel action. The Enter/action key must retain its normal icon with a small microphone badge, and the badge must be absent in password fields. The original Enter key must not remain visible behind the floating STT key. Comma must show a small settings gear; long-pressing it must open the original shortcuts panel, which includes Settings and access to the bundled light-to-dark themes. Releasing comma without sliding must close the panel without opening Emoji; sliding deliberately to an action must still activate it.

## Distribution

Publish the APK only from a clean `main` commit and create a matching Git tag and GitHub release.
The release page and source archive provide the GPLv3 corresponding source. Never upload
`local.properties`, signing material, server environment files, pairing codes, or OpenAI credentials.

Every recipient needs a separately generated ten-minute pairing code. Installing the APK alone
enables normal typing, but not server-backed dictation. For public or untrusted distribution,
confirm that the STT gateway's per-device quotas and revocation commands are active before sharing.
