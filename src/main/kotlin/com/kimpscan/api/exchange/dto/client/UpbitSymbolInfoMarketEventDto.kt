package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class UpbitSymbolInfoMarketEventDto(

    @SerialName("warning") var warning: Boolean,
    @SerialName("caution") var upbitSymbolInfoCautionDto: UpbitSymbolInfoCautionDto? = null

)