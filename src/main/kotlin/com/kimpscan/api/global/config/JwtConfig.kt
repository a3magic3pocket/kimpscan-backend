package com.kimpscan.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtConfig (
    val secretKey: String,
    val accessExpirationTime: Int,
    val refreshExpirationTime: Int,
)