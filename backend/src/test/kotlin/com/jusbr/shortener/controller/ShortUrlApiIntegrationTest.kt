package com.jusbr.shortener.controller

import com.jusbr.shortener.TestcontainersConfiguration
import com.jusbr.shortener.domain.ShortUrl
import com.jusbr.shortener.repository.ClickRepository
import com.jusbr.shortener.repository.ShortUrlRepository
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ShortUrlApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var shortUrlRepository: ShortUrlRepository

    @Autowired
    lateinit var clickRepository: ClickRepository

    @BeforeEach
    fun cleanDatabase() {
        clickRepository.deleteAll()
        shortUrlRepository.deleteAll()
    }

    private fun shorten(body: String) = mockMvc.post("/api/shorten") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    @Test
    fun `creates short url`() {
        val expiresAt = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)

        shorten("""{"url":"https://example.com/page","expiresAt":"$expiresAt"}""").andExpect {
            status { isCreated() }
            jsonPath("$.shortCode") { value(matchesPattern("[A-Za-z0-9]{7}")) }
            jsonPath("$.shortUrl") { value(matchesPattern("http://localhost:8080/[A-Za-z0-9]{7}")) }
            jsonPath("$.originalUrl") { value("https://example.com/page") }
            jsonPath("$.createdAt") { value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*Z")) }
            jsonPath("$.expiresAt") { value(expiresAt.toString()) }
            jsonPath("$.analyticsUrl") { value(matchesPattern("http://localhost:3000/analytics/[A-Za-z0-9]{7}")) }
        }
    }

    @Test
    fun `creates short url without expiration`() {
        shorten("""{"url":"http://example.com"}""").andExpect {
            status { isCreated() }
            jsonPath("$.expiresAt") { value(null as Any?) }
        }
    }

    @Test
    fun `rejects invalid requests with 400`() {
        listOf(
            """{"url":"not-a-url"}""",
            """{}""",
            """{"url":"https://example.com","expiresAt":"2000-01-01T00:00:00Z"}""",
            """{"url":"https://example.com","expiresAt":"garbage"}""",
        ).forEach { body ->
            shorten(body).andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { isString() }
            }
        }
    }

    @Test
    fun `redirects and records click`() {
        val shortUrl = shortUrlRepository.save(ShortUrl("Abc1234", "https://example.com/target", Instant.now(), null))

        mockMvc.get("/Abc1234") {
            header("Referer", "https://google.com")
            header("User-Agent", "JUnit")
            header("X-Forwarded-For", "10.0.0.1, 10.0.0.2")
        }.andExpect {
            status { isEqualTo(301) }
            header { string("Location", "https://example.com/target") }
            header { string("Cache-Control", "no-store") }
        }

        val click = clickRepository.findAll().single()
        assertEquals(shortUrl.id, click.shortUrlId)
        assertEquals("https://google.com", click.referrer)
        assertEquals("JUnit", click.userAgent)
        assertEquals("10.0.0.1", click.ip)
    }

    @Test
    fun `returns 404 for unknown code`() {
        mockMvc.get("/Zzzzzzz").andExpect {
            status { isNotFound() }
            jsonPath("$.error") { isString() }
        }
    }

    @Test
    fun `returns 410 for expired url without recording click`() {
        shortUrlRepository.save(ShortUrl("Exp1234", "https://example.com", Instant.now().minusSeconds(60), Instant.now().minusSeconds(1)))

        mockMvc.get("/Exp1234").andExpect {
            status { isGone() }
            jsonPath("$.error") { isString() }
        }
        assertEquals(0, clickRepository.count())
    }
}
