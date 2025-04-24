package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class BinanceExchangeInfoResDto(

    @SerialName("timezone") var timezone: String? = null,
    @SerialName("serverTime") var serverTime: Long? = null,
    @SerialName("symbols") var symbols: ArrayList<BinanceSymbolsResDto> = arrayListOf(),

)