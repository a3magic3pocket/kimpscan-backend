package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Binance24hTickerApiResDto(

    @SerialName("symbol") var symbol: String,
    @SerialName("openPrice") var openPrice: String,
    @SerialName("highPrice") var highPrice: String,
    @SerialName("lowPrice") var lowPrice: String,
    @SerialName("lastPrice") var lastPrice: String,
    @SerialName("volume") var volume: String,
    @SerialName("quoteVolume") var quoteVolume: String,
    @SerialName("openTime") var openTime: Double,
    @SerialName("closeTime") var closeTime: Double,
    @SerialName("firstId") var firstId: Double,
    @SerialName("lastId") var lastId: Double,
    @SerialName("count") var count: Double,
    val extra: Map<String, @Contextual Any> = emptyMap()

)