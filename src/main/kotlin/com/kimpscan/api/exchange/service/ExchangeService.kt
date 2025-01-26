package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import com.kimpscan.api.exchange.client.UpbitClient
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExchangeService(
    private val upbitClient: UpbitClient,
    private val binanceClient: BinanceClient,
    private val exRateClient: ExRateClient,
) {
    val exRateChecker = createExRateChecker()

    @PostConstruct
    fun init() {

    }

    private fun convertSymbolUpbit(market: String): String {
        return market.replace("KRW-", "") + "USDT"
    }

    private fun createExRateChecker(): () -> Double {
        var baseDate: LocalDateTime? = null
        var exRate = 0.0

        return {
            runBlocking {
                val now = LocalDateTime.now().toLocalDate().atStartOfDay()
                if (baseDate == null || now.isAfter(baseDate)) {
                    baseDate = now

                    val exRateDeferred = async { exRateClient.getCurrentExRate() }
                    exRate = exRateDeferred.await()
                }
                exRate
            }
        }
    }

    fun getKimp(): String {
        runBlocking {
            val upbitDeferred = async { upbitClient.getTickers() }
            val binanceDeferred = async { binanceClient.getTickers() }

            val upbitTickers = upbitDeferred.await()
            val binanceTickers = binanceDeferred.await()
            val usdWonExRate = exRateChecker()

            val binanceTickerMap = binanceTickers.associate { it.symbol to it.price }

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
