package com.jusbr.shortener.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShortCodeGeneratorTest {

    private val generator = ShortCodeGenerator()

    @Test
    fun `generates 7 base62 characters`() {
        repeat(100) {
            val code = generator.generate()
            assertEquals(7, code.length)
            assertTrue(code.matches(Regex("[A-Za-z0-9]{7}")))
        }
    }

    @Test
    fun `generates distinct codes`() {
        val codes = (1..1000).map { generator.generate() }.toSet()
        assertTrue(codes.size > 990)
    }
}
