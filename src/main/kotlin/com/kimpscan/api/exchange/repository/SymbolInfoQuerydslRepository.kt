package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.SymbolInfo

interface SymbolInfoQuerydslRepository {
    fun searchSymbolInfo(keyword: String?, isStatusTrading: Boolean, limit: Long): List<SymbolInfo>
}
