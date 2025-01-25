package com.kimpscan.api.exchange.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceTickerDto(
    @SerialName("symbol") var symbol: String? = null,
    @SerialName("price") var price: String? = null
)