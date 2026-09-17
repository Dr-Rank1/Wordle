package com.rank.lexi.ui.composable

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rank.lexi.domain.model.GameState
import com.rank.lexi.domain.model.GameStatus
import com.rank.lexi.ui.util.ShareResult

@Composable
fun ShareButton(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    if (state.status == GameStatus.IN_PROGRESS) return

    val context = LocalContext.current

    Button(
        onClick = {
            runCatching { ShareResult.share(context, state) }.onFailure {
                val text = ShareResult.buildShareText(state)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Share your result"))
            }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share",
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Share",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
