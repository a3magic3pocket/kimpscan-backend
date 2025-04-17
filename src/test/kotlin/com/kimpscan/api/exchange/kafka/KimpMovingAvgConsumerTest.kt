package com.kimpscan.api.exchange.kafka

import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.entity.KeyValueStore
import com.kimpscan.api.exchange.handler.WebSocketKimpMovingAvgHandler
import com.kimpscan.api.exchange.service.KeyValueStoreService
import com.kimpscan.api.global.config.AppConfig
import com.kimpscan.api.global.config.KafkaMessageListenerConfig
import io.mockk.*
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KimpMovingAvgConsumerTest {

    private lateinit var webSocketKimpMovingAvgHandler: WebSocketKimpMovingAvgHandler
    private lateinit var keyValueStoreService: KeyValueStoreService
    private lateinit var kafkaMessageListenerConfig: KafkaMessageListenerConfig
    private lateinit var appConfig: AppConfig
    private lateinit var consumer: KimpMovingAvgConsumer

    @BeforeEach
    fun setUp() {
        webSocketKimpMovingAvgHandler = mockk(relaxed = true)
        keyValueStoreService = mockk(relaxed = true)
        kafkaMessageListenerConfig = mockk()
        appConfig = mockk()

        every { appConfig.containerId } returns "test"

        consumer = KimpMovingAvgConsumer(
            webSocketKimpMovingAvgHandler,
            keyValueStoreService,
            kafkaMessageListenerConfig,
            appConfig
        )
    }

    @Test
    fun `메시지 수신 시 broadcast 로 전달되는 result 값 검증`() {
        // given
        val inputSymbol = "BTCUSDT"
        val currentKimp = 2.5
        val exchangeTickerDto = ExchangeTickerDto(
            usdWonExRage = 1350.0,
            kimpTickerMap = mutableMapOf(
                inputSymbol to KimpTickerDto(
                    rootSymbol = "BTC",
                    korName = "비트코인",
                    wonPrice = "123456789.12345",
                    wonOldPrice = "122000000.00000",
                    won24hVolume = "1400.12345",
                    usdtPrice = "85000.00000",
                    usdtOldPrice = "84000.00000",
                    usdt24hVolume = "21000.54321",
                    kimp = currentKimp.toString()
                )
            )
        )

        val beforeExchangeTickerDto = ExchangeTickerDto(
            usdWonExRage = 1350.0,
            kimpTickerMap = mutableMapOf(
                inputSymbol to KimpTickerDto(
                    rootSymbol = "BTC",
                    korName = "비트코인",
                    wonPrice = "123456789.12345",
                    wonOldPrice = "122000000.00000",
                    won24hVolume = "1400.12345",
                    usdtPrice = "85000.00000",
                    usdtOldPrice = "84000.00000",
                    usdt24hVolume = "21000.54321",
                    kimp = "2.0"
                )
            )
        )

        val kimpMovingAvgCache = mutableMapOf(
            inputSymbol to mutableListOf(
                1.80000,
                1.66723,
                1.91117,
                1.90594,
                2.34909,
                1.72352,
                2.3144,
                1.71714,
                1.62966,
                1.5741,
                1.64297,
                1.50608,
                2.09057,
                1.94201,
                1.91064,
                1.71433,
                1.56699,
                2.37406,
                2.06449,
                1.63872
            )
        )
        val beforeKimpMovingAvgMap: MutableMap<String, MutableList<List<Double>>> = mutableMapOf()
        println("kimpMovingAvgCache"+kimpMovingAvgCache)

        every { keyValueStoreService.retrieveBeforeExchangeTickerDto() } returns beforeExchangeTickerDto
        every { keyValueStoreService.retrieveKimpMovingAvgCache() } returns kimpMovingAvgCache
        every { keyValueStoreService.retrieveBeforeKimpMovingAvgMap() } returns beforeKimpMovingAvgMap
        coEvery { keyValueStoreService.upsertKimpMovingAvgCache(any()) } returns KeyValueStore(
            id = -1,
            key = "unknwon",
            value = "unknwon",
            updatedAt = ZonedDateTime.now(),
        )
        coEvery { keyValueStoreService.upsertBeforeKimpMovingAvgMap(any()) } returns KeyValueStore(
            id = -1,
            key = "unknwon",
            value = "unknwon",
            updatedAt = ZonedDateTime.now(),
        )

        val capturedResult = slot<MutableMap<String, List<Double>>>()
        every { webSocketKimpMovingAvgHandler.broadcast(capture(capturedResult)) } just Runs

        val consumer = KimpMovingAvgConsumer(
            webSocketKimpMovingAvgHandler,
            keyValueStoreService,
            kafkaMessageListenerConfig,
            appConfig
        )

        val record = mockk<ConsumerRecord<String, String>>()
        val jsonString = Json.encodeToString(ExchangeTickerDto.serializer(), exchangeTickerDto)
        every { record.value() } returns jsonString

        // when
        val listener = consumer.messageListener()
        listener.onMessage(record)

        // then
        verify(exactly = 1) { webSocketKimpMovingAvgHandler.broadcast(any()) }

        // 캡쳐된 result 확인
        val result = capturedResult.captured
        println("Captured result: $result")

        val kimpList = result[inputSymbol]
        requireNotNull(kimpList)
        assertEquals(currentKimp, kimpList[0])

        val targetKimpMovingAvgCache = kimpMovingAvgCache[inputSymbol]
        assertNotNull(targetKimpMovingAvgCache)

        // 현재 김프가 kimpMovingAvgCache 에 추가된 상태
        assertEquals(currentKimp, targetKimpMovingAvgCache.last())

        val expectedMovingAvg5 = targetKimpMovingAvgCache.take(5)?.average()
        assertEquals(expectedMovingAvg5, kimpList[1])

        val expectedMovingAvg20 = targetKimpMovingAvgCache.take(20)?.average()
        assertEquals(expectedMovingAvg20, kimpList[2])
    }
}