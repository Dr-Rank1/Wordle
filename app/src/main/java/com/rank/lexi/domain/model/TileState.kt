package com.rank.lexi.domain.model

/** The visual/result state of a single tile in the grid. */
enum class TileState {
    /** Letter not yet evaluated (empty or currently-being-typed row). */
    EMPTY,
    /** Letter typed but row not yet submitted. */
    FILLED,
    /** Letter is in the correct position (green). */
    CORRECT,
    /** Letter is in the word but in the wrong position (yellow). */
    MISPLACED,
    /** Letter is not in the word (grey). */
    ABSENT;

    fun priority(): Int = when (this) {
        CORRECT -> 3
        MISPLACED -> 2
        ABSENT -> 1
        FILLED, EMPTY -> 0
    }
}
