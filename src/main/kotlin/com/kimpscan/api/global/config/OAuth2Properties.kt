package com.kimpscan.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
data class OAuth2Properties(
    val registration: Map<String, ClientRegistrationProperties>,
    val provider: Map<String, ProviderProperties>
)

data class ClientRegistrationProperties(
    val clientId: String,
    val clientSecret: String,
    val authorizationGrantType: String,
    val scope: List<String>,
    val redirectUri: String
)

data class ProviderProperties(
    val tokenUri: String,
    val userInfoUri: String
)