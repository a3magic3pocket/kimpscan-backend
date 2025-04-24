package com.kimpscan.api.exchange.dto

import com.kimpscan.api.exchange.entity.BinanceSymbolStatus
import kotlinx.serialization.Serializable

@Serializable
data class KimpTickerDto (
    val rootSymbol: String,
    val korName: String,
    val wonPrice: String,
    val wonOldPrice: String,
    val won24hVolume: String,
    val usdtPrice: String,
    val usdtOldPrice: String,
    val usdt24hVolume: String,
    val upbitWarning: Boolean,
    val binanceStatus: BinanceSymbolStatus,
    val kimp: String,
)