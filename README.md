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

- **Daily word puzzle** — a new target word is chosen each day, derived
  deterministically from the date so the same word appears on every device.
- **Hybrid word source** — ships with a curated local word list; automatically
  fetches the latest list from the GitHub Wordle repository in the background
  whenever the device is online. Falls back silently to the local list if the
  network is unavailable.
- **Tile flip animations** — each submitted row flips its tiles one column at
  a time to reveal the result, matching the feel of the original game.
- **Shake animation** — the current row shakes when the player submits a word
  that is too short or not in the word list.
- **Hint system** — one hint per game reveals the correct letter for the first
  unsolved position. The button disables itself once used.
- **Share results** — generates a standard Wordle-style emoji grid and sends it
  to the Android share sheet with a single tap.
- **Dark mode** — a toggle in Settings switches between light and dark themes
  instantly. The preference is remembered across sessions.
- **Full game history** — every completed game is stored in a local Room
  database. A Statistics screen shows total games, win percentage, current
  streak, best streak, and a guess-distribution bar chart.
- **Session restore** — if the app is closed mid-game, progress is fully
  restored on next launch using DataStore.
- **No ads, no tracking** — no analytics or advertising SDKs. The only network
  request is the optional word-list refresh from GitHub.

---

## Tech Stack

| Layer | Library / Tool |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, clean layering (UI / Domain / Data) |
| State management | ViewModel + StateFlow |
| Dependency injection | Hilt |
| Local persistence | Room (game history) + DataStore Preferences (active state) |
| Networking | Retrofit 2 + OkHttp 4 |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose |
| Testing | JUnit 4, Kotlin Coroutines Test, Compose UI Test |
| Build | Gradle Kotlin DSL, AGP 8.5, KSP |

---

## Project Structure

```
app/src/main/java/com/lexiguess/app/
  domain/
    GameEngine.kt              Pure-Kotlin evaluation engine (no Android deps)
    model/
      TileState.kt             Enum: EMPTY, FILLED, CORRECT, MISPLACED, ABSENT
      GameStatus.kt            Enum: IN_PROGRESS, WON, LOST
      GameState.kt             Immutable UI snapshot
  data/
    db/
      GameRecord.kt            Room entity
      GameDao.kt               DAO: history, streaks, distribution queries
      AppDatabase.kt           Room database
    network/
      WordApiService.kt        Retrofit interface (GitHub raw content API)
      NetworkModule.kt         Hilt module: OkHttp + Retrofit
    repository/
      WordRepository.kt        Hybrid word source (local asset + remote API)
      GameRepository.kt        DataStore (active state) + Room (history)
    di/
      DatabaseModule.kt        Hilt module: Room + DAO
      EngineModule.kt          Hilt module: GameEngine singleton
  ui/
    theme/
      Color.kt                 Wordle palette (light + dark)
      Type.kt                  Typography
      Theme.kt                 Material 3 theme with LocalDarkMode
    composable/
      TileGrid.kt              6x5 board with flip + shake animations
      WordleKeyboard.kt        Colour-coded on-screen keyboard
      HintButton.kt            Single-use hint button
      ShareButton.kt           Emoji grid + Android share sheet
      StatsChart.kt            Horizontal bar chart for guess distribution
    screen/
      GameScreen.kt            Main game screen
      StatsScreen.kt           Statistics and history screen
      SettingsScreen.kt        Dark mode toggle
    navigation/
      NavGraph.kt              Navigation Compose graph
    viewmodel/
      GameViewModel.kt         Game logic, input, restore, hint, shake
      StatsViewModel.kt        Aggregated stats from Room
  LexiGuessApp.kt              @HiltAndroidApp Application class
  MainActivity.kt              Entry point with Hilt + Navigation Compose
app/src/main/assets/
  words.txt                    Bundled offline word list
app/src/test/
  domain/GameEngineTest.kt     Unit tests for the evaluation engine
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
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
./gradlew test
```

### Run Compose UI Tests (requires emulator or device)

```bash
./gradlew connectedAndroidTest
```

---

## Word Source

LexiGuess uses a two-tier strategy:

1. **Offline (bundled)** — `app/src/main/assets/words.txt` contains a curated
   list of five-letter English words that is always available without any
   network access.
2. **Online (GitHub API)** — at app launch, if a network connection is
   present, the app fetches the word list from
   [tabatkins/wordle-list](https://github.com/tabatkins/wordle-list) via the
   GitHub raw content API and merges any new words into the live pool.

If the network fetch fails for any reason the app continues silently with the
bundled list. The target word for the day is always derived deterministically
from the merged pool.

---

## How Evaluation Works

Each guess is evaluated in two passes against the target word:

1. **Correct positions** — every letter that matches the target at the same
   index is marked green and that position in the target is consumed.
2. **Misplaced letters** — for each remaining guess letter, if it exists
   anywhere in the remaining (unconsumed) target letters it is marked yellow
   and that occurrence is consumed.
3. Everything else is marked grey.

This mirrors the official Wordle rules and handles duplicate letters correctly.

---

## Roadmap

- Hard mode (every subsequent guess must use all revealed hints)
- Infinite mode (unlimited rounds in a single session)
- Additional languages
- Improved accessibility (screen-reader labels, high-contrast mode)
- Widget showing today's progress on the home screen

---

## Contributing

Pull requests are welcome. For significant changes, open an issue first to
discuss the proposed approach. Make sure all existing tests pass before
submitting.

---

## License

MIT License. See [LICENSE](LICENSE) for full text.
