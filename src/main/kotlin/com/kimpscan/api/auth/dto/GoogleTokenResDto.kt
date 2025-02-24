package com.kimpscan.api.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GoogleTokenResDto(

    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("expires_in")
    val expiresIn: Int,

    @SerialName("scope")
    val scope: String,

    @SerialName("token_type")
    val tokenType: String,

    @SerialName("id_token")
    val idToken: String,

)
