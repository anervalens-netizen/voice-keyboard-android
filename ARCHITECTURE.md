# Voice Keyboard Android architecture

The application keeps HeliBoard's keyboard engine and replaces its external voice-IME shortcut with an internal, final-result dictation flow.

## Dictation flow

1. The microphone key starts a local AAC/M4A recording after runtime permission is granted.
2. A floating microphone expands a status pill to the left while recording; a second tap stops recording and contracts the pill to a compact transcription-in-progress state. The app does not stream partial audio or text.
3. A P-256 key held by Android Keystore signs the request timestamp, nonce, request ID, language, and SHA-256 digest of the exact audio bytes.
4. The client tries the public HTTPS endpoint first and the Tailscale endpoint only after a transport failure.
5. The returned final text is committed once through the active `InputConnection`.

The OpenAI key never leaves the server. The client stores only public endpoint configuration, a random device ID, enrollment status, and its non-exportable private key.

The voice key remains hidden for password fields and editors that set `IME_FLAG_NO_MICROPHONE`, following HeliBoard's existing `InputAttributes` behavior.

## Product geometry

Portrait geometry keeps the large phone-first key surfaces while using compact vertical gaps and minimal bottom padding. The suggestion-strip plane is visually transparent while retaining its fixed height so the keyboard does not jump between states. The pinned voice control floats slightly inward from the right edge, is blue while idle and red while recording, and grows a rounded status pill toward the left. The pill contracts for transcription progress and fully retracts when dictation returns to idle.
