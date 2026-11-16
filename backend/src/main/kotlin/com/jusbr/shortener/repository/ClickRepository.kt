package com.jusbr.shortener.repository

import com.jusbr.shortener.domain.Click
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface DayCountRow {
    val day: String
    val total: Long
}

interface ReferrerCountRow {
    val referrer: String?
    val total: Long
}

interface ClickRepository : JpaRepository<Click, Long> {

    fun countByShortUrlId(shortUrlId: Long): Long

    @Query(
        value = """
        SELECT to_char(clicked_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS day, COUNT(*) AS total
        FROM click
        WHERE short_url_id = :shortUrlId AND clicked_at >= :from
        GROUP BY day
        """,
        nativeQuery = true,
    )
    fun countByDaySince(shortUrlId: Long, from: Instant): List<DayCountRow>

    @Query("select c.referrer as referrer, count(c) as total from Click c where c.shortUrlId = :shortUrlId group by c.referrer")
    fun countByReferrer(shortUrlId: Long): List<ReferrerCountRow>
}
