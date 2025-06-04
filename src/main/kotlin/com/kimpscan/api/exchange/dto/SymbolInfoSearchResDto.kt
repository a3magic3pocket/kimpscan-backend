package com.kimpscan.api.exchange.dto

import kotlinx.serialization.Serializable

@Serializable
data class SymbolInfoSearchResDto(
    val symbol: String,
    val rootSymbol: String,
    val korName: String,
)
