package com.jusbr.shortener.controller

import com.jusbr.shortener.domain.InvalidRequestException
import com.jusbr.shortener.domain.ShortUrlExpiredException
import com.jusbr.shortener.domain.ShortUrlNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalid(e: InvalidRequestException) = ErrorResponse(e.message!!)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnreadable(e: HttpMessageNotReadableException) = ErrorResponse("Malformed request body")

    @ExceptionHandler(ShortUrlNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(e: ShortUrlNotFoundException) = ErrorResponse(e.message!!)

    @ExceptionHandler(ShortUrlExpiredException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun handleExpired(e: ShortUrlExpiredException) = ErrorResponse(e.message!!)
}
