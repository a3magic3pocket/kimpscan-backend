package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.dto.UpbitExchangeInfoDto
import com.kimpscan.api.exchange.entity.BinanceSymbolStatus
import com.kimpscan.api.exchange.entity.SymbolInfo
import com.kimpscan.api.exchange.repository.SymbolInfoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SymbolInfoService(
    private val symbolInfoRepository: SymbolInfoRepository,
) {

    @Transactional(rollbackFor = [Exception::class])
    fun upsertSymbolInfo(
        upbitExchangeInfoMap: MutableMap<String, UpbitExchangeInfoDto>,
        binanceExchangeInfoMap: MutableMap<String, BinanceSymbolStatus>
    ) {
        val existSymbols = mutableSetOf<String>()
        val symbolInfoList = symbolInfoRepository.findAll()
        symbolInfoList.forEach { symbolInfo ->
            val upbitExchangeInfo = upbitExchangeInfoMap[symbolInfo.symbol]
            val binanceSymbolStatus = binanceExchangeInfoMap[symbolInfo.symbol]

            upbitExchangeInfo?.let {
                symbolInfo.korName = it.korName
                symbolInfo.upbitWarning = it.warning
            }

            binanceSymbolStatus?.let {
                symbolInfo.binanceSymbolStatus = it.toString()
            }

            existSymbols.add(symbolInfo.symbol)
        }

        for ((symbol, upbitExchangeInfo) in upbitExchangeInfoMap.entries) {
            if (existSymbols.contains(symbol)) {
                continue
            }

            val binanceSymbolStatus = binanceExchangeInfoMap[symbol]

            val symbolInfo = SymbolInfo(
                symbol = symbol,
                korName = upbitExchangeInfo.korName,
                upbitWarning = upbitExchangeInfo.warning,
                binanceSymbolStatus = binanceSymbolStatus?.toString() ?: BinanceSymbolStatus.TRADING.toString(),
            )
            symbolInfoList.add(symbolInfo)
        }

        symbolInfoRepository.saveAll(symbolInfoList)
    }
}