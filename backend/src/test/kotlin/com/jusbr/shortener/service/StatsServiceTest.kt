package com.jusbr.shortener.service

import com.jusbr.shortener.domain.DailyCount
import com.jusbr.shortener.domain.ReferrerCount
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.repository.ClickRepository
import com.jusbr.shortener.repository.DayCountRow
import com.jusbr.shortener.repository.ReferrerCountRow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals

class StatsServiceTest {

    private val now = Instant.parse("2026-11-16T14:00:00Z")
    private val shortUrlService = mockk<ShortUrlService>()
    private val clickRepository = mockk<ClickRepository>()
    private val service = StatsService(shortUrlService, clickRepository, Clock.fixed(now, ZoneOffset.UTC))

    private fun dayRow(day: String, total: Long) = object : DayCountRow {
        override val day = day
        override val total = total
    }

    private fun referrerRow(referrer: String?, total: Long) = object : ReferrerCountRow {
        override val referrer = referrer
        override val total = total
    }

    @Test
    fun `builds stats from aggregated counts`() {
        every { shortUrlService.findByCode("abc1234") } returns ShortUrl("abc1234", "https://example.com", now, null, id = 7)
        every { clickRepository.countByShortUrlId(7) } returns 6
        every { clickRepository.countByDaySince(7, Instant.parse("2026-10-18T00:00:00Z")) } returns listOf(dayRow("2026-11-16", 4))
        every { clickRepository.countByReferrer(7) } returns listOf(referrerRow(null, 5), referrerRow("https://google.com", 1))

        val stats = service.statsFor("abc1234")

        assertEquals(6, stats.totalClicks)
        assertEquals(30, stats.clicksByDay.size)
        assertEquals(DailyCount(LocalDate.parse("2026-10-18"), 0), stats.clicksByDay.first())
        assertEquals(DailyCount(LocalDate.parse("2026-11-16"), 4), stats.clicksByDay.last())
        assertEquals(listOf(ReferrerCount("direct", 5), ReferrerCount("https://google.com", 1)), stats.topReferrers)
    }
}
