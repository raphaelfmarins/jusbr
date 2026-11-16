package com.jusbr.shortener.repository

import com.jusbr.shortener.domain.ShortUrl
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

data class ShortUrlWithClicks(val shortUrl: ShortUrl, val clickCount: Long)

interface ShortUrlRepository : JpaRepository<ShortUrl, Long> {
    fun findByCode(code: String): ShortUrl?

    @Query(
        """
        select new com.jusbr.shortener.repository.ShortUrlWithClicks(s, count(c.id))
        from ShortUrl s left join Click c on c.shortUrlId = s.id
        group by s
        order by s.createdAt desc, s.id desc
        """,
    )
    fun findAllWithClickCount(): List<ShortUrlWithClicks>
}
