package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeTickerResDto(
    val usdWonExRage: Double,
    val kimpTickerMap: MutableMap<String, KimpTickerDto>
)
