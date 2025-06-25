package com.kimpscan.api.user.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FcmReqDto(
    @field:NotNull()
    @field:NotEmpty()
    val key: String,
)