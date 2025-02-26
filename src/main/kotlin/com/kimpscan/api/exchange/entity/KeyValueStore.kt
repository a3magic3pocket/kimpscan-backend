package com.kimpscan.api.exchange.entity

import jakarta.persistence.*
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Entity
@Table(name = "key_value_store")
data class KeyValueStore(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val key: String,

    @Column(name = "container_id", nullable = true)
    var value: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

)