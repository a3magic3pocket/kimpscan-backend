package com.kimpscan.api.global.exception

class NotFoundException(
    val field: String,
    val value: Any,
    message: String = "$value is not found",
    val code: String = "404",
) : RuntimeException(message)
