package com.kimpscan.api.exchange.kafka

import com.kimpscan.api.constant.KafkaTopic
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class ExchangeTickerProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    fun sendTicker(exchangeTickerDto: ExchangeTickerDto) {
        val message = Json.encodeToString(exchangeTickerDto)

        kafkaTemplate.send(KafkaTopic.EXCHANGE_TICKER, message)
    }

}
