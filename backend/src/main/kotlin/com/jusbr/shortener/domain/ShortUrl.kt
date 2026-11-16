package com.jusbr.shortener.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "short_url")
class ShortUrl(
    @Column(nullable = false, unique = true, length = 7)
    val code: String,
    @Column(name = "original_url", nullable = false, length = 2048)
    val originalUrl: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "expires_at")
    val expiresAt: Instant? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun isExpired(now: Instant): Boolean = expiresAt != null && !now.isBefore(expiresAt)
}
