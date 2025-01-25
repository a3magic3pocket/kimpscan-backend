package com.kimpscan.api.exchange.dto

data class ExchangeTickerDto(
    val symbol: String,
    val korName: String,
    val kimp: Double,
)
