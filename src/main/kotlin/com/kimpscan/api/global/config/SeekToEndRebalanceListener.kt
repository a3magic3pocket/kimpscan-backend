package com.kimpscan.api.global.config

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener
import org.springframework.stereotype.Component

@Component
class SeekToEndRebalanceListener: ConsumerAwareRebalanceListener {
    override fun onPartitionsAssigned(consumer: Consumer<*, *>, partitions: MutableCollection<TopicPartition>) {
        // 파티션 할당 시, 해당 파티션의 오프셋을 끝으로 이동
        partitions.forEach { partition ->
            consumer.seekToEnd(listOf(partition))
        }
    }
}
