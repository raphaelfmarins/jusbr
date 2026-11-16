package com.jusbr.shortener.config

import com.jusbr.shortener.domain.ShortCodeGenerator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class AppConfig(private val appProperties: AppProperties) : WebMvcConfigurer {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun shortCodeGenerator() = ShortCodeGenerator()

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(appProperties.frontendUrl)
            .allowedMethods("GET")
    }
}
