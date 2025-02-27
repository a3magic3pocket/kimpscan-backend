package com.kimpscan.api.exchange.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Entity
@Table(name = "service_leader_lock")
data class ServiceLeaderLock(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "container_id", nullable = true, length = 100)
    var containerId: String? = null,

    @Column(name = "timestamp", nullable = false)
    var timestamp: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

)