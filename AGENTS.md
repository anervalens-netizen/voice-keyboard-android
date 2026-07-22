# AGENTS.md — Astra Keyboard

- Astra Keyboard is a GPLv3 product fork of HeliBoard v4.0, pinned initially at upstream commit `bd48798b99cccc99704eebf2a9259c02dbd684d5`.
- Preserve the mature keyboard engine, dictionaries, layouts, autocorrection, and password-field protections. Keep custom code focused on batch voice dictation and product defaults.
- Never embed an OpenAI credential, server secret, pairing code, recorded audio, or transcription in source, resources, BuildConfig, logs, or crash reports.
- Device authentication uses a non-exportable Android Keystore P-256 key. The app may store endpoints, device ID, and enrollment status locally.
- Voice input is explicit push-to-start/push-to-stop, never continuous or live. Do not record in password fields or after the input view closes.
- Build and test with `/home/andrei/android-sdk`, install through the Gaming ADB server, and validate on the physical OnePlus Nord 4 before closure.
- Read `ARCHITECTURE.md` and `RUNBOOK.md` for design and procedures.
