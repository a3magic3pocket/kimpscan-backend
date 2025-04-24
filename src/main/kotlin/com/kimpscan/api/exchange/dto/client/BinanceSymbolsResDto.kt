package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class BinanceSymbolsResDto(

    @SerialName("symbol") var symbol: String? = null,
    @SerialName("status") var status: String? = null,

)