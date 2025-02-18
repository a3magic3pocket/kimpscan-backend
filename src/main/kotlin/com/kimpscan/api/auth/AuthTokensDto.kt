package com.kimpscan.api.auth

data class AuthTokensDto (
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiry: String,
    val refreshTokenExpiry: String
)