# ChessMind

ChessMind is a private, offline-first Android chess solver and trainer. Capture or import a board photo, build or correct the position, choose an analysis level, and get a best move with a readable explanation—without an account or server.

> **Current version: 1.0.2.** The app is usable end-to-end for camera/gallery capture, manual positions, FEN/PGN imports, local analysis, interactive line exploration, and history. Automatic piece recognition is deliberately labelled as unavailable until a suitably accurate on-device model is validated; ChessMind never invents a detected position.

## What works

- Sky-blue Material 3 interface with a minimal Home–Scan–History floating dock
- Bundled Space Grotesk and JetBrains Mono typography
- Original ChessMind hero illustration and animated analysis state
- Custom launcher artwork sourced from `public/logo.png`
- Short first-run onboarding and edge-to-edge phone layout
- CameraX scanner overlay plus gallery import and image rotation/review
- Manual board editor with tap-to-move, piece palette, long-press removal, flip, undo/redo, side-to-move, castling, and en-passant controls
- Strict FEN validation/import/export and common PGN import
- Complete legal move generation, including check filtering, castling, en passant, and promotion
- Four analysis presets: Beginner, Intermediate, Master, and God mode
- Packaged Stockfish 19 UCI analysis on ARM devices, with a cancellable built-in engine fallback
- MultiPV candidate moves, evaluation, depth, principal variation, and human-readable reasons
- Interactive analysis board with previous/next line controls, free legal play, reset, and re-analysis
- Local analysis history plus JSON backup/restore
- Local player profile with editable name and profile photo
- Functional settings for animation, coordinates, legal hints, haptics, and high-contrast board colors
- No account, analytics SDK, cloud engine, or image upload

## Project structure

```text
app/src/main/
├── assets/licenses/                 # Third-party license text
├── java/app/chessmind/
│   ├── data/                        # Local persistence
│   ├── data/engine/                 # Stockfish process adapter and fallback
│   ├── domain/engine/               # Engine contract, models, local search
│   ├── domain/model/                # Position, rules, FEN, PGN
│   ├── MainActivity.kt              # Compose screens and components
│   └── MainViewModel.kt             # App state and workflows
└── jniLibs/                         # Locally fetched Stockfish binaries (ignored)
scripts/fetch-stockfish.ps1          # Reproducible Stockfish fetch
docs/BUILD_BRIEF.md                  # Product and architecture brief
```

## Requirements

- JDK 17
- Android SDK 36
- Android 8.0 / API 26 or newer
- GitHub CLI (`gh`) only when fetching the official Stockfish binaries

## Build

Fetch the official Android ARM binaries used by release builds:

```powershell
.\scripts\fetch-stockfish.ps1
```

Then verify and build:

```powershell
.\gradlew.bat test lint assembleDebug assembleRelease
```

Outputs are written under `app/build/outputs/apk/`. If the native binaries are absent, the APK still builds and uses ChessMind's smaller local search engine.

Release builds use R8/resource shrinking. Without a local signing configuration they fall back to the Android debug key, which is appropriate only for development artifacts. Production signing uses an ignored `keystore.properties` file:

```properties
storeFile=C:/secure/chessmind-release.jks
storePassword=your-store-password
keyAlias=chessmind
keyPassword=your-key-password
```

## Stockfish and licensing

ChessMind's engine layer is implementation-independent. On supported ARM devices it launches the packaged official [Stockfish 19](https://github.com/official-stockfish/Stockfish/releases/tag/sf_19) binary through UCI; unsupported ABIs transparently use the built-in fallback engine. Stockfish is licensed under GPLv3, whose text is packaged at `app/src/main/assets/licenses/stockfish-gpl-3.txt`. The exact upstream source is available from the linked release/tag. Preserve the license and corresponding-source availability when redistributing an APK containing Stockfish.

Space Grotesk and JetBrains Mono are bundled under the SIL Open Font License; their license texts are included beside the Stockfish license.

## Privacy

Core features run offline. Positions, statistics, and history use app-private local storage. Captured images are placed in the app cache and are not uploaded. Camera access is requested only when opening the scanner; no broad storage permission is requested.

## Known limitation

Automatic recognition of arbitrary physical boards, book diagrams, and screenshots is a computer-vision product in its own right. The capture/review/correction pipeline is ready, but no model is bundled in 1.0.2 because an unvalidated model would silently create wrong chess positions. The next vision milestone is an on-device, confidence-scored detector with uncertain-square review and real-photo evaluation.

## Contributing

Keep Android presentation, chess rules, persistence, engine, and future vision code independent. Add unit tests for rule or notation changes, and run `test lint assembleRelease` before publishing.
