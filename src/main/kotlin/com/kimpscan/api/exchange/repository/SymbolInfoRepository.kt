package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.SymbolInfo
import org.springframework.data.jpa.repository.JpaRepository

interface SymbolInfoRepository : JpaRepository<SymbolInfo, Long> {
}