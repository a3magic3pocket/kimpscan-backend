package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.ServiceLeaderLock
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ServiceLeaderLockRepository : JpaRepository<ServiceLeaderLock, Int> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByName(name: String): ServiceLeaderLock?

}