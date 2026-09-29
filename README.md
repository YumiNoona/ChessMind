# ChessMind

ChessMind is a privacy-first Android chess position solver and learning companion. It is designed to answer both **“What should I play?”** and **“Why?”** without requiring an account, server, or internet connection.

> **Release status:** v1.0.0 is the first foundation release. Manual position setup, FEN handling, local legal-move analysis, and the premium Compose interface are functional. Camera recognition and packaged Stockfish analysis remain on the roadmap and are not simulated or misrepresented.

## Features

- Native Android interface built with Kotlin and Jetpack Compose
- Interactive, reusable chessboard with tap-to-move editing
- Piece palette, board flipping, turn selection, clear, and reset controls
- Strict FEN import, export, and validation
- Local legal move generation with check filtering
- Castling, en passant, and promotion support
- Beginner, Intermediate, Master, and God Mode presentation presets
- Engine-neutral analysis contract ready for Stockfish
- Offline-first and account-free architecture
- Responsive dark visual system for phones and tablets

## Screens

The current vertical slice includes:

1. Home and privacy status
2. Manual position setup
3. Analysis-level selection
4. Local preview analysis
5. Practice, Saved, and Settings foundations

## Project structure

```text
ChessMind/
├── app/
│   ├── src/main/java/app/chessmind/
│   │   ├── domain/engine/    # Engine API and analysis models
│   │   ├── domain/model/     # Chess model, FEN, and legal rules
│   │   ├── MainActivity.kt   # Compose presentation
│   │   └── MainViewModel.kt  # UI state and actions
│   └── src/test/             # Domain unit tests
├── docs/
│   └── BUILD_BRIEF.md        # Product, architecture, and delivery specification
├── gradle/wrapper/           # Reproducible Gradle tooling
└── build.gradle.kts
```

## Requirements

- Android Studio with JDK 17
- Android SDK 36
- Android 8.0 (API 26) or newer device/emulator

## Build and test

From PowerShell:

```powershell
.\gradlew.bat test assembleDebug
```

Build a release APK:

```powershell
.\gradlew.bat clean test assembleRelease
```

Without local signing properties, release builds fall back to Android’s debug signing key for installable development artifacts. Production signing can be configured using an ignored `keystore.properties` file:

```properties
storeFile=C:/secure/chessmind-release.jks
storePassword=your-store-password
keyAlias=chessmind
keyPassword=your-key-password
```

## Quality checks

The domain test suite covers FEN round trips, validation failures, en-passant state, legal starting moves, and pinned-piece safety. All releases should pass:

```powershell
.\gradlew.bat test lint assembleRelease
```

## Roadmap

- Stockfish UCI integration with cancellable background analysis
- CameraX capture and gallery import
- On-device board and piece recognition with confidence review
- Room-backed history, saved positions, and analysis cache
- Offline tactics packs, daily practice, and learning statistics
- PGN import/export, accessibility audit, and baseline profiles

The detailed technical and product plan is available in [docs/BUILD_BRIEF.md](docs/BUILD_BRIEF.md).

## Privacy

ChessMind does not require an account or network connection. Core positions and future scan data are designed to remain on-device. Photos will be discarded by default unless the user explicitly chooses to save them.

## Contributing

Issues and pull requests are welcome. Please keep domain logic independent of Android UI and add tests for chess-rule changes.
