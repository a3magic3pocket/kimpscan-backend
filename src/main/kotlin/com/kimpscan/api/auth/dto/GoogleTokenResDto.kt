package com.kimpscan.api.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GoogleTokenResDto(

    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String? = null,

    @SerialName("expires_in")
    val expiresIn: Int? = null,

    @SerialName("scope")
    val scope: String? = null,

    @SerialName("token_type")
    val tokenType: String? = null,

    @SerialName("id_token")
    val idToken: String? = null,

    @SerialName("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int? = null

)
