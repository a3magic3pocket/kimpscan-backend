package com.kimpscan.api.exchange.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaConsumerGroupId
import com.kimpscan.api.constant.KafkaTopic
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.handler.WebSocketKimpTickerHandler
import com.kimpscan.api.exchange.service.KeyValueStoreService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KimpTickerConsumer(
    private val objectMapper: ObjectMapper,
    private val webSocketKimpTickerHandler: WebSocketKimpTickerHandler,
    private val keyValueStoreService: KeyValueStoreService,
) {

    @KafkaListener(topics = [KafkaTopic.TICKER], groupId = KafkaConsumerGroupId.KIMP_TICKER, concurrency = "1")
    fun consume(record: ConsumerRecord<String, String>) {
        val typeRef = object : TypeReference<ExchangeTickerDto>() {}
        val exchangeTickerDto: ExchangeTickerDto = objectMapper.readValue(record.value(), typeRef)
        val beforeExchangeTickerDto = keyValueStoreService.retrieveBeforeExchangeTickerDto()

        val diffTickerMap = getDiffKimpTickerMap(
            kimpTickerMap = exchangeTickerDto.kimpTickerMap,
            beforeKimpTickerMap = beforeExchangeTickerDto.kimpTickerMap
        )

        // diffTickerMap 을 JSON 문자열로 변환
        val diffExchangeTickerDto = mapOf(
            ExchangeTickerDto::usdWonExRage.name to exchangeTickerDto.usdWonExRage,
            ExchangeTickerDto::kimpTickerMap.name to diffTickerMap,
        )
        val diffTickerJson = objectMapper.writeValueAsString(diffExchangeTickerDto)

        // diffDto 를 모든 세션에 브로드캐스트
        for (session in webSocketKimpTickerHandler.sessions) {
            webSocketKimpTickerHandler.broadcast(diffTickerJson)
        }

        // 직전 TickerMap 갱신
        keyValueStoreService.upsertBeforeExchangeTickerDto(
            exchangeTickerDto = exchangeTickerDto
        )
    }


    private fun getDiffKimpTickerMap(
        kimpTickerMap: MutableMap<String, KimpTickerDto>,
        beforeKimpTickerMap: Map<String, KimpTickerDto> = mutableMapOf()
    ): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()

        for ((symbol, currentTicker) in kimpTickerMap) {
            val beforeTicker = beforeKimpTickerMap[symbol]
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
}