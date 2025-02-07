package com.kimpscan.api.config

import com.kimpscan.api.constant.KafkaTopic
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class KafkaStreamsConfig {

    fun kStreamsConfigs(): KafkaStreamsConfiguration {
        val props = mapOf<String, Any>(
            StreamsConfig.APPLICATION_ID_CONFIG to "kafka-streams-main",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass
        )
        return KafkaStreamsConfiguration(props)
    }

    @Bean("tickerStreamsBuilder")
    fun tickerStreamsBuilder(): StreamsBuilderFactoryBean {
        return StreamsBuilderFactoryBean(
            kStreamsConfigs()
        )
    }


    @Bean("tickerStream")
    fun tickerStream(
        @Qualifier("tickerStreamsBuilder") streamsBuilder: StreamsBuilder
    ): KStream<String, String> {
        val stream =
            streamsBuilder.stream(KafkaTopic.TICKER, Consumed.with(Serdes.String(), Serdes.String()))

        stream
            .mapValues { value -> "변환된: $value" } // 데이터 변환
            .foreach { key, value -> println("[Stream] 키: $key, 값: $value") } // 콘솔 출력

        return stream
    }

}