package com.jusbr.shortener.service

import com.jusbr.shortener.domain.Click
import com.jusbr.shortener.domain.InvalidRequestException
import com.jusbr.shortener.domain.ShortCodeGenerator
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.domain.ShortUrlExpiredException
import com.jusbr.shortener.domain.ShortUrlNotFoundException
import com.jusbr.shortener.repository.ClickRepository
import com.jusbr.shortener.repository.ShortUrlRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals

class ShortUrlServiceTest {

    private val now = Instant.parse("2026-11-16T14:00:00Z")
    private val shortUrlRepository = mockk<ShortUrlRepository>()
    private val clickRepository = mockk<ClickRepository>()
    private val codeGenerator = mockk<ShortCodeGenerator>()
    private val service = ShortUrlService(shortUrlRepository, clickRepository, codeGenerator, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun `shortens valid url`() {
        every { codeGenerator.generate() } returns "abc1234"
        every { shortUrlRepository.saveAndFlush(any<ShortUrl>()) } answers { firstArg() }

        val result = service.shorten("https://example.com", now.plusSeconds(3600))

        assertEquals("abc1234", result.code)
        assertEquals("https://example.com", result.originalUrl)
        assertEquals(now, result.createdAt)
        assertEquals(now.plusSeconds(3600), result.expiresAt)
    }

    @Test
    fun `retries with new code on collision`() {
        every { codeGenerator.generate() } returnsMany listOf("dup0001", "new0001")
        every { shortUrlRepository.saveAndFlush(match<ShortUrl> { it.code == "dup0001" }) } throws DataIntegrityViolationException("dup")
        every { shortUrlRepository.saveAndFlush(match<ShortUrl> { it.code == "new0001" }) } answers { firstArg() }

        assertEquals("new0001", service.shorten("https://example.com", null).code)
    }

    @Test
    fun `fails after max collision attempts`() {
        every { codeGenerator.generate() } returns "dup0001"
        every { shortUrlRepository.saveAndFlush(any<ShortUrl>()) } throws DataIntegrityViolationException("dup")

        assertThrows<IllegalStateException> { service.shorten("https://example.com", null) }
        verify(exactly = ShortUrlService.MAX_ATTEMPTS) { shortUrlRepository.saveAndFlush(any<ShortUrl>()) }
    }

    @Test
    fun `rejects missing invalid url and past expiration`() {
        assertThrows<InvalidRequestException> { service.shorten(null, null) }
        assertThrows<InvalidRequestException> { service.shorten("ftp://example.com", null) }
        assertThrows<InvalidRequestException> { service.shorten("https://example.com", now) }
    }

    @Test
    fun `resolves url and records click`() {
        every { shortUrlRepository.findByCode("abc1234") } returns ShortUrl("abc1234", "https://example.com", now, null, id = 1)
        val click = slot<Click>()
        every { clickRepository.save(capture(click)) } answers { firstArg() }

        val url = service.resolve("abc1234", ClickInfo("https://google.com", "UA", "1.2.3.4"))

        assertEquals("https://example.com", url)
        assertEquals(1L, click.captured.shortUrlId)
        assertEquals(now, click.captured.clickedAt)
        assertEquals("https://google.com", click.captured.referrer)
        assertEquals("1.2.3.4", click.captured.ip)
    }

    @Test
    fun `throws not found for unknown code`() {
        every { shortUrlRepository.findByCode("unknown") } returns null

        assertThrows<ShortUrlNotFoundException> { service.resolve("unknown", ClickInfo(null, null, null)) }
    }

    @Test
    fun `throws expired without recording click`() {
        every { shortUrlRepository.findByCode("abc1234") } returns ShortUrl("abc1234", "https://example.com", now.minusSeconds(10), now, id = 1)

        assertThrows<ShortUrlExpiredException> { service.resolve("abc1234", ClickInfo(null, null, null)) }
        verify(exactly = 0) { clickRepository.save(any()) }
    }
}
