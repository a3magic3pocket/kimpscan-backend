package com.kimpscan.api.exchange.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaTopic
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.handler.WebSocketKimpMovingAvgHandler
import com.kimpscan.api.exchange.service.ExchangeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class KimpMovingAvgConsumer(
    private val objectMapper: ObjectMapper,
    private val exchangeService: ExchangeService,
    private val webSocketKimpMovingAvgHandler: WebSocketKimpMovingAvgHandler
) {

    // 김프를 저장하는 슬라이딩 윈도우 (종목별로 관리)
    private val kimpData: MutableMap<String, MutableList<Double>> = ConcurrentHashMap()
    private var beforeKimpTickerMap = mutableMapOf<String, ExchangeTickerDto>()
    private var isInit = false

    @KafkaListener(topics = [KafkaTopic.TICKER], groupId = "kimp-ticker-moving-avg", concurrency = "1")
    fun consume(record: ConsumerRecord<String, String>) {
        val result: MutableMap<String, List<Double>> = mutableMapOf()

        if (!isInit) {
            beforeKimpTickerMap = exchangeService.getBeforeKimpTickerMap()
        }

        val typeRef = object : TypeReference<Map<String, String>>() {}
        val kimpMap: Map<String, String> = objectMapper.readValue(record.value(), typeRef)

        for ((symbol, beforeKimpTicker) in beforeKimpTickerMap) {
            // kimpData에 해당 symbol이 없으면 빈 리스트로 초기화
            val kimpList = kimpData.computeIfAbsent(symbol) { mutableListOf() }

            // kimpList 크기가 20이면 첫 번째 요소를 제거
            if (kimpList.size == 20) {
                kimpList.removeFirst()
            }

            // kimp 값을 가져오고, 없으면 beforeKimpTicker.kimp 사용
            val kimp = kimpMap[symbol] ?: beforeKimpTicker.kimp
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

            result[symbol] = listOf(movingAvg5, movingAvg20)

            // kimpData[symbol] 출력
            println("kimpData[$symbol]: $kimpList")
        }

        isInit = true

        // 브로드 캐스트
        webSocketKimpMovingAvgHandler.broadcast(result)

        println("record" + record.value())
        println("result" + result)

    }
}