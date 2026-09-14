# LexiGuess

A modern, word-guessing Android game inspired by the original Wordle puzzle.
Built entirely in Kotlin with Jetpack Compose, LexiGuess challenges players to
identify a hidden five-letter word within six attempts. The app works fully
offline and silently refreshes its word pool from a public GitHub Wordle API
whenever a network connection is available.

---

## Features

- **Wordle-style gameplay** – six attempts, five letters, instant colour-coded
  feedback on every guess.
- **Hybrid word source** – ships with a bundled local word list; fetches fresh
  words from a public GitHub Wordle API in the background when online.
- **Hint system** – a single hint per game reveals one correct letter without
  penalising the player's streak.
- **Share results** – copy or share a standard Wordle-style emoji grid to any
  app on the device.
- **Dark mode** – automatic system-based and manual dark/light toggle baked
  into Material 3 theming.
- **Full game history** – every completed game is persisted in a local Room
  database; browse past results in a dedicated Statistics screen.
- **Streak tracking** – current streak, best streak, win percentage, and guess
  distribution chart.
- **No analytics, no ads** – completely private; no data leaves the device
  except the single API call to refresh the word list.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + clean layering (UI / Domain / Data) |
| State | ViewModel + StateFlow |
| Local persistence | Room (game history) + DataStore Preferences (active game state) |
| Networking | Retrofit 2 + OkHttp (word list refresh) |
| Async | Kotlin Coroutines + Flow |
| Dependency injection | Hilt |
| Testing | JUnit 4, Mockito-Kotlin, Compose UI Test |

---

## Project Structure

```
app/
  src/
    main/
      java/com/lexiguess/
        data/
          db/          Room database, DAO, entities
          network/     Retrofit service and DTOs
          repository/  WordRepository (hybrid: local + remote)
        domain/
          GameEngine.kt   Pure-Kotlin guess evaluation logic
          model/          GameState, GuessResult, TileState
        ui/
          screen/
            GameScreen.kt
            StatsScreen.kt
          composable/
            Grid.kt
            Keyboard.kt
            HintButton.kt
            ShareButton.kt
          theme/
            Theme.kt
            Color.kt
            Type.kt
          viewmodel/
            GameViewModel.kt
        MainActivity.kt
      assets/
        words.txt          Bundled offline word list
    test/                  Unit tests (GameEngine, Repository)
    androidTest/           Compose UI tests
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- An Android device or emulator running API 24+

### Clone and Build

```bash
git clone https://github.com/Dr-Rank1/Wordle.git
cd Wordle
./gradlew assembleDebug
```

Install on a connected device:

```bash
./gradlew installDebug
```

### Run Tests

Unit tests:

```bash
./gradlew test
```

Compose UI tests (requires running emulator or device):

```bash
./gradlew connectedAndroidTest
```

---

## Word Source

LexiGuess uses a two-tier strategy for its word pool:

1. **Offline (bundled)** – `assets/words.txt` contains a curated list of
   five-letter English words that is always available without network access.
2. **Online (GitHub API)** – at launch, if the device is online, the app
   fetches the latest word list from the public
   [tabatkins/wordle-list](https://github.com/tabatkins/wordle-list) repository
   via the GitHub raw content API and merges it with the local list.

The target word for each session is chosen from the merged pool. If the network
call fails, the game falls back gracefully to the bundled list.

---

## Roadmap

- Hard mode (guesses must use revealed hints)
- Infinite mode (play unlimited rounds in one session)
- Multiple languages
- Accessibility improvements (screen-reader support, high-contrast palette)

---

## Contributing

Pull requests are welcome. For significant changes please open an issue first
to discuss the proposed approach. Make sure all tests pass before submitting.

---

## License

MIT License. See [LICENSE](LICENSE) for full text.
