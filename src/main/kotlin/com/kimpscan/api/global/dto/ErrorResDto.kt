package com.kimpscan.api.global.dto

import java.util.Date

data class ErrorResDto(
    var code: String,
    var timestamp: Date,
    var path: String,
    var message: String,
    var fieldErrorDtos: List<FieldErrorDto> = listOf()
)