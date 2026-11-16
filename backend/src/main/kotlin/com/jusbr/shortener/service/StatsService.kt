package com.jusbr.shortener.service

import com.jusbr.shortener.domain.ClickStats
import com.jusbr.shortener.domain.DailyCount
import com.jusbr.shortener.domain.ReferrerCount
import com.jusbr.shortener.repository.ClickRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

data class UrlStats(
    val shortCode: String,
    val originalUrl: String,
    val totalClicks: Long,
    val clicksByDay: List<DailyCount>,
    val topReferrers: List<ReferrerCount>,
)

@Service
class StatsService(
    private val shortUrlService: ShortUrlService,
    private val clickRepository: ClickRepository,
    private val clock: Clock,
) {

    fun statsFor(code: String): UrlStats {
        val shortUrl = shortUrlService.findByCode(code)
        val id = shortUrl.id!!
        val today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)
        val from = ClickStats.firstDay(today).atStartOfDay().toInstant(ZoneOffset.UTC)

        val countsByDay = clickRepository.countByDaySince(id, from).associate { LocalDate.parse(it.day) to it.total }
        val referrerCounts = clickRepository.countByReferrer(id).map { it.referrer to it.total }

        return UrlStats(
            shortCode = shortUrl.code,
            originalUrl = shortUrl.originalUrl,
            totalClicks = clickRepository.countByShortUrlId(id),
            clicksByDay = ClickStats.dailyCounts(countsByDay, today),
            topReferrers = ClickStats.topReferrers(referrerCounts),
        )
    }
}
