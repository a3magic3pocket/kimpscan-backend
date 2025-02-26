package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.entity.ServiceLeaderLock
import com.kimpscan.api.constant.ServiceLeaderLock as ServiceLeaderLockConst
import com.kimpscan.api.exchange.repository.ServiceLeaderLockRepository
import com.kimpscan.api.global.config.AppConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
class ServiceLeaderLockService(
    private val appConfig: AppConfig,
    private val serviceLeaderLockRepository: ServiceLeaderLockRepository,
) {
    companion object {
        // time-to-live for service leader lock
        val ttl: Duration = Duration.ofSeconds(1).plusMillis(2)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun tryToAcquireLock(): Boolean {
        val lock = serviceLeaderLockRepository.findByName(
            name = ServiceLeaderLockConst.NAME,
        )

        // 최초 락 점유
        if (lock == null) {
            val serviceLeaderLock = ServiceLeaderLock(
                name = ServiceLeaderLockConst.NAME,
                containerId = appConfig.containerId,
                timestamp = ZonedDateTime.now(ZoneOffset.UTC),
            )

            serviceLeaderLockRepository.save(serviceLeaderLock)

            return true
        }

        // 현재 컨테이너가 락을 점유하고 있는 상황
        // - timestamp 갱신
        if (lock.containerId == appConfig.containerId) {
            lock.timestamp = ZonedDateTime.now(ZoneOffset.UTC)

            serviceLeaderLockRepository.save(lock)

            return true
        }

        // TTL 만료 시
        val utcNow = ZonedDateTime.now(ZoneOffset.UTC)
        val expirationTime = lock.timestamp.plus(ttl)
        if (utcNow.isAfter(expirationTime)) {
            lock.containerId = appConfig.containerId
            lock.timestamp = ZonedDateTime.now(ZoneOffset.UTC)

            serviceLeaderLockRepository.save(lock)

            return true
        }

        return false
    }
}