package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeTickerDto(
    val usdWonExRage: Double,
    val kimpTickerMap: MutableMap<String, KimpTickerDto>
)
