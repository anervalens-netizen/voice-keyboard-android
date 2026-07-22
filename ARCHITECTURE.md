# Astra Keyboard architecture

The application keeps HeliBoard's keyboard engine and replaces its external voice-IME shortcut with an internal, final-result dictation flow.

## Dictation flow

1. The microphone key starts a local AAC/M4A recording after runtime permission is granted.
2. Holding the Enter/action key for the Android system long-press interval starts recording without emitting Enter and produces long-press haptic feedback. Enter grows by 25%, floats above the slightly dimmed keyboard, and pulses in blue; a slim translucent status bar slides in only while status information is useful. While recording, one short Enter tap stops recording and is consumed. The app does not stream partial audio or text.
3. A P-256 key held by Android Keystore signs the request timestamp, nonce, request ID, language, and SHA-256 digest of the exact audio bytes.
4. The client tries the public HTTPS endpoint first and the Tailscale endpoint only after a transport failure.
5. The returned final text is committed once through the active `InputConnection`.

The OpenAI key never leaves the server. The client stores only public endpoint configuration, a random device ID, enrollment status, and its non-exportable private key.

The voice key remains hidden for password fields and editors that set `IME_FLAG_NO_MICROPHONE`, following HeliBoard's existing `InputAttributes` behavior.

## Product geometry

Portrait geometry keeps the large phone-first key surfaces while using compact vertical gaps and minimal bottom padding. The 30 dp suggestion-strip container remains present for the full lifetime of the keyboard, so changing word suggestions, clipboard or inline content, toolbar actions, and voice status never moves the keys. Its inset drawer surface is flush with the keyboard at the bottom, has pronounced top corners, restrained bottom corners, a subtle outline and elevation, and a distinct tint so it reads as an attached extension rather than another keyboard row. Additional suggestions remain available by swiping up or long-pressing, without an ellipsis indicator. Recording and transcription reuse the same drawer geometry with a restrained blue gradient, microphone state, and integrated red cancel control. The redundant toolbar microphone stays hidden because dictation belongs to Enter. The Enter/action key keeps its normal short-press behavior and shows a microphone badge only where voice input is allowed. Long-pressing comma retains HeliBoard's original shortcut panel, including Settings, and requires an intentional slide before an action is selected; releasing without sliding closes the panel without opening Emoji or another action. A small gear badge makes Settings discoverable, and the existing white, light, dark, and black theme presets remain available under Appearance.
