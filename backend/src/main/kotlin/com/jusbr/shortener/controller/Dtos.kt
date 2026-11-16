package com.jusbr.shortener.controller

import com.jusbr.shortener.config.AppProperties
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.repository.ShortUrlWithClicks
import java.time.Instant

data class ShortenRequest(val url: String?, val expiresAt: Instant?)

data class ShortUrlResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val analyticsUrl: String,
) {
    companion object {
        fun from(shortUrl: ShortUrl, props: AppProperties) = ShortUrlResponse(
            shortCode = shortUrl.code,
            shortUrl = props.shortUrl(shortUrl.code),
            originalUrl = shortUrl.originalUrl,
            createdAt = shortUrl.createdAt,
            expiresAt = shortUrl.expiresAt,
            analyticsUrl = props.analyticsUrl(shortUrl.code),
        )
    }
}

data class ShortUrlSummaryResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val analyticsUrl: String,
    val clickCount: Long,
) {
    companion object {
        fun from(row: ShortUrlWithClicks, props: AppProperties) = ShortUrlResponse.from(row.shortUrl, props).let {
            ShortUrlSummaryResponse(it.shortCode, it.shortUrl, it.originalUrl, it.createdAt, it.expiresAt, it.analyticsUrl, row.clickCount)
        }
    }
}

data class ErrorResponse(val error: String)
