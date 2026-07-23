# AGENTS.md — Astra Keyboard

## Fast lane pentru schimbări mici

- Pentru schimbări UI mici, corecții locale, configurări/documentație și date punctuale, favorizează finalizarea în 5–10 minute, fără a sacrifica corectitudinea.
- Flux implicit: inspectează strict zona afectată, implementează, rulează un singur set de verificări țintite, fă un singur deploy când este în scope, apoi verifică exact comportamentul cerut și health-ul.
- Nu crea framework-uri, scripturi generice, migrații, medii temporare, documentație nouă, screenshot-uri sau suite complete de teste decât dacă sunt strict necesare rezultatului.
- Nu repeta lint, typecheck, build sau teste pe același conținut neschimbat; reutilizează dovezile încă valide și păstrează output-ul compact.
- Nu instala instrumente sau browsere pentru o validare minoră dacă există deja o cale directă de verificare.
- Extinde investigația numai când ruta simplă este blocată, riscul este ridicat sau verificarea țintită eșuează; explică motivul într-o singură frază.
- Porțile obligatorii specifice proiectului rămân valabile când schimbarea le activează direct; „proportionate checks” nu înseamnă automat toate verificările disponibile.


- Astra Keyboard is a GPLv3 product fork of HeliBoard v4.0, pinned initially at upstream commit `bd48798b99cccc99704eebf2a9259c02dbd684d5`.
- Preserve the mature keyboard engine, dictionaries, layouts, autocorrection, and password-field protections. Keep custom code focused on batch voice dictation and product defaults.
- Never embed an OpenAI credential, server secret, pairing code, recorded audio, or transcription in source, resources, BuildConfig, logs, or crash reports.
- Device authentication uses a non-exportable Android Keystore P-256 key. The app may store endpoints, device ID, and enrollment status locally.
- Voice input is explicit push-to-start/push-to-stop, never continuous or live. Do not record in password fields or after the input view closes.
- Build and test with `/home/andrei/android-sdk`, install through the Gaming ADB server, and validate on the physical OnePlus Nord 4 before closure.
- Read `ARCHITECTURE.md` and `RUNBOOK.md` for design and procedures.
