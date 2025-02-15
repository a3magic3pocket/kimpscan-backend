package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Serializable

@Serializable
data class UsdWonExRateDto(
    val date: String,
    val usd: Map<String, Double>
)