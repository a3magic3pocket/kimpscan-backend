package com.kimpscan.api.exchange.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaTopic
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KimpProducer(
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    fun sendTicker(exchangeTickerDto: ExchangeTickerDto) {
        val message = objectMapper.writeValueAsString(exchangeTickerDto)

        kafkaTemplate.send(KafkaTopic.TICKER, message)
    }

}