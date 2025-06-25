package com.kimpscan.api.auth.service

import com.kimpscan.api.auth.controller.OAuthController.Companion.GOOGLE
import com.kimpscan.api.auth.dto.GoogleTokenResDto
import com.kimpscan.api.auth.dto.GoogleUserResDto
import com.kimpscan.api.global.config.OAuth2Properties
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Service
class OAuth2Service(
    private val webClient: WebClient,
    private val oAuth2Properties: OAuth2Properties,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)
    }

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
            .onStatus(HttpStatusCode::is4xxClientError) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .flatMap { errorBody ->
                        // ⭐⭐⭐ Google의 실제 상세 오류 메시지 여기에 출력 ⭐⭐⭐
                        // 이 로그 라인이 문제 해결의 핵심 단서를 제공할 것입니다!
                        logger.error("Google API 상세 오류 응답 (${clientResponse.statusCode()}): $errorBody")
                        Mono.error(WebClientResponseException(
                            clientResponse.statusCode().value(),
                            "Google Token Exchange Failed",
                            clientResponse.headers().asHttpHeaders(),
                            errorBody.toByteArray(),
                            StandardCharsets.UTF_8 // 응답 본문 인코딩에 맞게 설정
                        ))
                    }
            }
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