package com.kimpscan.api.auth.controller

import com.kimpscan.api.auth.dto.AuthTokenDto
import com.kimpscan.api.auth.service.AuthService
import com.kimpscan.api.auth.service.OAuth2Service
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class OAuthController(
    private val oAuth2Service: OAuth2Service,
    private val authService: AuthService,
) {
    companion object {
        const val GOOGLE = "google"
    }

    @PostMapping("/login/oauth2/code/google/web")
    suspend fun loginGoogle(
        request: HttpServletRequest,
        @RequestParam code: String
    ): ResponseEntity<AuthTokenDto> {
        val tokenResponse = oAuth2Service.getGoogleAccessToken(code = code) ?: throw Exception("401 에러")


        val googleUser = oAuth2Service.getGoogleUser(
            accessToken = tokenResponse.accessToken
        ) ?: throw Exception("401 에러")

        val user = authService.getUser(
            sub = googleUser.id,
            provider = GOOGLE,
            name = googleUser.name
        )

        val authTokenDto = authService.getAuthTokenDto(sub = user.id.toString())

        return ResponseEntity.ok(authTokenDto)
    }
}