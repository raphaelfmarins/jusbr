package com.jusbr.shortener.domain

class InvalidRequestException(message: String) : RuntimeException(message)

class ShortUrlNotFoundException(code: String) : RuntimeException("Short URL '$code' not found")

class ShortUrlExpiredException(code: String) : RuntimeException("Short URL '$code' has expired")
