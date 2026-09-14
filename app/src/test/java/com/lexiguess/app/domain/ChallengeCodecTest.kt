package com.lexiguess.app.domain

import org.junit.Assert.*
import org.junit.Test

class ChallengeCodecTest {

    @Test
    fun `encode and decode preserves 5-letter word and max attempts`() {
        val word = "CRANE"
        val code = ChallengeCodec.encode(word, 6)

        assertTrue(code.startsWith("LX-"))
        val decoded = ChallengeCodec.decode(code)

        assertNotNull(decoded)
        assertEquals("CRANE", decoded?.word)
        assertEquals(5, decoded?.length)
        assertEquals(6, decoded?.maxAttempts)
    }

    @Test
    fun `encode and decode works for 4-letter, 6-letter, and 7-letter words`() {
        val words = listOf("BOLD", "CASTLE", "WARRIOR")
        for (w in words) {
            val code = ChallengeCodec.encode(w, 7)
            val decoded = ChallengeCodec.decode(code)
            assertNotNull(decoded)
            assertEquals(w, decoded?.word)
            assertEquals(w.length, decoded?.length)
            assertEquals(7, decoded?.maxAttempts)
        }
    }

    @Test
    fun `decode handles codes without LX- prefix`() {
        val code = ChallengeCodec.encode("SHINE")
        val stripped = code.removePrefix("LX-")
        val decoded = ChallengeCodec.decode(stripped)
        assertNotNull(decoded)
        assertEquals("SHINE", decoded?.word)
    }

    @Test
    fun `decode returns null for corrupted code`() {
        assertNull(ChallengeCodec.decode("INVALID"))
        assertNull(ChallengeCodec.decode("LX-123"))
        assertNull(ChallengeCodec.decode(""))
    }
}
