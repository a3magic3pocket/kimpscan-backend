package com.kimpscan.api.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KafkaTopic
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.WindowedSerdes
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import java.time.Duration

@Configuration
class KafkaStreamsConfig(
    private val objectMapper: ObjectMapper
) {

    fun kStreamsConfigs(): KafkaStreamsConfiguration {
        val props = mapOf<String, Any>(
            StreamsConfig.APPLICATION_ID_CONFIG to "kafka-streams-main",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
//            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass
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

        val typeRef = object : TypeReference<Map<String, String>>() {}

        val aggregated = stream
            .mapValues { value ->
                val parsed: Map<String, String> = objectMapper.readValue(value, typeRef)
                parsed.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
            } // 데이터 변환
            .flatMap { _, parsedMap ->
                parsedMap.map { (key, value) -> KeyValue(key, value) }
            }
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
            .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(5), Duration.ofSeconds(1)))
            .aggregate(
                { 0.0 }, // 초기 값 (이동평균 초기화)
                { _, value, aggValue ->
                    // 이동평균 계산
                    (aggValue + value) // 여기에서 이동평균 계산 로직을 정의
                },
                Materialized.with(Serdes.String(), Serdes.Double())
            )
            .mapValues { value -> value / 5 }

        val windowedSerde = WindowedSerdes.timeWindowedSerdeFrom(String::class.java, 5)  // Windowed<String>에 적합한 Serde
        val doubleSerde = Serdes.Double()  // Double 타입의 값에 적합한 Serde

        // 직렬화된 데이터로 Kafka로 전송
        aggregated.toStream()
        .foreach { key, value -> println("[Stream] 키: $key, 값: $value") } // 콘솔 출력
//            .to("output-topic", Produced.with(windowedSerde, doubleSerde))

        return stream
    }

}
