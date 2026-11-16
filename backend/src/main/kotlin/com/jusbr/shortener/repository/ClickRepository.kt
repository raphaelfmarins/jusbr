package com.jusbr.shortener.repository

import com.jusbr.shortener.domain.Click
import org.springframework.data.jpa.repository.JpaRepository

interface ClickRepository : JpaRepository<Click, Long>
