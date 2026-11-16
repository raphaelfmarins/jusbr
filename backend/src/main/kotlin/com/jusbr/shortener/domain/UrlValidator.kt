package com.jusbr.shortener.domain

import java.net.URI
import java.net.URISyntaxException

object UrlValidator {

    private const val MAX_LENGTH = 2048
    private val ALLOWED_SCHEMES = setOf("http", "https")

    fun isValid(url: String): Boolean {
        if (url.isBlank() || url.length > MAX_LENGTH) return false
        return try {
            val uri = URI(url)
            uri.scheme?.lowercase() in ALLOWED_SCHEMES && !uri.host.isNullOrBlank()
        } catch (e: URISyntaxException) {
            false
        }
    }
}
