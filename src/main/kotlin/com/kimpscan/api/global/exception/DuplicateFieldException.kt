package com.kimpscan.api.global.exception

class DuplicateFieldException(
    val field: String,
    val value: Any,
    message: String = "$value already exists",
    val code: String = "409",
) : RuntimeException(message)
