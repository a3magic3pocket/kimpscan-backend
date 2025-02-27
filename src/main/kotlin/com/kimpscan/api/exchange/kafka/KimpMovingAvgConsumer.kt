package com.kimpscan.api.exchange.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaConsumerGroupId
import com.kimpscan.api.constant.KafkaTopic
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.handler.WebSocketKimpMovingAvgHandler
import com.kimpscan.api.exchange.service.KeyValueStoreService
import com.kimpscan.api.global.config.AppConfig
import com.kimpscan.api.global.config.KafkaMessageListenerConfig
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Service

@Service
class KimpMovingAvgConsumer(
    private val webSocketKimpMovingAvgHandler: WebSocketKimpMovingAvgHandler,
    private val keyValueStoreService: KeyValueStoreService,
    private val kafkaMessageListenerConfig: KafkaMessageListenerConfig,
    private val appConfig: AppConfig,
) {

    @Bean
    fun kimpMovingAvgConsume(): KafkaMessageListenerContainer<String, String> {
        val container = kafkaMessageListenerConfig.createKafkaMessageListenerContainer(
            topic = KafkaTopic.EXCHANGE_TICKER,
            groupId = "${KafkaConsumerGroupId.KIMP_TICKER_MOVING_AVG}-${appConfig.containerId}",
            messageListener = messageListener()
        )
        container.start()

        return container
    }

    private fun messageListener(): MessageListener<String, String> {
        return MessageListener { record ->
            val result: MutableMap<String, List<Double>> = mutableMapOf()

            val json = Json { ignoreUnknownKeys = true } // 설정 옵션 (알 수 없는 키를 무시)

            val input = record.value()
                .trim('"')
                .replace("\\\"", "\"")

            val exchangeTickerDto = json.decodeFromString<ExchangeTickerDto>(input)

            val beforeExchangeTickerDto = keyValueStoreService.retrieveBeforeExchangeTickerDto()

            // 김프를 저장하는 슬라이딩 윈도우 (종목별로 관리)
            val kimpMovingAvgCache = keyValueStoreService.retrieveKimpMovingAvgCache()

            // 이전 김프 이동평균 맵
            val beforeKimpMovingAvgMap = keyValueStoreService.retrieveBeforeKimpMovingAvgMap()

            for ((symbol, beforeKimpTicker) in beforeExchangeTickerDto.kimpTickerMap) {
                // kimpMovingAvgCache 에 해당 symbol 이 없으면 빈 리스트로 초기화
                val kimpList = kimpMovingAvgCache.computeIfAbsent(symbol) { mutableListOf() }

                // kimpList 크기가 20이면 첫 번째 요소를 제거
                if (kimpList.size == 20) {
                    kimpList.removeFirst()
                }

                // 현재 kimp 값을 가져오고, 없으면 beforeKimpTicker.kimp 사용
                val kimp = exchangeTickerDto.kimpTickerMap[symbol]?.kimp ?: beforeKimpTicker.kimp
                kimpList.add(kimp.toDouble())

                val movingAvg5 = if (kimpList.size >= 5) {
                    kimpList.take(5).average()
                } else {
                    0.0
                }

                val movingAvg20 = if (kimpList.size == 20) {
                    kimpList.average()
                } else {
                    0.0
                }

                val movingAvgList = listOf(kimp.toDouble(), movingAvg5, movingAvg20)
                result[symbol] = movingAvgList

                // beforeKimpMovingAvgMap 갱신
                updateBeforeKimpMovingAvgMap(
                    symbol = symbol,
                    movingAvgList = movingAvgList,
                    beforeKimpMovingAvgMap = beforeKimpMovingAvgMap,
                )
            }

            // kimpMovingAvgCache 갱신
            keyValueStoreService.upsertKimpMovingAvgCache(kimpMovingAvgCache)

            // 브로드 캐스트
            webSocketKimpMovingAvgHandler.broadcast(result)
        }
    }

    private fun updateBeforeKimpMovingAvgMap(
        symbol: String,
        movingAvgList: List<Double>,
        beforeKimpMovingAvgMap: MutableMap<String, MutableList<List<Double>>>
    ) {
        val beforeMovingAvgList = beforeKimpMovingAvgMap.getOrPut(symbol) { mutableListOf() }

        // 리스트 크기가 7 이상이면 가장 오래된 값 제거
        if (beforeMovingAvgList.size >= 7) {
            beforeMovingAvgList.removeAt(0)
        }

        // 새 값 추가
        beforeMovingAvgList.add(movingAvgList)
    }
}