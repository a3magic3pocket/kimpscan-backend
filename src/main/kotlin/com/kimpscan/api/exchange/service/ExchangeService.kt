package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import com.kimpscan.api.exchange.client.UpbitClient
import com.kimpscan.api.exchange.dto.Binance24hTickerDto
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.dto.client.UpbitSymbolInfoApiResDto
import com.kimpscan.api.exchange.kafka.ExchangeTickerProducer
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
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
    private val exchangeTickerProducer: ExchangeTickerProducer,
    private val serviceLeaderLockService: ServiceLeaderLockService,
    private val keyValueStoreService: KeyValueStoreService,
) {
    private val exRateGetter = createExRateGetter()
    private val binance24hTickerDtoMapGetter = createBinance24hTickerDtoMapGetter()
    private val upbitSymbolInfoMap = mutableMapOf<String, UpbitSymbolInfoApiResDto>()

    // CoroutineScope 를 애플리케이션 라이프사이클에 맞게 관리
    private val job = SupervisorJob() // 코루틴 작업을 관리할 Job
    private val scope = CoroutineScope(Dispatchers.Default + job) // 스코프 설정

    @PostConstruct
    fun init() {
        scope.launch {
            updateUpbitSymbolInfo()
        }
    }

    @Scheduled(fixedRate = 1000)
    fun publishKimp() {
        val isAcquired = serviceLeaderLockService.tryToAcquireLock()
        if (isAcquired) {
            scope.launch {
                exchangeTickerProducer.sendTicker(getExchangeTicker())
            }
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

    fun getBeforeKimpMovingAvg(symbol: String): MutableList<List<Double>> {
        val beforeKimpMovingAvgMap = keyValueStoreService.retrieveBeforeKimpMovingAvgMap()

        return beforeKimpMovingAvgMap[symbol] ?: mutableListOf()
    }

    fun getBeforeKimpTickerMap(): MutableMap<String, KimpTickerDto> {
        val exchangeTickerDto = keyValueStoreService.retrieveBeforeExchangeTickerDto()

        return exchangeTickerDto.kimpTickerMap
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
