package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import com.kimpscan.api.exchange.client.UpbitClient
import com.kimpscan.api.exchange.handler.WebSocketMessageHandler
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExchangeService(
    private val upbitClient: UpbitClient,
    private val binanceClient: BinanceClient,
    private val exRateClient: ExRateClient,
    private val webSocketMessageHandler: WebSocketMessageHandler,
) {
    val exRateChecker = createExRateChecker()
    // CoroutineScope를 애플리케이션 라이프사이클에 맞게 관리
    private val job = SupervisorJob() // 코루틴 작업을 관리할 Job
    private val scope = CoroutineScope(Dispatchers.Default + job) // 스코프 설정

    @PostConstruct
    fun init() {
        startBroadcastKimpLoop()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startBroadcastKimpLoop() {
        GlobalScope.launch {
            while (true) {
                delay(1000)
                webSocketMessageHandler.printSessions()
            }
        }
//        scope.launch {
//            while (true) {
//                webSocketMessageHandler.printSessions()
//                delay(1000)
//                val kimp = getKimp()
//                println("kimp"+kimp)
//                webSocketMessageHandler.broadcast(kimp)
//            }
//        }
    }

    suspend fun getKimp(): String {
        coroutineScope {
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

//                println("upbitSymbol" + upbitSymbol)
//                println("kimp" + kimp)
//                println()
            }
        }

        return "getKimp"
    }

    private fun convertSymbolUpbit(market: String): String {
        return market.replace("KRW-", "") + "USDT"
    }

    private fun createExRateChecker(): suspend () -> Double {
        var baseDate: LocalDateTime? = null
        var exRate = 0.0

        return suspend {
            coroutineScope {
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

}
