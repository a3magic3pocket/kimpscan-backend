package com.kimpscan.api.user.entity

import com.kimpscan.api.global.converter.BooleanToIntegerAttributeConverter
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime

@Entity
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "oauth2_sub", nullable = false)
    val oauth2Sub: String,

    @Column(name = "oauth2_provider", nullable = false)
    val oauth2Provider: String,

    @Convert(converter = BooleanToIntegerAttributeConverter::class)
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    @CreationTimestamp
    var createdAt: ZonedDateTime? = null,

    @Column(name = "updated_at", nullable = true, insertable = true, updatable = true)
    @UpdateTimestamp
    var updatedAt: ZonedDateTime? = null
)