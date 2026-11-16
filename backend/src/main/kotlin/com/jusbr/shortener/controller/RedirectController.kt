package com.jusbr.shortener.controller

import com.jusbr.shortener.service.ClickInfo
import com.jusbr.shortener.service.ShortUrlService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class RedirectController(private val shortUrlService: ShortUrlService) {

    @GetMapping("/{code:[A-Za-z0-9]{7}}")
    fun redirect(@PathVariable code: String, request: HttpServletRequest): ResponseEntity<Void> {
        val originalUrl = shortUrlService.resolve(code, clickInfo(request))
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(URI.create(originalUrl))
            .cacheControl(CacheControl.noStore())
            .build()
    }

    private fun clickInfo(request: HttpServletRequest) = ClickInfo(
        referrer = request.getHeader(HttpHeaders.REFERER),
        userAgent = request.getHeader(HttpHeaders.USER_AGENT),
        ip = request.getHeader("X-Forwarded-For")?.split(",")?.first()?.trim()?.ifBlank { null } ?: request.remoteAddr,
    )
}
