package com.example.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.wordle.ui.WordleScreen

class MainActivity : ComponentActivity() {
    private val viewModel: WordleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordleScreen(viewModel)
        }
    }
}
