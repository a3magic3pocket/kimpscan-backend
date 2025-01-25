package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.UpbitClient
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class ExchangeService(
    private val upbitClient: UpbitClient,
    private val binanceClient: BinanceClient,
) {

    @PostConstruct
    fun init() {

    }

    private fun convertSymbolUpbit(market: String): String {
        return market.replace("KRW-", "") + "USDT"
    }

    fun getKimp(): String {
        runBlocking {
            val upbitDeferred = async { upbitClient.getTickers() }
            val binanceDeferred = async { binanceClient.getTickers() }

            val upbitTickers = upbitDeferred.await()
            val binanceTickers = binanceDeferred.await()

            val binanceTickerMap = binanceTickers.associate { it.symbol to it.price }
            val usdWonExRate = 1430

            for (upbitTicker in upbitTickers) {
                val upbitSymbol = convertSymbolUpbit(market = upbitTicker.market)
                val binancePrice = binanceTickerMap[upbitSymbol] ?: continue

                val wonBinancePrice = binancePrice.toDouble() * usdWonExRate

                val kimp = (upbitTicker.tradePrice - wonBinancePrice) / wonBinancePrice * 100

                println("upbitSymbol" + upbitSymbol)
                println("kimp" + kimp)
                println()
            }


        }

        return "getKimp"
    }

}
