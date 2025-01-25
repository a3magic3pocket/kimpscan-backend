package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class UpbitTickerDto(
    
    @SerialName("market") var market: String? = null,
    @SerialName("trade_date") var tradeDate: String? = null,
    @SerialName("trade_time") var tradeTime: String? = null,
    @SerialName("trade_date_kst") var tradeDateKst: String? = null,
    @SerialName("trade_time_kst") var tradeTimeKst: String? = null,
    @SerialName("trade_timestamp") var tradeTimestamp: Double? = null,
    @SerialName("opening_price") var openingPrice: Double? = null,
    @SerialName("high_price") var highPrice: Double? = null,
    @SerialName("low_price") var lowPrice: Double? = null,
    @SerialName("trade_price") var tradePrice: Double? = null,
    @SerialName("prev_closing_price") var prevClosingPrice: Double? = null,
    @SerialName("change") var change: String? = null,
    @SerialName("change_price") var changePrice: Double? = null,
    @SerialName("change_rate") var changeRate: Double? = null,
    @SerialName("signed_change_price") var signedChangePrice: Double? = null,
    @SerialName("signed_change_rate") var signedChangeRate: Double? = null,
    @SerialName("trade_volume") var tradeVolume: Double? = null,
    @SerialName("acc_trade_price") var accTradePrice: Double? = null,
    @SerialName("acc_trade_price_24h") var accTradePrice24h: Double? = null,
    @SerialName("acc_trade_volume") var accTradeVolume: Double? = null,
    @SerialName("acc_trade_volume_24h") var accTradeVolume24h: Double? = null,
    @SerialName("highest_52_week_price") var highest52WeekPrice: Double? = null,
    @SerialName("highest_52_week_date") var highest52WeekDate: String? = null,
    @SerialName("lowest_52_week_price") var lowest52WeekPrice: Double? = null,
    @SerialName("lowest_52_week_date") var lowest52WeekDate: String? = null,
    @SerialName("timestamp") var timestamp: Double? = null

)