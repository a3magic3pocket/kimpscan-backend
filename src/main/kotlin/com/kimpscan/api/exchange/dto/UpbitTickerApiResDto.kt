package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class UpbitTickerApiResDto(

    @SerialName("market") val market: String,
    @SerialName("trade_date") val tradeDate: String? = null,
    @SerialName("trade_time") val tradeTime: String? = null,
    @SerialName("trade_date_kst") val tradeDateKst: String? = null,
    @SerialName("trade_time_kst") val tradeTimeKst: String? = null,
    @SerialName("trade_timestamp") val tradeTimestamp: Double? = null,
    @SerialName("opening_price") val openingPrice: Double? = null,
    @SerialName("high_price") val highPrice: Double? = null,
    @SerialName("low_price") val lowPrice: Double? = null,
    @SerialName("trade_price") val tradePrice: Double,
    @SerialName("prev_closing_price") val prevClosingPrice: Double? = null,
    @SerialName("change") val change: String? = null,
    @SerialName("change_price") val changePrice: Double? = null,
    @SerialName("change_rate") val changeRate: Double? = null,
    @SerialName("signed_change_price") val signedChangePrice: Double? = null,
    @SerialName("signed_change_rate") val signedChangeRate: Double? = null,
    @SerialName("trade_volume") val tradeVolume: Double? = null,
    @SerialName("acc_trade_price") val accTradePrice: Double? = null,
    @SerialName("acc_trade_price_24h") val accTradePrice24h: Double? = null,
    @SerialName("acc_trade_volume") val accTradeVolume: Double? = null,
    @SerialName("acc_trade_volume_24h") val accTradeVolume24h: Double? = null,
    @SerialName("highest_52_week_price") val highest52WeekPrice: Double? = null,
    @SerialName("highest_52_week_date") val highest52WeekDate: String? = null,
    @SerialName("lowest_52_week_price") val lowest52WeekPrice: Double? = null,
    @SerialName("lowest_52_week_date") val lowest52WeekDate: String? = null,
    @SerialName("timestamp") val timestamp: Double? = null

)