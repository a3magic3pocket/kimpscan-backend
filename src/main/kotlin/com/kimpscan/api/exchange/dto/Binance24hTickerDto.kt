package com.kimpscan.api.exchange.dto

import java.math.BigDecimal

data class Binance24hTickerDto(
    val openPrice: BigDecimal,
    val volume: BigDecimal,
)
