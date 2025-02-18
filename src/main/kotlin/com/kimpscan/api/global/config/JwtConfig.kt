package com.kimpscan.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtConfig (
    val secretKey: String,
    val accessExpirationTime: Int,
    val refreshExpirationTime: Int,
)