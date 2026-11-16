package com.jusbr.shortener.controller

import com.jusbr.shortener.config.AppProperties
import com.jusbr.shortener.service.ShortUrlService
import com.jusbr.shortener.service.StatsService
import com.jusbr.shortener.service.UrlStats
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ShortUrlController(
    private val shortUrlService: ShortUrlService,
    private val statsService: StatsService,
    private val appProperties: AppProperties,
) {

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    fun shorten(@RequestBody request: ShortenRequest): ShortUrlResponse =
        ShortUrlResponse.from(shortUrlService.shorten(request.url, request.expiresAt), appProperties)

    @GetMapping("/urls")
    fun list(): List<ShortUrlSummaryResponse> =
        shortUrlService.listWithClickCount().map { ShortUrlSummaryResponse.from(it, appProperties) }

    @GetMapping("/stats/{code}")
    fun stats(@PathVariable code: String): UrlStats = statsService.statsFor(code)
}
