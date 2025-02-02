package com.kimpscan.api.exchange.dto.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class UpbitSymbolInfoCautionDto(

    @SerialName("PRICE_FLUCTUATIONS") var PRICEFLUCTUATIONS: Boolean,
    @SerialName("TRADING_VOLUME_SOARING") var TRADINGVOLUMESOARING: Boolean,
    @SerialName("DEPOSIT_AMOUNT_SOARING") var DEPOSITAMOUNTSOARING: Boolean,
    @SerialName("GLOBAL_PRICE_DIFFERENCES") var GLOBALPRICEDIFFERENCES: Boolean,
    @SerialName("CONCENTRATION_OF_SMALL_ACCOUNTS") var CONCENTRATIONOFSMALLACCOUNTS: Boolean

)