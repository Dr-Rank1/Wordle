package com.rank.lexi.domain.model

/**
 * Represents a purchasable item in the LexiGuess shop.
 */
sealed class ShopItem(
    val id: String,
    val displayName: String,
    val description: String,
    val emoji: String,
    val coinCost: Int,
    val transactionType: String,
) {
    /** Adds one extra guess row to the current game. */
    object ExtraGuess : ShopItem(
        id = "EXTRA_GUESS",
        displayName = "Extra Guess",
        description = "Add one more guess row to your current game.",
        emoji = "➕",
        coinCost = 80,
        transactionType = "SPEND_EXTRA_GUESS",
    )

    /** Auto-reveals one unknown letter in the correct position. */
    object RevealLetter : ShopItem(
        id = "REVEAL_LETTER",
        displayName = "Reveal a Letter",
        description = "Instantly reveal one hidden letter in the correct position.",
        emoji = "💡",
        coinCost = 60,
        transactionType = "SPEND_REVEAL",
    )

    /** Skips the current word and starts fresh with a new one. */
    object SkipWord : ShopItem(
        id = "SKIP_WORD",
        displayName = "Skip Word",
        description = "Too tough? Replace the current word with a new one.",
        emoji = "⏭️",
        coinCost = 120,
        transactionType = "SPEND_SKIP",
    )

    /** Adds one streak freeze to protect a losing streak day. */
    object StreakFreeze : ShopItem(
        id = "STREAK_FREEZE",
        displayName = "Streak Freeze",
        description = "Bank an extra streak freeze shield for a missed day.",
        emoji = "🛡️",
        coinCost = 100,
        transactionType = "SPEND_FREEZE",
    )

    companion object {
        val allItems: List<ShopItem> = listOf(ExtraGuess, RevealLetter, SkipWord, StreakFreeze)
        val COIN_REWARD_PER_AD = 50
    }
}
