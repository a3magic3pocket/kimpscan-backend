package com.kimpscan.api.global.exception

class UnauthorizedException(
    message: String = "unauthorized",
    val code: String = "401",
) : RuntimeException(message)
