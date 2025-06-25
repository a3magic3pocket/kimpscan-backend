package com.kimpscan.api.global.exception

class InvalidFieldException(
    val field: String,
    val value: Any,
    message: String = "$value is invalid",
    val code: String = "400",
) : RuntimeException(message)
