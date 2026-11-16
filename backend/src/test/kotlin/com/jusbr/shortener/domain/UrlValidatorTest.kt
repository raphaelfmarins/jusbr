package com.jusbr.shortener.domain

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["http://example.com", "https://example.com/path?q=1#x", "https://sub.domain.com.br:8443/a"])
    fun `accepts http and https urls with host`(url: String) {
        assertTrue(UrlValidator.isValid(url))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "example.com", "ftp://example.com", "javascript:alert(1)", "http://", "https:///path", "http://exa mple.com", "mailto:a@b.com"])
    fun `rejects invalid urls`(url: String) {
        assertFalse(UrlValidator.isValid(url))
    }

    @org.junit.jupiter.api.Test
    fun `rejects urls longer than 2048 chars`() {
        assertFalse(UrlValidator.isValid("https://example.com/" + "a".repeat(2048)))
    }
}
