package com.rank.lexi.ui.navigation

object GameRoutes {
    const val PATTERN =
        "game?mode={mode}&length={length}&level={level}&word={word}&attempts={attempts}&epoch={epoch}"

    fun daily(): String = build(mode = "DAILY")

    fun archive(epochDay: Long): String = build(mode = "DAILY", epoch = epochDay.toString())

    fun practice(length: Int): String = build(mode = "PRACTICE", length = length.toString())

    fun practiceWord(word: String): String = build(mode = "PRACTICE", word = word.uppercase())

    fun rush(): String = build(mode = "TIMED_RUSH")

    fun level(levelNumber: Int): String = build(mode = "LEVEL", level = levelNumber.toString())

    fun custom(word: String, attempts: Int): String =
        build(mode = "CUSTOM", word = word.uppercase(), attempts = attempts.toString())

    private fun build(
        mode: String,
        length: String = "",
        level: String = "",
        word: String = "",
        attempts: String = "",
        epoch: String = "",
    ): String {
        return "game?mode=$mode&length=$length&level=$level&word=$word&attempts=$attempts&epoch=$epoch"
    }
}
