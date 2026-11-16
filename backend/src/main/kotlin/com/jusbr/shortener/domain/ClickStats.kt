package com.jusbr.shortener.domain

import java.time.LocalDate

data class DailyCount(val date: LocalDate, val count: Long)

data class ReferrerCount(val referrer: String, val count: Long)

object ClickStats {

    const val DAYS = 30L
    private const val TOP_REFERRERS = 10
    private const val DIRECT = "direct"

    fun firstDay(today: LocalDate): LocalDate = today.minusDays(DAYS - 1)

    fun dailyCounts(countsByDay: Map<LocalDate, Long>, today: LocalDate): List<DailyCount> =
        (0 until DAYS).map { offset ->
            val day = firstDay(today).plusDays(offset)
            DailyCount(day, countsByDay[day] ?: 0)
        }

    fun topReferrers(countsByReferrer: List<Pair<String?, Long>>): List<ReferrerCount> =
        countsByReferrer
            .groupBy({ (referrer, _) -> referrer?.takeIf { it.isNotBlank() } ?: DIRECT }, { it.second })
            .map { (referrer, counts) -> ReferrerCount(referrer, counts.sum()) }
            .sortedByDescending { it.count }
            .take(TOP_REFERRERS)
}
