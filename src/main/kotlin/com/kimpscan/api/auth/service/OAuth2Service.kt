package com.kimpscan.api.auth.service

import com.kimpscan.api.auth.controller.OAuthController.Companion.GOOGLE
import com.kimpscan.api.auth.dto.GoogleTokenResDto
import com.kimpscan.api.auth.dto.GoogleUserResDto
import com.kimpscan.api.global.config.OAuth2Properties
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class OAuth2Service(
    private val webClient: WebClient,
    private val oAuth2Properties: OAuth2Properties,
) {

    suspend fun getGoogleAccessToken(code: String, redirectUrl: String): GoogleTokenResDto? {
        val googleRegistration = oAuth2Properties.registration[GOOGLE] ?: throw NotFoundException()
        val googleProvider = oAuth2Properties.provider[GOOGLE] ?: throw NotFoundException()

        return webClient.post()
            .uri(googleProvider.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                "code=$code" +
                        "&client_id=${googleRegistration.clientId}" +
                        "&client_secret=${googleRegistration.clientSecret}" +
                        "&redirect_uri=$redirectUrl" +
                        "&grant_type=authorization_code"
            )
            .retrieve()
            .bodyToMono(GoogleTokenResDto::class.java)
            .awaitSingle()
    }

    suspend fun getGoogleUser(accessToken: String): GoogleUserResDto? {
        val googleProvider = oAuth2Properties.provider[GOOGLE] ?: throw NotFoundException()

        return webClient.get()
            .uri(googleProvider.userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .bodyToMono(GoogleUserResDto::class.java)
            .awaitSingle()
    }
}