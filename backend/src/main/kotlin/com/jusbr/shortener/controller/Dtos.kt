package com.jusbr.shortener.controller

import com.jusbr.shortener.config.AppProperties
import com.jusbr.shortener.domain.ShortUrl
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

data class ErrorResponse(val error: String)
