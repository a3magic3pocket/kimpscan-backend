package com.kimpscan.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
data class AuthConfig (
    val successUrl: String,
    val failureUrl: String,
)
