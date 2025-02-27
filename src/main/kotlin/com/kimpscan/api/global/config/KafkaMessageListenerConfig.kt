package com.kimpscan.api.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener

@Configuration
class KafkaMessageListenerConfig(
    private val seekToEndRebalanceListener: SeekToEndRebalanceListener,
) {

    @Value("\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapServers: String

    fun createKafkaMessageListenerContainer(
        topic: String,
        groupId: String,
        messageListener: MessageListener<String, String>
    ): KafkaMessageListenerContainer<String, String> {
        val consumerProps = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProps)

        // 구독할 토픽을 동적으로 지정
        val containerProperties = ContainerProperties(topic)

        // 메시지를 수동으로 처리할 Listener
        containerProperties.messageListener = messageListener

        // 최신 오프셋에서 시작하도록 설정
        containerProperties.setConsumerRebalanceListener(seekToEndRebalanceListener)

        return KafkaMessageListenerContainer(consumerFactory, containerProperties)
    }

}