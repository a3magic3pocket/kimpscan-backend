package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.KeyValueStore
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface KeyValueStoreRepository : JpaRepository<KeyValueStore, Int> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByKeyWithLock(key: String): KeyValueStore?

    fun findByKey(key: String): KeyValueStore?

}