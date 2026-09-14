# LexiGuess

A Wordle-style Android word-guessing game built with Kotlin and Jetpack Compose.
The app is fully playable offline and silently refreshes its word pool from the
GitHub Wordle word list whenever a network connection is available.

---

## What is LexiGuess?

LexiGuess presents a new hidden five-letter word every day. The player has six
attempts to guess it. After each guess, every tile flips to reveal whether its
letter is in the correct position (green), present somewhere in the word
(yellow), or absent entirely (grey). The on-screen keyboard mirrors the same
colour coding so the player can track which letters remain useful.

---

## Features

## Features

- **Multi-Length Word Support** — Play with 4, 5, 6, or 7-letter words, supported by over 37,000 curated words partitioned across dedicated dictionary tiers.
- **50-Level Campaign Mode** — Journey through 4 progressive worlds (Beginner's Glade, The Lexicon Labyrinth, Semantic Citadel, Master's Sanctum) with 1 to 3 star ratings.
- **Timed Rush Mode** — 60-second high-speed puzzle challenge with dynamic countdown progress bars and personal best tracking.
- **Procedural Low-Latency Audio Engine** — Built from scratch with Android `AudioTrack` direct PCM synthesis. Zero audio file dependencies, instant response for key presses, rising chord arpeggios on tile reveal, victory fanfares, and error thuds.
- **Daily Word Puzzle** — Deterministically selected daily target word shared across all players globally.
- **Hard Mode Rule Enforcement** — Option to mandate that revealed hints (correct and misplaced letters) must be used in subsequent guesses.
- **Wordle Bot Candidate Counter** — Live computation indicating how many possible solutions remain after each guess.
- **Player XP & Lexicographer Ranks** — Gain XP for every puzzle solved, unlocking progressive titles from Novice to Grand Lexicographer.
- **16 Achievement Badges** — Milestone unlocks for winning streaks, multi-length word mastery, flawless solves, and speed records.
- **Bottom Navigation Hub** — Clean Material 3 navigation bar connecting the Home Dashboard, Campaign Levels, Active Play, Statistics, and Badge Showcase.
- **Haptic Feedback & Sound Toggles** — Custom tactile responses and audio settings configurable in the Settings menu.
- **Dark Mode & Offline First** — Instant light/dark theme switching and complete offline playability with Room and DataStore persistence.

---

## Tech Stack

| Layer | Library / Tool |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, Clean Architecture (UI / Domain / Data) |
| State Management | ViewModel + StateFlow |
| Dependency Injection | Hilt (Dagger) |
| Local Persistence | Room (Campaign levels, achievements, game history) + DataStore Preferences |
| Audio Engine | Low-latency procedural PCM synthesis via Android AudioTrack |
| Networking | Retrofit 2 + OkHttp 4 |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| Testing | JUnit 4, Kotlin Coroutines Test, Compose UI Test |
| Build | Gradle Kotlin DSL, AGP 8.5, KSP |

---

## Project Structure

```
app/src/main/java/com/lexiguess/app/
  domain/
    GameEngine.kt              Evaluation engine (multi-length, hard mode, candidate count)
    model/
      TileState.kt             Enum: EMPTY, FILLED, CORRECT, MISPLACED, ABSENT
      GameStatus.kt            Enum: IN_PROGRESS, WON, LOST
      GameState.kt             Immutable UI snapshot with timer, bot clues, and length
      GameMode.kt              Enum: DAILY, PRACTICE, TIMED_RUSH, LEVEL, DUEL
  data/
    db/
      GameRecord.kt            Room entity for match history
      GameDao.kt               DAO: match streaks, win rates, distribution
      LevelRecord.kt           Room entity for 50 campaign levels
      LevelDao.kt              DAO: campaign progression and stars
      AchievementRecord.kt     Room entity for 16 badges
      AchievementDao.kt        DAO: badge unlocks and progress
      AppDatabase.kt           Room database (v2)
    network/
      WordApiService.kt        Retrofit interface for dictionary updates
      NetworkModule.kt         Hilt module: OkHttp + Retrofit
    repository/
      WordRepository.kt        Multi-tier dictionary loader + campaign/badge seeder
      GameRepository.kt        Match history persistence
      PlayerPreferences.kt     DataStore: XP, settings, audio, timed rush records
    di/
      DatabaseModule.kt        Hilt module: Room DAOs
      EngineModule.kt          Hilt module: GameEngine singleton
  ui/
    audio/
      SoundManager.kt          Procedural PCM sound synthesizer (clicks, arpeggios, fanfares)
    theme/
      Color.kt                 Wordle palette (light + dark)
      Type.kt                  Typography
      Theme.kt                 Material 3 theme
    composable/
      TileGrid.kt              Responsive grid with tile flip arpeggio sound callbacks
      WordleKeyboard.kt        Proportionally weighted keyboard with haptic feedback
      HintButton.kt            Single-use hint button
      ShareButton.kt           Wordle-style share formatter
      StatsChart.kt            Horizontal bar chart for guess distribution
      GameOverSheet.kt         End-game modal sheet with definitions and retry options
    screen/
      HomeScreen.kt            Main dashboard with XP rank, daily card, and mode launcher
      LevelsScreen.kt          50 campaign levels across 4 worlds
      AchievementsScreen.kt    16 milestone badges with unlock progress
      GameScreen.kt            Gameplay board with countdown timer and candidate tracker
      StatsScreen.kt           Comprehensive player statistics, win rates, and streaks
      SettingsScreen.kt        Audio, haptics, hard mode, and theme toggles
    navigation/
      NavGraph.kt              Material 3 NavigationBar and Compose NavHost
    viewmodel/
      GameViewModel.kt         Game lifecycle, timer, audio, XP awards, and state
      StatsViewModel.kt        Aggregated stats and Room flows
  LexiGuessApp.kt              Application class with Hilt
  MainActivity.kt              Single-activity entry point
app/src/main/assets/
  words_4.txt                  3,176 4-letter words
  target_words.txt             2,315 curated 5-letter solution words
  valid_words.txt              12,972 valid 5-letter dictionary words
  words_6.txt                  9,180 6-letter words
  words_7.txt                  11,801 7-letter words
app/src/test/
  domain/GameEngineTest.kt     Unit test suite for evaluation and validation
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android device or emulator running API 24 or higher

### Clone and Build

```bash
git clone https://github.com/Dr-Rank1/Wordle.git
cd Wordle
./gradlew assembleDebug
```

Install directly on a connected device or running emulator:

```bash
./gradlew installDebug
```

### Run Unit Tests

```bash
./gradlew testDebugUnitTest
```

---

## Contributing

Contributions and feedback are welcome. Please ensure all unit tests pass before submitting pull requests.

---

## License

MIT License. See [LICENSE](LICENSE) for full text.
