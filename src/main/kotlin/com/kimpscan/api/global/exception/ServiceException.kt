package com.kimpscan.api.global.exception

class ServiceException(
    message: String = "internal server error occurred",
    val code: String = "500",
) : RuntimeException(message)
