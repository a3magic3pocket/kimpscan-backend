package com.kimpscan.api.auth

import com.kimpscan.api.global.config.OAuth2Properties
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

data class GoogleErrorResponse(val error: String, val error_description: String)

@RestController
class OAuthController(
    private val webClient: WebClient,
    private val oAuth2Properties: OAuth2Properties,
) {

    @PostMapping("/login/oauth2/code/google/web")
    fun getGoogleUser(request: HttpServletRequest, @RequestParam code: String): String {
        val googleRegistration = oAuth2Properties.registration["google"] ?: throw NotFoundException()
        val googleProvider = oAuth2Properties.provider["google"] ?: throw NotFoundException()
        val serverPortPhrase = if (request.serverPort in setOf(80, 443)) {
            ""
        } else {
            ":${request.serverPort}"
        }
        val redirectUrl = "${request.scheme}://${request.serverName}$serverPortPhrase"

        val aaa = webClient.post()
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
            .onStatus({ status -> status.is4xxClientError }, { response ->
                // response는 DefaultClientResponse 타입이므로, bodyToMono를 호출하여 에러 본문을 추출합니다.
                response.bodyToMono(GoogleErrorResponse::class.java)
                    .flatMap { errorResponse ->
                        // 에러 응답 본문에서 더 구체적인 에러 메시지를 추출하여 로그로 출력
                        println("Google OAuth Error: ${errorResponse.error}, Description: ${errorResponse.error_description}")
                        Mono.error(RuntimeException("API Error: ${errorResponse.error_description}"))
                    }
            })
            .bodyToMono(Map::class.java)
//            .flatMap { tokenResponse ->
//                println("tokenResponse++" + tokenResponse)
//                val accessToken = tokenResponse["access_token"] as String
//                println("accessToken++" + accessToken)
//                // 구글 사용자 정보 가져오기
//                webClient.get()
//                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
//                    .headers { headers -> headers.setBearerAuth(accessToken) }
//                    .retrieve()
//                    .bodyToMono(String::class.java)
//            }
//            .doOnTerminate { // 사용자 정보를 받으면 변수에 저장
//                println("success")
//            }
//            .doOnError { e ->
//                // 요청이 실패했을 때 실행될 동작 (예: 로그 출력)
//                println("Error occurred: ${e.message}")
//            }
        aaa.subscribe(
            { response ->
                println("response++" + response)
            },
            { error ->
                // 실패한 경우 여기서 에러 처리
                println("error++" + error)
                println("Error caught: ${error.message}")
            }
        )

        return "kim"

    }
}