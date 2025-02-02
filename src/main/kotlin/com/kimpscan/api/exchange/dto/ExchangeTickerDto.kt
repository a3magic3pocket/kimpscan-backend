package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeTickerDto(
    val rootSymbol: String,
    val korName: String,
    val kimp: Double,
)
