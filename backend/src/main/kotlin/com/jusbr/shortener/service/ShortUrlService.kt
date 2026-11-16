package com.jusbr.shortener.service

import com.jusbr.shortener.domain.Click
import com.jusbr.shortener.domain.InvalidRequestException
import com.jusbr.shortener.domain.ShortCodeGenerator
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.domain.ShortUrlExpiredException
import com.jusbr.shortener.domain.ShortUrlNotFoundException
import com.jusbr.shortener.domain.UrlValidator
import com.jusbr.shortener.repository.ClickRepository
import com.jusbr.shortener.repository.ShortUrlRepository
import com.jusbr.shortener.repository.ShortUrlWithClicks
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ClickInfo(val referrer: String?, val userAgent: String?, val ip: String?)

@Service
class ShortUrlService(
    private val shortUrlRepository: ShortUrlRepository,
    private val clickRepository: ClickRepository,
    private val codeGenerator: ShortCodeGenerator,
    private val clock: Clock,
) {

    fun shorten(url: String?, expiresAt: Instant?): ShortUrl {
        if (url.isNullOrBlank()) throw InvalidRequestException("url is required")
        if (!UrlValidator.isValid(url)) throw InvalidRequestException("url must be a valid http or https URL")
        val now = now()
        if (expiresAt != null && !expiresAt.isAfter(now)) throw InvalidRequestException("expiresAt must be in the future")

        repeat(MAX_ATTEMPTS) {
            try {
                return shortUrlRepository.saveAndFlush(ShortUrl(codeGenerator.generate(), url, now, expiresAt))
            } catch (e: DataIntegrityViolationException) {
                // code collision: retry with a new code
            }
        }
        throw IllegalStateException("Could not generate a unique short code after $MAX_ATTEMPTS attempts")
    }

    fun resolve(code: String, clickInfo: ClickInfo): String {
        val shortUrl = findByCode(code)
        val now = now()
        if (shortUrl.isExpired(now)) throw ShortUrlExpiredException(code)
        clickRepository.save(Click(shortUrl.id!!, now, clickInfo.referrer, clickInfo.userAgent, clickInfo.ip))
        return shortUrl.originalUrl
    }

    fun listWithClickCount(): List<ShortUrlWithClicks> = shortUrlRepository.findAllWithClickCount()

    fun findByCode(code: String): ShortUrl =
        shortUrlRepository.findByCode(code) ?: throw ShortUrlNotFoundException(code)

    private fun now(): Instant = clock.instant().truncatedTo(ChronoUnit.MILLIS)

    companion object {
        const val MAX_ATTEMPTS = 5
    }
}
