package com.kimpscan.api.exchange.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.entity.KeyValueStore
import com.kimpscan.api.exchange.repository.KeyValueStoreRepository
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.kimpscan.api.constant.KeyValueStore as KeyValueStoreConst

@Service
class KeyValueStoreService(
    private val objectMapper: ObjectMapper,
    private val keyValueStoreRepository: KeyValueStoreRepository,
) {

    @Transactional(rollbackFor = [Exception::class])
    fun upsertBeforeKimpMovingAvgMap(
        beforeKimpMovingAvgMap: MutableMap<String, MutableList<List<Double>>>
    ): KeyValueStore {
        return upsertKeyValueStore(
            key = KeyValueStoreConst.BEFORE_KIMP_MOVING_AVG_MAP,
            value = beforeKimpMovingAvgMap
        )
    }

    @Transactional(readOnly = true)
    fun retrieveBeforeKimpMovingAvgMap(): MutableMap<String, MutableList<List<Double>>> {
        return retrieveValue(
            key = KeyValueStoreConst.BEFORE_KIMP_MOVING_AVG_MAP,
            defaultValue = mutableMapOf()
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun upsertKimpMovingAvgCache(
        kimpMovingAvgCache: MutableMap<String, MutableList<Double>>
    ): KeyValueStore {
        return upsertKeyValueStore(
            key = KeyValueStoreConst.KIMP_MOVING_AVG_CACHE,
            value = kimpMovingAvgCache
        )
    }

    @Transactional(readOnly = true)
    fun retrieveKimpMovingAvgCache(): MutableMap<String, MutableList<Double>> {
        return retrieveValue(
            key = KeyValueStoreConst.KIMP_MOVING_AVG_CACHE,
            defaultValue = mutableMapOf()
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun upsertBeforeExchangeTickerDto(exchangeTickerDto: ExchangeTickerDto): KeyValueStore {
        return upsertKeyValueStore(
            key = KeyValueStoreConst.BEFORE_EXCHANGE_TICKER_DTO,
            value = exchangeTickerDto
        )
    }

    @Transactional(readOnly = true)
    fun retrieveBeforeExchangeTickerDto(): ExchangeTickerDto {
        return retrieveValue(
            key = KeyValueStoreConst.BEFORE_EXCHANGE_TICKER_DTO,
            defaultValue = ExchangeTickerDto(
                usdWonExRage = 0.0,
                kimpTickerMap = mutableMapOf()
            )
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun upsertKeyValueStore(key: String, value: Any): KeyValueStore {
        val exchangeTickerDtoJson = objectMapper.writeValueAsString(value)
        val savedKeyValueStore =
            keyValueStoreRepository.findByKeyWithLock(key)
        if (savedKeyValueStore == null) {
            val keyValueStore = KeyValueStore(
                key = key,
                value = exchangeTickerDtoJson,
            )

            return keyValueStoreRepository.save(keyValueStore)
        }

        savedKeyValueStore.value = exchangeTickerDtoJson

        return keyValueStoreRepository.save(savedKeyValueStore)
    }

    @Transactional(readOnly = true)
    private inline fun <reified T> retrieveValue(key: String, defaultValue: T): T {
        val savedKeyValueStore = keyValueStoreRepository.findByKey(key) ?: return defaultValue

        return try {
            Json.decodeFromString<T>(savedKeyValueStore.value)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
}