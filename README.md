# LexiGuess

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_MVVM_+_MVI-FF6F00.svg?style=flat)](https://developer.android.com/topic/architecture)
[![DI](https://img.shields.io/badge/DI-Hilt-yellow.svg?style=flat)](https://dagger.dev/hilt/)
[![Database](https://img.shields.io/badge/Database-Room_SQLite-009688.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Tests](https://img.shields.io/badge/Unit_Tests-37_Passing-brightgreen.svg?style=flat)](app/src/test/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_On--Device-success.svg?style=flat)](#privacy-and-data-safety)

A tactile, modern, offline-first Android word deduction game. Engineered with Jetpack Compose, Material 3, 2.5D extruded acrylic tiles, mechanical 3D keycaps, hardware gyroscope tilt parallax, multi-board Dordle/Quordle, campaign boss fights, and a low-latency procedural audio engine.

---

## Overview

Unlike flat web-based ports, LexiGuess is designed specifically for Android hardware to recreate the physical sensation of playing with real tactile tiles:

- **Mechanical 3D Keycaps**: Each on-screen key simulates physical switch mechanics with a 3.5dp press-down depression offset, bottom extrusion lip shadows, and top-edge specular glints.
- **Extruded 2.5D Game Tiles & Recessed Tray**: Empty letter slots appear as sunken wells stamped into a slate/wood tray; submitted and revealed tiles feature glossy chamfers, beveled edges, and colored ambient drop shadows.
- **3D Air-Lift Flip**: Row reveals lift tiles toward the camera ($1.12\times$ parabolic scale lift at 90° edge-on), dynamically darkening them as they rotate perpendicular to directional light before snapping face-up with bounce lighting.
- **Hardware Gyroscope Tilt Parallax**: Tilting your physical device dynamically rotates the game board in 3D perspective using low-pass filtered gravity and accelerometer sensor data.
- **Adaptive 3-Way Display Theme**: System Default (follows OS day/night mode), Light Mode, and Dark Mode, switchable in one tap from the home screen header.
- **Zero-Allocation Procedural Audio**: Tactile key clicks, chorded flip arpeggios, and victory fanfares synthesized in real time via direct PCM `AudioTrack` buffers without external audio assets.

---

## Game Modes

| Mode | Description |
|---|---|
| **Daily Challenge** | The global daily puzzle shared deterministically across all players with calendar streak tracking. |
| **Multi-Board (Dordle & Quordle)** | Solve 2 or 4 secret words simultaneously using multi-split colored keyboards. |
| **50-Level Campaign** | 4 progressive worlds with star ratings, escalating difficulty, and Boss Health Bar battles. |
| **Timed Rush (120s Blitz)** | High-speed puzzle sprint—solve as many words as possible before the countdown expires. |
| **Daily Archive** | Interactive 28-day calendar allowing players to tap and replay any past missed daily puzzle. |
| **Pass & Play Duel** | 2-player local hot-seat battle on a single device. |
| **Unlimited Practice** | Free play across 4, 5, 6, and 7-letter word lengths with zero lockout timers. |
| **Custom Challenge Maker** | Create custom secret puzzles and generate shareable `LX-XXXX` alphanumeric challenge codes. |

---

## Progression and Meta Systems

- **Lexicographer Ranks & XP**: Earn XP on every solve to progress from Novice through Wordsmith to Grandmaster.
- **Streak Calendar & Automated Shields**: 28-day calendar tracking wins and missed days. Bank up to 2 Streak Freezes that automatically deploy if a day is missed.
- **Word Vault & Vocabulary Codex**: Personal dictionary cataloging all solved words with parts of speech, phonetic definitions, and mastery stars.
- **Lexi Bot Breakdown**: Post-game algorithmic analysis showing remaining candidate counts and elimination efficiency on every guess.
- **Daily Quests**: Rotating daily missions granting bonus XP and cosmetics.
- **16 Milestone Achievements**: Badges for speed records, multi-length word mastery, clutch wins, and flawless solves.

---

## Personalization and Visuals

- **6 Board Color Palettes**:
  - `Classic Emerald`: Traditional Wordle green and warm ochre.
  - `Midnight OLED`: Pitch black background with electric neon mint.
  - `Cyberpunk Synth`: High-voltage cyan and hot magenta.
  - `Rose Gold`: Soft pastel rose, warm amber, and slate.
  - `Royal Gold`: Deep sapphire navy with lustrous imperial gold.
  - `Solar Flare`: Radiant solar amber and fiery crimson.
- **Tile Materials**: Unlockable Classic, Glassmorphism, Carbon Fiber, and Golden Obsidian tile finishes.
- **Confetti Victory Engine**: Physics-driven particle celebration rendered directly on the Compose Canvas.

---

## Architecture and Tech Stack

LexiGuess strictly adheres to modern Android Clean Architecture and Unidirectional Data Flow (UDF).

```
+-----------------------------------------------------------------+
|                           UI LAYER                              |
|   Jetpack Compose · Material 3 · Compose Navigation · Canvas    |
|   Sensors (Tilt Parallax) · AudioTrack Procedural Synthesizer   |
+--------------------------------+--------------------------------+
                                 |
                                 v
+-----------------------------------------------------------------+
|                        VIEWMODEL LAYER                          |
|        StateFlow · SharedFlow · SavedStateHandle · Hilt         |
+--------------------------------+--------------------------------+
                                 |
                                 v
+-----------------------------------------------------------------+
|                         DOMAIN LAYER                            |
|   GameEngine (Evaluator, Bot Clues, Modulo Hash, Hard Mode)     |
|   MultiBoardEngine · BossFightEngine · ChallengeCodec           |
+--------------------------------+--------------------------------+
                                 |
                                 v
+-----------------------------------------------------------------+
|                          DATA LAYER                             |
|   Room Database (Match history, Levels, Vault, Achievements)    |
|   DataStore Preferences (XP, Theme, Tilt, Audio, High Scores)   |
|   Assets (15,287 Offline Words: 4L, 5L, 6L, 7L) · FreeDict API  |
+-----------------------------------------------------------------+
```

### Core Technologies
- **Language**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose (BOM 2024.06.00) + Material 3
- **Dependency Injection**: Dagger Hilt 2.51.1
- **Local Persistence**: Room SQLite 2.6.1 (with KSP) + Jetpack DataStore Preferences
- **Hardware Sensors**: Android `SensorManager` (`TYPE_GRAVITY`, `TYPE_ACCELEROMETER`)
- **Audio Engine**: Direct PCM synthesis via Android `AudioTrack` (zero allocation, pre-cached static buffers)
- **Networking**: Retrofit 2 + OkHttp 4 (used for optional anonymous definition lookups)
- **Testing**: JUnit 4, Kotlinx Coroutines Test, Compose UI Test

---

## Project Structure

```
app/src/main/java/com/lexiguess/app/
├── LexiGuessApp.kt                   # Hilt Application entry point
├── MainActivity.kt                   # Single-activity root & Theme/Parallax providers
├── data/
│   ├── db/                           # Room Database entities & DAOs
│   │   ├── AppDatabase.kt            # Room DB migration v2
│   │   ├── GameDao.kt & Record.kt    # Match history, streaks, distributions
│   │   ├── LevelDao.kt & Record.kt   # 50 campaign levels & star ratings
│   │   ├── VaultDao.kt & Record.kt   # Vocabulary codex & definitions cache
│   │   └── AchievementDao.kt         # 16 milestone badges
│   ├── local/
│   │   └── PlayerPreferences.kt      # DataStore: XP, Theme, Tilt, Haptics, Audio
│   ├── network/
│   │   └── WordApiService.kt         # Retrofit interface for remote dictionary fallback
│   └── repository/
│       ├── GameRepository.kt         # Streak logic, calendar queries, freeze shields
│       ├── WordRepository.kt         # 15,287-word offline loader & definition lookup
│       └── QuestRepository.kt        # Daily quest tracking & rewards
├── domain/
│   ├── GameEngine.kt                 # Multi-length evaluation, hash selector, Bot metrics
│   ├── MultiBoardEngine.kt           # Dordle & Quordle state evaluation
│   ├── ChallengeCodec.kt             # Share code encoder/decoder (LX-XXXX)
│   └── model/                        # Immutable domain models (GameState, TileState, etc.)
└── ui/
    ├── audio/
    │   └── SoundManager.kt           # Procedural PCM audio synthesizer
    ├── composable/
    │   ├── TileGrid.kt               # 2.5D Extruded tiles, recessed tray, 3D flip
    │   ├── WordleKeyboard.kt         # Tactile 3D keycaps with physical depression
    │   ├── ConfettiParticleEngine.kt # Physics-based Compose Canvas particles
    │   ├── StreakCalendarDialog.kt   # 28-day streak matrix & archive launcher
    │   └── GameOverSheet.kt          # End-game scorecard & definition sheet
    ├── navigation/
    │   └── NavGraph.kt               # NavigationHost & Material 3 BottomBar
    ├── screen/
    │   ├── HomeScreen.kt             # Dashboard, branding header, mode launcher
    │   ├── GameScreen.kt             # Main game board & hardware keyboard listener
    │   ├── MultiBoardScreen.kt       # Split-tile Dordle/Quordle screen
    │   ├── SettingsScreen.kt         # Theme mode, 3D tilt, palettes, legal notices
    │   ├── LevelsScreen.kt           # Campaign stage selector
    │   ├── StatsScreen.kt            # Charts, streaks, win distributions
    │   ├── VaultScreen.kt            # Vocabulary journal & practice launcher
    │   ├── PassAndPlayScreen.kt      # 2-player local duel
    │   └── AchievementsScreen.kt     # Badge unlock showcases
    ├── theme/
    │   ├── Color.kt & Theme.kt       # 6 Board palettes & light/dark color schemes
    │   └── Type.kt                   # Custom typography scales
    └── util/
        └── TiltParallax.kt           # Low-pass filtered sensor-driven 3D tilt
```

---

## Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or later recommended.
- **JDK**: Version 17 or 21.
- **Android Device / Emulator**: Running Android 8.0 (API level 26) or higher.

### Clone and Build
```bash
git clone https://github.com/Dr-Rank1/Wordle.git
cd Wordle

# Build debug APK
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Install onto Connected Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.lexiguess.app/.MainActivity
```

---

## Legal and Trademark Notice

- **Trademark Disclaimer**: Wordle is a registered trademark of The New York Times Company (USPTO Reg. No. 6,838,829). LexiGuess is an independent game and is not affiliated with, sponsored by, authorized by, or endorsed by The New York Times Company or any of its subsidiaries.
- **Game Mechanics**: Under 17 U.S.C. § 102(b), game mechanics and systems of rules are not copyrightable. The deductive letter-matching mechanic originates from the public-domain games Jotto (1955), Mastermind (1970), and Lingo (1987).
- **Daily Word Scheduling**: Daily puzzle solutions in LexiGuess are determined via an independent algorithmic epoch hash and do not replicate or copy the chronological solution list of any third-party game.

---

## Privacy and Data Safety

- **100% Local and On-Device**: All statistics, streak histories, campaign progress, and vocabulary records are stored exclusively on your device in a local Room SQLite database.
- **No Telemetry / No Tracking**: Zero analytics SDKs, zero advertising networks, zero tracking cookies, and zero personal data collection.
- **Network Access**: The `android.permission.INTERNET` permission is used strictly for optional, anonymous word definition queries via the Free Dictionary API.

---

## License

This project is open source and available under the terms of the [MIT License](LICENSE).
