package com.kimpscan.api.auth.dto

data class AuthTokenDto (
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiry: String,
    val refreshTokenExpiry: String
)