package com.jusbr.shortener.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class AppProperties(val baseUrl: String, val frontendUrl: String) {
    fun shortUrl(code: String) = "${baseUrl.removeSuffix("/")}/$code"
    fun analyticsUrl(code: String) = "${frontendUrl.removeSuffix("/")}/analytics/$code"
}
