package com.kimpscan.api.exchange.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Entity
@Table(name = "symbol_info")
data class SymbolInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "symbol", nullable = false, length = 100)
    val symbol: String,

    @Column(name = "kor_name", nullable = false, length = 100)
    var korName: String,

    @Column(name = "upbit_warning", nullable = false, length = 100)
    var upbitWarning: Boolean,

    @Column(name = "binance_symbol_status", nullable = false, length = 100)
    var binanceSymbolStatus: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

)