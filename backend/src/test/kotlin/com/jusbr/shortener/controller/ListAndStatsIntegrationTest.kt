package com.jusbr.shortener.controller

import com.jusbr.shortener.TestcontainersConfiguration
import com.jusbr.shortener.domain.Click
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.repository.ClickRepository
import com.jusbr.shortener.repository.ShortUrlRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ListAndStatsIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var shortUrlRepository: ShortUrlRepository

    @Autowired
    lateinit var clickRepository: ClickRepository

    private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    @BeforeEach
    fun cleanDatabase() {
        clickRepository.deleteAll()
        shortUrlRepository.deleteAll()
    }

    private fun click(shortUrl: ShortUrl, at: Instant, referrer: String?) =
        clickRepository.save(Click(shortUrl.id!!, at, referrer, "JUnit", "127.0.0.1"))

    @Test
    fun `lists urls newest first with click counts`() {
        val older = shortUrlRepository.save(ShortUrl("Old1234", "https://old.com", now.minusSeconds(60), null))
        shortUrlRepository.save(ShortUrl("New1234", "https://new.com", now, now.plusSeconds(3600)))
        click(older, now, null)
        click(older, now, "https://google.com")

        mockMvc.get("/api/urls").andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(2))
            jsonPath("$[0].shortCode") { value("New1234") }
            jsonPath("$[0].clickCount") { value(0) }
            jsonPath("$[0].shortUrl") { value("http://localhost:8080/New1234") }
            jsonPath("$[0].analyticsUrl") { value("http://localhost:3000/analytics/New1234") }
            jsonPath("$[0].expiresAt") { value(now.plusSeconds(3600).toString()) }
            jsonPath("$[1].shortCode") { value("Old1234") }
            jsonPath("$[1].originalUrl") { value("https://old.com") }
            jsonPath("$[1].clickCount") { value(2) }
            jsonPath("$[1].createdAt") { value(now.minusSeconds(60).toString()) }
        }
    }

    @Test
    fun `returns stats for code`() {
        val shortUrl = shortUrlRepository.save(ShortUrl("Sta1234", "https://example.com", now.minus(60, ChronoUnit.DAYS), null))
        click(shortUrl, now, "https://google.com")
        click(shortUrl, now, "https://google.com")
        click(shortUrl, now, null)
        click(shortUrl, now.minus(5, ChronoUnit.DAYS), "")
        click(shortUrl, now.minus(40, ChronoUnit.DAYS), "https://old.com")
        val today = LocalDate.ofInstant(now, ZoneOffset.UTC)

        mockMvc.get("/api/stats/Sta1234").andExpect {
            status { isOk() }
            jsonPath("$.shortCode") { value("Sta1234") }
            jsonPath("$.originalUrl") { value("https://example.com") }
            jsonPath("$.totalClicks") { value(5) }
            jsonPath("$.clicksByDay", hasSize<Any>(30))
            jsonPath("$.clicksByDay[0].date") { value(today.minusDays(29).toString()) }
            jsonPath("$.clicksByDay[29].date") { value(today.toString()) }
            jsonPath("$.clicksByDay[29].count") { value(3) }
            jsonPath("$.clicksByDay[24].count") { value(1) }
            jsonPath("$.topReferrers[0].referrer") { value("direct") }
            jsonPath("$.topReferrers[0].count") { value(2) }
            jsonPath("$.topReferrers[1].referrer") { value("https://google.com") }
            jsonPath("$.topReferrers[1].count") { value(2) }
            jsonPath("$.topReferrers", hasSize<Any>(3))
        }
    }

    @Test
    fun `returns 404 stats for unknown code`() {
        mockMvc.get("/api/stats/Unknown").andExpect {
            status { isNotFound() }
            jsonPath("$.error") { isString() }
        }
    }
}
