package com.jusbr.shortener.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "click")
class Click(
    @Column(name = "short_url_id", nullable = false)
    val shortUrlId: Long,
    @Column(name = "clicked_at", nullable = false)
    val clickedAt: Instant,
    @Column(length = 2048)
    val referrer: String?,
    @Column(name = "user_agent", length = 1024)
    val userAgent: String?,
    @Column(length = 64)
    val ip: String?,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
