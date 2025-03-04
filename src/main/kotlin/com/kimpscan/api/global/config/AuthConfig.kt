package com.kimpscan.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
data class AuthConfig (
    val origin: String,
    val successUrl: String,
    val failureUrl: String,
)
