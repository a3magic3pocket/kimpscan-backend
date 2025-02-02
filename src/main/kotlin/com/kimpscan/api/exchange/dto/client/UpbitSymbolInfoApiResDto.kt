package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class UpbitSymbolInfoApiResDto(

    @SerialName("market") var market: String,
    @SerialName("korean_name") var koreanName: String,
    @SerialName("english_name") var englishName: String,
    @SerialName("market_event") var upbitSymbolInfoMarketEventDto: UpbitSymbolInfoMarketEventDto? = null,
    val extra: Map<String, @Contextual Any> = emptyMap()

)
