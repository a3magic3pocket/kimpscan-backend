package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeTickerDto(
    val rootSymbol: String,
    val korName: String,
    val korPrice: Double,
    val korOldPrice: Double,
    val korVolume: Double,
    val korOldVolume: Double,
    val kimp: Double,
)
