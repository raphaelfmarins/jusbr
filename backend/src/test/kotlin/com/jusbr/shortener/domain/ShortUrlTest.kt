package com.jusbr.shortener.domain

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShortUrlTest {

    private val now = Instant.parse("2026-11-16T14:00:00Z")

    private fun shortUrl(expiresAt: Instant?) =
        ShortUrl(code = "abc1234", originalUrl = "https://example.com", createdAt = now.minusSeconds(60), expiresAt = expiresAt)

    @Test
    fun `never expires without expiration date`() {
        assertFalse(shortUrl(null).isExpired(now))
    }

    @Test
    fun `is not expired before expiration date`() {
        assertFalse(shortUrl(now.plusSeconds(1)).isExpired(now))
    }

    @Test
    fun `is expired at or after expiration date`() {
        assertTrue(shortUrl(now).isExpired(now))
        assertTrue(shortUrl(now.minusSeconds(1)).isExpired(now))
    }
}
