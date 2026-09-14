package com.lexiguess.app.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexiguess.app.data.repository.WordRepository
import com.lexiguess.app.domain.ChallengeCodec
import com.lexiguess.app.domain.ChallengeData
import com.lexiguess.app.ui.theme.TileCorrect
import com.lexiguess.app.ui.theme.TileMisplaced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomChallengeScreen(
    wordRepository: WordRepository,
    onStartChallengeGame: (word: String, maxAttempts: Int) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Create tab state
    var secretWordInput by remember { mutableStateOf("") }
    var maxAttempts by remember { mutableIntStateOf(6) }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var wordError by remember { mutableStateOf<String?>(null) }

    // Play tab state
    var challengeCodeInput by remember { mutableStateOf("") }
    var decodedChallenge by remember { mutableStateOf<ChallengeData?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CUSTOM PUZZLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TileCorrect,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = "Challenge a Friend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Create Challenge", fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Play Challenge", fontWeight = FontWeight.Bold) },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (selectedTab == 0) {
                    // CREATE CHALLENGE
                    Text(
                        text = "Create a custom secret word challenge and share the code with friends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = secretWordInput,
                        onValueChange = {
                            val filtered = it.filter { ch -> ch.isLetter() }.uppercase()
                            if (filtered.length <= 7) {
                                secretWordInput = filtered
                                wordError = null
                                generatedCode = null
                            }
                        },
                        label = { Text("Secret Word (4 to 7 letters)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = wordError != null,
                        supportingText = {
                            wordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                ?: Text("${secretWordInput.length}/7 letters")
                        },
                    )

                    // Attempts Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Allowed Guesses: $maxAttempts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            listOf(4, 5, 6, 7, 8).forEach { count ->
                                FilterChip(
                                    selected = maxAttempts == count,
                                    onClick = { maxAttempts = count },
                                    label = { Text("$count") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val word = secretWordInput.trim().uppercase()
                            if (word.length !in 4..7) {
                                wordError = "Word must be between 4 and 7 letters"
                                return@Button
                            }
                            if (!wordRepository.isValidWord(word, word.length)) {
                                wordError = "'$word' is not in the valid dictionary"
                                return@Button
                            }
                            wordError = null
                            generatedCode = ChallengeCodec.encode(word, maxAttempts)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Challenge Code", fontWeight = FontWeight.Bold)
                    }

                    generatedCode?.let { code ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "CHALLENGE CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TileCorrect,
                                )

                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Challenge Code", code))
                                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy")
                                    }

                                    Button(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "I challenged you to a secret word in LexiGuess!\nCan you solve it?\n\nCode: $code\nPaste this code in LexiGuess -> Custom Puzzle to play!",
                                                )
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Challenge"))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // PLAY CHALLENGE
                    Text(
                        text = "Enter a challenge code sent by a friend to start their custom puzzle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = challengeCodeInput,
                        onValueChange = {
                            challengeCodeInput = it.uppercase()
                            codeError = null
                            decodedChallenge = ChallengeCodec.decode(it)
                        },
                        label = { Text("Paste Challenge Code (e.g. LX-...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = codeError != null,
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    challengeCodeInput = clip.trim().uppercase()
                                    decodedChallenge = ChallengeCodec.decode(challengeCodeInput)
                                    if (decodedChallenge == null) {
                                        codeError = "Invalid challenge code"
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste")
                            }
                        },
                        supportingText = {
                            codeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                    )

                    decodedChallenge?.let { challenge ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TileCorrect.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Puzzle Verified",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TileCorrect,
                                )
                                Text(
                                    text = "• Word Length: ${challenge.length} letters\n• Maximum Guesses: ${challenge.maxAttempts} attempts",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onStartChallengeGame(challenge.word, challenge.maxAttempts)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TileCorrect),
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play Custom Challenge", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
