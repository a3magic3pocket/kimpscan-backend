package com.kimpscan.api.exchange.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import com.kimpscan.api.exchange.client.UpbitClient
import com.kimpscan.api.exchange.dto.Binance24hTickerDto
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.dto.client.UpbitSymbolInfoApiResDto
import com.kimpscan.api.exchange.handler.WebSocketKimpTickerHandler
import com.kimpscan.api.exchange.kafka.KimpProducer
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Service
class ExchangeService(
    private val upbitClient: UpbitClient,
    private val binanceClient: BinanceClient,
    private val exRateClient: ExRateClient,
    private val webSocketKimpTickerHandler: WebSocketKimpTickerHandler,
    private val objectMapper: ObjectMapper,
    private val kimpTickerProducer: KimpProducer,
    private val serviceLeaderLockService: ServiceLeaderLockService,
) {
    private val exRateGetter = createExRateGetter()
    private val binance24hTickerDtoMapGetter = createBinance24hTickerDtoMapGetter()
    private val upbitSymbolInfoMap = mutableMapOf<String, UpbitSymbolInfoApiResDto>()

    // CoroutineScope 를 애플리케이션 라이프사이클에 맞게 관리
    private val job = SupervisorJob() // 코루틴 작업을 관리할 Job
    private val scope = CoroutineScope(Dispatchers.Default + job) // 스코프 설정

    // SharedFlow 생성 (replay = 0은 과거 데이터를 저장하지 않음)
    private val exchangeTickerSharedFlow = MutableSharedFlow<ExchangeTickerDto>(replay = 0)

    // 직전 kimpTickerMap
    private val kimpTickerMapLock = ReentrantReadWriteLock()
    private var beforeKimpTickerMap: MutableMap<String, KimpTickerDto> = mutableMapOf()

    @PostConstruct
    fun init() {
        startBroadcastKimpTickerMapLoop()
        startProduceKimpMapLoop()
        scope.launch {
            updateUpbitSymbolInfo()
        }
    }

    fun startBroadcastKimpTickerMapLoop() {
        scope.launch {
            exchangeTickerSharedFlow.collect { exchangeTicker ->
                val diffTickerMap = getDiffKimpTickerMap(exchangeTicker.kimpTickerMap)

                // diffTickerMap 을 JSON 문자열로 변환
                val diffExchangeTickerDto = mapOf(
                    ExchangeTickerDto::usdWonExRage.name to exchangeTicker.usdWonExRage,
                    ExchangeTickerDto::kimpTickerMap.name to diffTickerMap,
                )
                val diffTickerJson = objectMapper.writeValueAsString(diffExchangeTickerDto)

                // diffDto 를 모든 세션에 브로드캐스트
                for (session in webSocketKimpTickerHandler.sessions) {
                    webSocketKimpTickerHandler.broadcast(diffTickerJson)
                }

                // 직전 TickerMap 갱신
                kimpTickerMapLock.write {
                    beforeKimpTickerMap = exchangeTicker.kimpTickerMap
                }
            }
        }
    }

    fun startProduceKimpMapLoop() {
        scope.launch {
            exchangeTickerSharedFlow.collect { exchangeTicker ->
                val kimpTickerMap = exchangeTicker.kimpTickerMap.mapValues { it.value.kimp }
                kimpTickerProducer.sendTicker(kimpTickerMap)
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    fun publishKimp() {
        val isAcquired = serviceLeaderLockService.tryToAcquireLock()
        if (isAcquired) {
            scope.launch {
                exchangeTickerSharedFlow.emit(getExchangeTicker())
            }
        }
    }

    fun getBeforeKimpTickerMap(): MutableMap<String, KimpTickerDto> {
        val loadedBeforeTickerMap = kimpTickerMapLock.read {
            beforeKimpTickerMap
        }

        return loadedBeforeTickerMap
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

    suspend fun getExchangeTicker(): ExchangeTickerDto {
        return coroutineScope {
            val upbitDeferred = async { upbitClient.getTickers() }
            val binanceDeferred = async { binanceClient.getTickers() }

            val upbitTickers = upbitDeferred.await()
            val binanceTickers = binanceDeferred.await()
            val usdWonExRate = exRateGetter()
            val binance24hTickerDtoMap = binance24hTickerDtoMapGetter()

            val binanceTickerMap = binanceTickers.associate { it.symbol to it.price }
            val tickerMap = mutableMapOf<String, KimpTickerDto>()

            for (upbitTicker in upbitTickers) {
                val upbitSymbol = convertSymbolUpbit(krwMarket = upbitTicker.market)
                val binancePrice = binanceTickerMap[upbitSymbol] ?: continue

                val wonBinancePrice = binancePrice.toDouble() * usdWonExRate

                val kimp = (upbitTicker.tradePrice - wonBinancePrice) / wonBinancePrice * 100
                if (kimp.isNaN() || kimp.isInfinite()) {
                    continue
                }

                val ticker = KimpTickerDto(
                    rootSymbol = convertRootSymbol(usdtSymbol = upbitSymbol),
                    korName = upbitSymbolInfoMap[upbitSymbol]?.koreanName ?: "",
                    wonPrice = roundDecimalPlaces(upbitTicker.tradePrice).toString(),
                    wonOldPrice = roundDecimalPlaces(
                        upbitTicker.prevClosingPrice
                    ).toString(),
                    won24hVolume = roundDecimalPlaces(upbitTicker.accTradeVolume24h).toString(),
                    usdtPrice = roundDecimalPlaces(binancePrice.toDouble()).toString(),
                    usdtOldPrice = binance24hTickerDtoMap[upbitSymbol]?.let {
                        roundDecimalPlaces(
                            it.openPrice.toDouble()
                        ).toString()
                    } ?: "0.0",
                    usdt24hVolume = binance24hTickerDtoMap[upbitSymbol]?.let {
                        roundDecimalPlaces(
                            it.volume.toDouble()
                        ).toString()
                    } ?: "0.0",
                    kimp = roundDecimalPlaces(kimp).toString(),
                )
                tickerMap[upbitSymbol] = ticker
            }

            return@coroutineScope ExchangeTickerDto(
                usdWonExRage = usdWonExRate,
                kimpTickerMap = tickerMap
            )
        }
    }

    private fun convertSymbolUpbit(krwMarket: String): String {
        return krwMarket.replace("KRW-", "") + "USDT"
    }

    private fun convertRootSymbol(usdtSymbol: String): String {
        return usdtSymbol.replace("USDT", "")
    }

    private fun roundDecimalPlaces(realNumber: Double, scale: Int = 5): BigDecimal {
        return BigDecimal(realNumber).setScale(scale, RoundingMode.HALF_UP)
    }

    private fun getDiffKimpTickerMap(currentTickerMap: MutableMap<String, KimpTickerDto>): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()

        val loadedBeforeTickerMap = kimpTickerMapLock.read {
            beforeKimpTickerMap
        }

        for ((symbol, currentTicker) in currentTickerMap) {
            val beforeTicker = loadedBeforeTickerMap[symbol]
            if (beforeTicker == null) {
                result[symbol] = currentTicker
                continue
            }

            if (currentTicker.toString() == beforeTicker.toString()) {
                continue
            }

            val row = mutableMapOf<String, Any>()

            if (beforeTicker.rootSymbol != currentTicker.rootSymbol) {
                row[KimpTickerDto::rootSymbol.name] = currentTicker.rootSymbol
            }
            if (beforeTicker.korName != currentTicker.korName) {
                row[KimpTickerDto::korName.name] = currentTicker.korName
            }
            if (beforeTicker.wonPrice != currentTicker.wonPrice) {
                row[KimpTickerDto::wonPrice.name] = currentTicker.wonPrice
            }
            if (beforeTicker.wonOldPrice != currentTicker.wonOldPrice) {
                row[KimpTickerDto::wonOldPrice.name] = currentTicker.wonOldPrice
            }
            if (beforeTicker.won24hVolume != currentTicker.won24hVolume) {
                row[KimpTickerDto::won24hVolume.name] = currentTicker.won24hVolume
            }
            if (beforeTicker.usdtPrice != currentTicker.usdtPrice) {
                row[KimpTickerDto::usdtPrice.name] = currentTicker.usdtPrice
            }
            if (beforeTicker.usdtOldPrice != currentTicker.usdtOldPrice) {
                row[KimpTickerDto::usdtOldPrice.name] = currentTicker.usdtOldPrice
            }
            if (beforeTicker.usdt24hVolume != currentTicker.usdt24hVolume) {
                row[KimpTickerDto::usdt24hVolume.name] = currentTicker.usdt24hVolume
            }
            if (beforeTicker.kimp != currentTicker.kimp) {
                row[KimpTickerDto::kimp.name] = currentTicker.kimp
            }

            if (row.isNotEmpty()) {
                result[symbol] = row
            }
        }

        return result
    }

    private fun createExRateGetter(): suspend () -> Double {
        var baseDate: LocalDateTime? = null
        var exRate = 0.0

        return suspend {
            coroutineScope {
                // 하루에 한 번 갱신
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

    private fun createBinance24hTickerDtoMapGetter(): suspend () -> MutableMap<String, Binance24hTickerDto> {
        var baseDateTime: LocalDateTime? = null
        val binance24hTickerDtoMap = mutableMapOf<String, Binance24hTickerDto>()

        return suspend {
            coroutineScope {
                // 5초에 한 번 갱신
                val now = LocalDateTime.now()
                if (baseDateTime == null || now.isAfter(baseDateTime!!.plusSeconds(5))) {
                    baseDateTime = now
                    val binance24hDeferred = async { binanceClient.get24hTickers() }
                    val binance24hTickerApiResDto = binance24hDeferred.await()
                    for (row in binance24hTickerApiResDto) {
                        binance24hTickerDtoMap[row.symbol] = Binance24hTickerDto(
                            openPrice = roundDecimalPlaces(row.openPrice.toDouble()),
                            volume = roundDecimalPlaces(row.volume.toDouble())
                        )
                    }
                }

                binance24hTickerDtoMap
            }
        }
    }

}
