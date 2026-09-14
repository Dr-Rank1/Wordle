package com.lexiguess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.lexiguess.app.ui.navigation.LexiGuessNavGraph
import com.lexiguess.app.ui.theme.LexiGuessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Dark mode preference is persisted across recompositions via rememberSaveable
            var darkMode by rememberSaveable { mutableStateOf(false) }

            LexiGuessTheme(darkTheme = darkMode) {
                LexiGuessNavGraph(
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                )
            }
        }
    }
}
