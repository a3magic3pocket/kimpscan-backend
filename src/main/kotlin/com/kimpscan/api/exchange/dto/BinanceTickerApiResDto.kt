package com.kimpscan.api.exchange.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceTickerApiResDto(
    @SerialName("symbol") val symbol: String,
    @SerialName("price") val price: String
)