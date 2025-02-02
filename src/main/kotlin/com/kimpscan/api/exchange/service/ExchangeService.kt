package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import com.kimpscan.api.exchange.client.UpbitClient
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.client.UpbitSymbolInfoApiResDto
import com.kimpscan.api.exchange.handler.WebSocketMessageHandler
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class ExchangeService(
    private val upbitClient: UpbitClient,
    private val binanceClient: BinanceClient,
    private val exRateClient: ExRateClient,
    private val webSocketMessageHandler: WebSocketMessageHandler
) {
    val exRateChecker = createExRateChecker()
    val upbitSymbolInfoMap = mutableMapOf<String, UpbitSymbolInfoApiResDto>()

    // CoroutineScope 를 애플리케이션 라이프사이클에 맞게 관리
    private val job = SupervisorJob() // 코루틴 작업을 관리할 Job
    private val scope = CoroutineScope(Dispatchers.Default + job) // 스코프 설정

    // SharedFlow 생성 (replay = 0은 과거 데이터를 저장하지 않음)
    val tickerMapSharedFlow = MutableSharedFlow<MutableMap<String, ExchangeTickerDto>>(replay = 0)

    @PostConstruct
    fun init() {
        startBroadcastKimpLoop()
        scope.launch {
            updateUpbitSymbolInfo()
        }
    }

    fun startBroadcastKimpLoop() {
        val serializer = MapSerializer(String.serializer(), ExchangeTickerDto.serializer())

        scope.launch {
            tickerMapSharedFlow.collect { tickerMap ->
                println("come in herer? startBroadcastKimpLoop")
                println("webSocketMessageHandler.sessions" + webSocketMessageHandler.sessions)
                println("tickerMap" + tickerMap)
                for (session in webSocketMessageHandler.sessions) {
                    println("session" + session)
                    println(
                        "data" + Json.encodeToString(
                            serializer = serializer,
                            value = tickerMap,
                        )
                    )
                    webSocketMessageHandler.broadcast(
                        Json.encodeToString(
                            serializer = serializer,
                            value = tickerMap,
                        )
                    )
                }
            }
        }
    }

    fun startCalculateKimpLoop() {
        scope.launch {

        }
    }

    @Scheduled(fixedRate = 1000)
    fun publishKimp() {
        scope.launch {
            println("come in here? every 1 min")
            tickerMapSharedFlow.emit(getKimp())
        }
    }

    suspend fun updateUpbitSymbolInfo() {
        coroutineScope {
            val upbitSymbolInfosDeferred = async { upbitClient.getSymbolInfo() }

            val upbitSymbolInfos = upbitSymbolInfosDeferred.await()

            for (upbitSymbolInfo in upbitSymbolInfos) {
                val upbitSymbol = convertSymbolUpbit(krwMarket = upbitSymbolInfo.market)
                upbitSymbolInfoMap[upbitSymbol] = upbitSymbolInfo
            }
        }
    }

    suspend fun getKimp(): MutableMap<String, ExchangeTickerDto> {
        return coroutineScope {
            val upbitDeferred = async { upbitClient.getTickers() }
            val binanceDeferred = async { binanceClient.getTickers() }

            val upbitTickers = upbitDeferred.await()
            val binanceTickers = binanceDeferred.await()
            val usdWonExRate = exRateChecker()

            val binanceTickerMap = binanceTickers.associate { it.symbol to it.price }
            val tickerMap = mutableMapOf<String, ExchangeTickerDto>()

            for (upbitTicker in upbitTickers) {
                val upbitSymbol = convertSymbolUpbit(krwMarket = upbitTicker.market)
                val binancePrice = binanceTickerMap[upbitSymbol] ?: continue

                val wonBinancePrice = binancePrice.toDouble() * usdWonExRate

                var kimp = (upbitTicker.tradePrice - wonBinancePrice) / wonBinancePrice * 100
                if (kimp.isNaN() || kimp.isInfinite()) {
                    continue
                }
                kimp = BigDecimal(kimp).setScale(5, RoundingMode.HALF_UP).toDouble()


                val ticker = ExchangeTickerDto(
                    rootSymbol = convertRootSymbol(usdtSymbol = upbitSymbol),
                    korName = upbitSymbolInfoMap[upbitSymbol]?.koreanName ?: "",
                    kimp = kimp
                )
                tickerMap[upbitSymbol] = ticker
            }

            return@coroutineScope tickerMap
        }
    }

    private fun convertSymbolUpbit(krwMarket: String): String {
        return krwMarket.replace("KRW-", "") + "USDT"
    }

    private fun convertRootSymbol(usdtSymbol: String): String {
        return usdtSymbol.replace("USDT", "")
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
