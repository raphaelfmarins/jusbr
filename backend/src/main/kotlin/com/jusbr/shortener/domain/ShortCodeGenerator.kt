package com.jusbr.shortener.domain

import java.security.SecureRandom

class ShortCodeGenerator(private val random: SecureRandom = SecureRandom()) {

    fun generate(): String = (1..LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")

    companion object {
        const val LENGTH = 7
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}
