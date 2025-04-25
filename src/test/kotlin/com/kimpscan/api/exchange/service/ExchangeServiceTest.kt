package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.client.BinanceClient
import com.kimpscan.api.exchange.client.ExRateClient
import io.mockk.*
import com.kimpscan.api.exchange.client.UpbitClient
import com.kimpscan.api.exchange.dto.client.Binance24hTickerApiResDto
import com.kimpscan.api.exchange.dto.client.BinanceTickerApiResDto
import com.kimpscan.api.exchange.dto.client.UpbitTickerApiResDto
import com.kimpscan.api.exchange.kafka.ExchangeTickerProducer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExchangeServiceTest {
    private val upbitClient: UpbitClient = mockk()
    private val binanceClient: BinanceClient = mockk()
    private val exRateClient: ExRateClient = mockk()
    private val exchangeTickerProducer: ExchangeTickerProducer = mockk()
    private val serviceLeaderLockService: ServiceLeaderLockService = mockk()
    private val keyValueStoreService: KeyValueStoreService = mockk()
    private val symbolInfoService: SymbolInfoService = mockk()

    private val exchangeService = ExchangeService(
        upbitClient,
        binanceClient,
        exRateClient,
        exchangeTickerProducer,
        serviceLeaderLockService,
        keyValueStoreService,
        symbolInfoService,
    )

    @Test
    fun `should return valid exchange rate and kimp tickers when getExchangeTicker is called`() = runTest {
        // Given: Mocking the API responses
        val upbitTickers = listOf(
            UpbitTickerApiResDto(
                market = "KRW-BTC",  // KRW-BTC 마켓
                tradeDate = "20250416",  // 거래일
                tradeTime = "012640",  // 거래 시간
                tradeDateKst = "20250416",  // KST 시간 기준 거래일
                tradeTimeKst = "102640",  // KST 시간 기준 거래 시간
                tradeTimestamp = 1744766800581.0,  // 타임스탬프
                openingPrice = 122154000.0,  // 시가
                highPrice = 122200000.0,  // 최고가
                lowPrice = 121290000.0,  // 최저가
                tradePrice = 121774000.0,  // 거래가
                prevClosingPrice = 122158000.0,  // 이전 종가
                change = "FALL",  // 변동 (하락)
                changePrice = -384000.0,  // 변동 가격
                changeRate = -0.0031410929,  // 변동률
                signedChangePrice = -384000.0,  // 부호가 있는 변동 가격
                signedChangeRate = -0.0031410929,  // 부호가 있는 변동률
                tradeVolume = 35.91802849,  // 거래량
                accTradePrice = 58661024754.038589048,  // 누적 거래 금액
                accTradePrice24h = 502269098146.89056485,  // 24시간 누적 거래 금액
                accTradeVolume = 90748319.14561526,  // 누적 거래량
                accTradeVolume24h = 838324113.02404402,  // 24시간 누적 거래량
                highest52WeekPrice = 122200000.0,  // 52주 최고가
                highest52WeekDate = "2025-04-16",  // 52주 최고가 날짜
                lowest52WeekPrice = 121290000.0,  // 52주 최저가
                lowest52WeekDate = "2025-04-03",  // 52주 최저가 날짜
                timestamp = 1744766800693.0,  // 타임스탬프
                extra = emptyMap()  // 추가 필드
            )
        )
        val binanceTickers = listOf(
            BinanceTickerApiResDto(
                symbol = "BTCUSDT",
                price = "83520.02000000",
                extra = emptyMap()
            )
        )
        val usdWonExRate = 1420.0
        val binance24hTickerDtos = listOf(
            Binance24hTickerApiResDto(
                symbol = "BTCUSDT",
                openPrice = "85038.48000000",
                highPrice = "86496.42000000",
                lowPrice = "83111.64000000",
                lastPrice = "83496.75000000",
                volume = "21402.27631000",
                quoteVolume = "1820773210.19171110",
                openTime = 1744680803001.0,
                closeTime = 1744767203001.0,
                firstId = 4826152990.0,
                lastId = 4829073371.0,
                count = 2920382.0
            )
        )

        // When: Mocking the API calls
        coEvery { upbitClient.getTickers() } returns upbitTickers
        coEvery { binanceClient.getTickers() } returns binanceTickers
        coEvery { exRateClient.getCurrentExRate() } returns usdWonExRate
        coEvery { binanceClient.get24hTickers() } returns binance24hTickerDtos

        // Then: Call the service method
        val result = exchangeService.getExchangeTicker()

        // Assertions to verify the output
        assertEquals(usdWonExRate, result.usdWonExRage)  // Verify exchange rate
        assertTrue(result.kimpTickerMap.isNotEmpty()) // Ensure there's at least one ticker
        val firstKey = result.kimpTickerMap.keys.firstOrNull()
        assertNotNull(firstKey)  // Ensure that there is at least one key
        val kimpTicker = result.kimpTickerMap[firstKey]

        // Verify the ticker symbol and volume
        val originalString = binance24hTickerDtos.first().volume
        val expectedBinance24hVolume = BigDecimal(originalString)
            .stripTrailingZeros()
            .toPlainString()
        assertEquals(expectedBinance24hVolume, kimpTicker?.usdt24hVolume)

        // Test convertRootSymbol()
        assertEquals("BTC", kimpTicker?.rootSymbol)

        // Test roundDecimalPlaces()
        assertEquals("838324113.02404", kimpTicker?.won24hVolume)

        // Verify kimp
        val upbitTicker = upbitTickers.first()
        val binanceTicker = binanceTickers.first()
        val wonBinancePrice = binanceTicker.price.toDouble() * usdWonExRate
        val kimp = (upbitTicker.tradePrice - wonBinancePrice) / wonBinancePrice * 100
        val expectedKimp = BigDecimal(kimp).setScale(5, RoundingMode.HALF_UP).toString()
        assertEquals(expectedKimp, kimpTicker?.kimp)
    }

}
