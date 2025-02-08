package com.kimpscan.api.exchange.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KimpProducer(
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    fun sendTicker(kimpMap: Map<String, String>) {
        val message = objectMapper.writeValueAsString(kimpMap)

        kafkaTemplate.send(KafkaTopic.TICKER, message)
    }

}