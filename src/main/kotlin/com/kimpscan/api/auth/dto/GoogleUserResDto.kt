package com.kimpscan.api.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserResDto (

    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("verified_email")
    val verifiedEmail: Boolean,

    @SerialName("name")
    val name: String,

    @SerialName("given_name")
    val givenName: String,

    @SerialName("family_name")
    val familyName: String,

    @SerialName("picture")
    val picture: String,

    @SerialName("hd")
    val hd: String? = null,

)