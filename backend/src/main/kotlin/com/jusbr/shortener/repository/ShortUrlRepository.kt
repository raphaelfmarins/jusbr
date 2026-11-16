package com.jusbr.shortener.repository

import com.jusbr.shortener.domain.ShortUrl
import org.springframework.data.jpa.repository.JpaRepository

interface ShortUrlRepository : JpaRepository<ShortUrl, Long> {
    fun findByCode(code: String): ShortUrl?
}
