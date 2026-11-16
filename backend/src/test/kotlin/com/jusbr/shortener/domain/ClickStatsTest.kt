package com.jusbr.shortener.domain

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class ClickStatsTest {

    private val today = LocalDate.parse("2026-11-16")

    @Test
    fun `fills last 30 days ascending with zeros`() {
        val counts = mapOf(today to 3L, today.minusDays(29) to 1L, today.minusDays(30) to 99L)

        val result = ClickStats.dailyCounts(counts, today)

        assertEquals(30, result.size)
        assertEquals(DailyCount(today.minusDays(29), 1), result.first())
        assertEquals(DailyCount(today, 3), result.last())
        assertEquals(4, result.sumOf { it.count })
    }

    @Test
    fun `groups null and blank referrers as direct and sorts desc`() {
        val counts = listOf<Pair<String?, Long>>(null to 2, "" to 1, "  " to 1, "https://google.com" to 3, "https://x.com" to 5)

        val result = ClickStats.topReferrers(counts)

        assertEquals(
            listOf(ReferrerCount("https://x.com", 5), ReferrerCount("direct", 4), ReferrerCount("https://google.com", 3)),
            result,
        )
    }

    @Test
    fun `limits top referrers to 10`() {
        val counts = (1..15).map { "https://site$it.com" to it.toLong() }

        val result = ClickStats.topReferrers(counts)

        assertEquals(10, result.size)
        assertEquals(ReferrerCount("https://site15.com", 15), result.first())
    }
}
