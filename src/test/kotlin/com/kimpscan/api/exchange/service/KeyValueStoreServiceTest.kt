package com.kimpscan.api.exchange.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.KeyValueStore
import com.kimpscan.api.exchange.repository.KeyValueStoreRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@Testcontainers
@ActiveProfiles("test")
class KeyValueStoreServiceTest {
    companion object {
        // MariaDB 컨테이너 설정
        @Container
        val mariaDB = MariaDBContainer("mariadb:lts-noble").apply {
            withDatabaseName("testdb")
            withUsername("root")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun mariaProperties(registry: DynamicPropertyRegistry) {
            // Testcontainers가 실행한 MariaDB 컨테이너에 연결할 수 있도록 동적으로 속성 추가
            registry.add("spring.datasource.url", mariaDB::getJdbcUrl)
            registry.add("spring.datasource.username", mariaDB::getUsername)
            registry.add("spring.datasource.password", mariaDB::getPassword)
        }
    }


    @Autowired
    lateinit var keyValueStoreService: KeyValueStoreService

    @Autowired
    lateinit var keyValueStoreRepository: KeyValueStoreRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `test upsert and retrieve BeforeKimpMovingAvgMap`() {
        val beforeKimpMovingAvgMap = mutableMapOf<String, MutableList<List<Double>>>()
        beforeKimpMovingAvgMap["BTCUSDT"] = mutableListOf(listOf(1.0, 2.0, 3.0))

        val savedKeyValueStore = keyValueStoreService.upsertBeforeKimpMovingAvgMap(beforeKimpMovingAvgMap)

        assertNotNull(savedKeyValueStore)
        assertEquals(KeyValueStore.BEFORE_KIMP_MOVING_AVG_MAP, savedKeyValueStore.key)

        // Retrieve the value and check if it matches
        val retrievedMap = keyValueStoreService.retrieveBeforeKimpMovingAvgMap()
        assertEquals(beforeKimpMovingAvgMap, retrievedMap)
    }

    @Test
    fun `test retrieve BeforeKimpMovingAvgMap with empty default value`() {
        // "BEFORE_KIMP_MOVING_AVG_MAP" 키로 데이터 가져오기
        val result = keyValueStoreService.retrieveBeforeKimpMovingAvgMap()

        assertNotNull(result)
        assertTrue(result.isEmpty())
        assertEquals(1, 2)
    }

}