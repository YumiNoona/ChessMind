# ChessMind — Refined Product & Build Brief

## Product goal

Build a premium, native Android app that answers two questions with the fewest possible steps:

1. **What should I play?**
2. **Why does it work?**

Users can scan a physical board, import an image, paste a FEN/PGN, or set up a position manually. The app reconstructs the board, asks the user to confirm uncertain squares, analyzes locally, explains the move at the user's level, and can turn the position into practice.

Core behavior must work in airplane mode. No sign-in, cloud engine, analytics SDK, or upload is required.

## Experience principles

- **Fast path first:** Open → Scan → Confirm → Analyze. Remember the last analysis level so returning users do not face an extra decision every time.
- **Trust over false confidence:** show per-square recognition confidence. If orientation, kings, side-to-move, or multiple pieces are uncertain, ask the user instead of guessing.
- **Board stays dominant:** controls collapse around the board on small screens and move beside it on tablets/foldables.
- **Progressive disclosure:** beginners see one move and a short explanation; engine details and MultiPV appear only for advanced users.
- **Interruptible work:** image processing and engine analysis never run on the main thread and can always be cancelled.
- **Private by design:** original images are discarded by default after recognition; saving one is an explicit choice.

## Primary flows

### Solve a board

Home → Camera → live board outline → capture → perspective/crop review → detected position → resolve uncertain squares → choose/remember level → analysis.

### Import a position

Home → image picker → crop/rotate → detection → confirmation → analysis.

### Manual setup

Home → position editor → drag/tap pieces → set turn/castling/en-passant → legality feedback → analysis.

### Learn from a result

Analysis → animate best move → step through principal variation → compare candidates → “Try it yourself” → practice feedback → save optional note.

## Analysis modes

Modes are not merely different Stockfish depths. The engine finds candidates; a separate explanation/ranking layer controls presentation.

| Mode | Target | Output | Default budget |
|---|---|---|---|
| Beginner | new player | one safe practical move, plain-language reason, one threat | 300–600 ms |
| Intermediate | club player | best move, tactical/positional reason, short line | 1–2 s |
| Master | tournament player | 3 candidates, evaluation, plans and critical line | 3–6 s |
| God mode | maximum local strength | MultiPV, live depth/eval, longer PV, engine controls | user-stoppable, 8 s default |

The app must distinguish **engine-best**, **practical**, and **easy-to-play** moves. Any non-top-engine recommendation is labelled clearly.

## Explanation system

Generate explanations deterministically from position features and engine lines before considering any learned language component. Detect and combine:

- checks, captures, direct threats, mate threats;
- forks, pins, skewers, discoveries, overloaded/removed defenders;
- development, king safety, space, open files, outposts, passed pawns;
- material changes and forced replies in the principal variation.

Every explanation must be traceable to the board and line. Avoid claims that cannot be verified from legal moves.

## Scanner and vision pipeline

`Camera/Image → board quadrilateral → perspective correction → orientation candidates → 64 crops → piece classifier → confidence map → legality repair suggestions → confirmation → FEN`

Requirements:

- Use CameraX and on-device inference.
- Process a throttled preview stream; run full resolution only after capture.
- Support physical boards, diagrams, books, and common 2D screenshots through model/version adapters.
- Never “repair” a position invisibly. Suggested fixes are reversible and highlighted.
- Validate exactly one king per side, pawn constraints, impossible checks, castling consistency, and side-to-move ambiguity.
- Provide crop manually and manual setup fallbacks after detection failure.

## Chess correctness

The shared chess core must support legal move generation, check/evasion, castling, en passant, promotion, repetition keys, insufficient material, mate/stalemate, SAN, FEN, and PGN. Property-test make/unmake and FEN round trips. Never rely on the UI component as the source of chess legality.

## Offline engine

- Package a maintained, license-compatible Stockfish build per supported ABI and include required license notices/source offer.
- Communicate through an engine-neutral `ChessEngine` contract.
- Keep one supervised engine process/session, serialize commands, parse UCI incrementally, and recover after crash/timeout.
- Auto-size threads and hash using device memory and thermal constraints; conservative defaults beat benchmark-chasing.
- Cache results by normalized position + engine version + settings.
- Pause/cancel analysis when the app backgrounds or a new position replaces it.

## Storage and privacy

Use Room for structured data and app-private files for optional images. Store schema versions and support export/import as a documented ZIP/JSON bundle.

Core entities: `Position`, `Analysis`, `CandidateLine`, `SavedPosition`, `PracticeSession`, `PracticeAttempt`, and `AppSettings`.

Retention defaults:

- history metadata: on;
- source photos: off;
- saved positions: until user deletes;
- one-tap clear and export from Settings.

## Practice

- Ship an offline curated puzzle pack with themes and difficulty metadata.
- Add engine-generated practice only when the position has a stable, verifiable solution.
- Score with centipawn loss plus tactical outcome; do not call every non-best move a mistake.
- Daily set uses a local date seed and works without connectivity.
- Track accuracy, streak, solve time, themes needing work, and average centipawn loss locally.

## Accessibility

- TalkBack labels include piece, color, square, state, and available action.
- Do not encode evaluation only by red/green; pair color with label/icon/value.
- Support font scaling, reduced motion, high contrast board, haptic toggle, and tap-to-move.
- Minimum 48 dp targets outside the board; provide an accessible move-entry alternative.

## Performance budgets

- Warm launch to interactive home: under 700 ms on a mid-range reference phone.
- No main-thread disk, model, or engine work; zero frozen frames caused by analysis.
- Board interaction response: under 50 ms.
- Preview detection: target 8–12 fps with backpressure; no unbounded frame queue.
- Captured-image result: target under 2 s after crop on reference hardware.
- Memory: stream crops and reuse buffers; do not retain full camera frames in history.
- APK: use ABI splits/app bundle; load vision model lazily.

Measure with Macrobenchmark, Baseline Profiles, Compose tracing, StrictMode in debug, and representative low/mid/high devices.

## Architecture

Use Kotlin, Jetpack Compose, coroutines/Flow, CameraX, Room, DataStore, and an on-device inference runtime.

Boundaries:

- `app/presentation` — screens, adaptive layout, navigation, state;
- `domain/chess` — rules, notation, position validation;
- `domain/analysis` — presets, explanation, practice grading;
- `data` — Room, files, import/export;
- `engine-api` / `engine-stockfish` — engine contract and UCI implementation;
- `vision-api` / `vision-local` — detection contracts and models;
- `camera` — permissions, capture, gallery, crop;
- `benchmark` and test fixtures.

UI depends on domain interfaces, never on Stockfish, CameraX, Room, or a particular ML model directly.

## Delivery plan

1. **Foundation:** project, design system, reusable board, legal chess core, FEN, adaptive navigation, tests.
2. **Solver:** Stockfish integration, live/cancellable analysis, result UI, explanation v1, cache.
3. **Input:** complete editor, PGN, gallery crop, CameraX capture.
4. **Vision:** board/piece detection, confidence review, device benchmarks and fallback UX.
5. **Training:** curated puzzles, grading, daily practice, local stats.
6. **Polish:** history/saved/notes, import/export, accessibility, baseline profile, release signing and license screen.

Each milestone must produce a usable APK. Camera ML is not allowed to block the manual/FEN solver from shipping.

## Definition of done for production

- Release APK/AAB builds reproducibly and passes lint/unit/instrumented tests.
- Core flows work with networking disabled and after process death.
- All moves shown are legal; FEN/PGN failures explain the exact issue.
- Analysis can be stopped and cannot freeze navigation.
- Camera permission is requested only on scanner entry; denied permission has a useful fallback.
- TalkBack can complete setup and analysis.
- Privacy, engine/model versions, licenses, and deletion controls are visible in-app.
- Crash-free and performance checks pass on the defined device matrix.

## Explicit non-goals for the first release

- Online play, accounts, cloud sync, social feeds, chat, ads, and remote engines.
- Claiming perfect recognition across arbitrary artistic piece sets.
- Natural-language advice that is not grounded in the legal engine line.
