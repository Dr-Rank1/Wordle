package com.rank.lexi.domain

/**
 * Data extracted from an encoded challenge code.
 */
data class ChallengeData(
    val word: String,
    val length: Int,
    val maxAttempts: Int = 6,
)

/**
 * Encodes and decodes custom word challenge codes.
 * Format: "LX-<encoded_hex>"
 */
object ChallengeCodec {

    private const val PREFIX = "LX-"
    private const val XOR_KEY = 0x4B // 'K'

    /**
     * Encodes a [word] and optional [maxAttempts] into a shareable challenge code.
     * Example: "CRANE" -> "LX-3809..."
     */
    fun encode(word: String, maxAttempts: Int = 6): String {
        val clean = word.trim().uppercase()
        require(clean.all { it in 'A'..'Z' }) { "Word must contain only English letters A-Z" }
        require(clean.length in 4..7) { "Word length must be between 4 and 7 characters" }

        val bytes = ByteArray(clean.length + 1)
        bytes[0] = ((clean.length shl 4) or (maxAttempts and 0x0F)).toByte()
        for (i in clean.indices) {
            bytes[i + 1] = (clean[i].code xor XOR_KEY xor (i * 7)).toByte()
        }

        val hex = bytes.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
        return "$PREFIX$hex"
    }

    fun buildShareableLink(word: String, maxAttempts: Int = 6): String =
        "https://lexiguess.app/c/${encode(word, maxAttempts)}"

    /**
     * Decodes an encoded challenge code or URL into [ChallengeData].
     * Returns null if the code format or checksum is invalid.
     */
    fun decode(rawInput: String): ChallengeData? {
        var clean = rawInput.trim()
        if (clean.contains("code=")) {
            clean = clean.substringAfter("code=").substringBefore("&").substringBefore(" ")
        } else if (clean.contains("/c/")) {
            clean = clean.substringAfter("/c/").substringBefore("?").substringBefore("/").substringBefore(" ")
        }
        val trimmed = clean.trim().uppercase()
        val hex = if (trimmed.startsWith(PREFIX)) {
            trimmed.removePrefix(PREFIX)
        } else {
            trimmed
        }

        if (hex.length < 4 || hex.length % 2 != 0) return null

        return try {
            val bytes = ByteArray(hex.length / 2)
            for (i in bytes.indices) {
                bytes[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }

            val header = bytes[0].toInt() and 0xFF
            val expectedLength = (header shr 4) and 0x0F
            val maxAttempts = header and 0x0F

            if (bytes.size - 1 != expectedLength || expectedLength !in 4..7) {
                return null
            }

            val chars = CharArray(expectedLength)
            for (i in 0 until expectedLength) {
                val decodedChar = (bytes[i + 1].toInt() xor XOR_KEY xor (i * 7)).toChar()
                if (decodedChar !in 'A'..'Z') return null
                chars[i] = decodedChar
            }

            val word = String(chars)
            val attempts = if (maxAttempts in 3..10) maxAttempts else 6
            ChallengeData(word = word, length = expectedLength, maxAttempts = attempts)
        } catch (_: Exception) {
            null
        }
    }
}
