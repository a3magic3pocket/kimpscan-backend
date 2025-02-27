package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.KeyValueStore
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface KeyValueStoreRepository : JpaRepository<KeyValueStore, Int> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM KeyValueStore k WHERE k.key = :key")
    fun findByKeyWithLock(@Param("key") key: String): KeyValueStore?

    fun findByKey(key: String): KeyValueStore?

}