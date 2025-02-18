package com.kimpscan.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.kimpscan.api.constant.Auth
import com.kimpscan.api.global.config.JwtConfig
import com.kimpscan.api.user.entity.User
import com.kimpscan.api.user.repository.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OidcLoginSuccessHandler(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val jwtConfig: JwtConfig,
    private val objectMapper: ObjectMapper,
) : AuthenticationSuccessHandler {
    // 로그인 성공 후 호출되는 메서드
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication.isAuthenticated) {
            val oidcUser = authentication.principal as OidcUser

            // kimpscan 유저 로드
            var user = userRepository.findByOauth2SubAndIsActive(
                oauth2Sub = oidcUser.subject,
            )
            if (user == null) {
                val provider = when {
                    oidcUser.issuer.toString().contains(Auth.PROVIDER_GOOGLE) -> Auth.PROVIDER_GOOGLE
                    else -> ""
                }
                val newUser = User(
                    name = oidcUser.fullName,
                    oauth2Sub = oidcUser.subject,
                    oauth2Provider = provider
                )

                user = userRepository.save(newUser)
            }

            // jwt 발급
            val authTokensDto = jwtProvider.createToken(
                sub = user.id.toString()
            )

            // 쿠키 설정
            // production 환경에서는 secure = true 이어야 함
            val accessTokenCookie = Cookie(Auth.ACCESS_TOKEN_COOKIE_NAME, authTokensDto.accessToken).apply {
                isHttpOnly = true
                path = "/"
                secure = false
                maxAge = jwtConfig.accessExpirationTime
            }

            val refreshTokenCookie = Cookie(Auth.REFRESH_TOKEN_COOKIE_NAME, authTokensDto.refreshToken).apply {
                isHttpOnly = true
                path = "/"
                secure = false
                maxAge = jwtConfig.refreshExpirationTime
            }

            val dummyAccessTokenCookie = Cookie("dummy-access-token", "").apply {
                isHttpOnly = false
                path = "/"
                secure = false
                maxAge = jwtConfig.refreshExpirationTime
            }

            val dummyRefreshTokenCookie = Cookie("dummy-refresh-token", "").apply {
                isHttpOnly = true
                path = "/"
                secure = false
                maxAge = jwtConfig.refreshExpirationTime
            }

            // 쿠키를 응답에 추가
            for (cookie in listOf(
                accessTokenCookie,
                refreshTokenCookie,
                dummyAccessTokenCookie,
                dummyRefreshTokenCookie
            )) {
                response.addCookie(refreshTokenCookie)
            }

            // 응답 설정
            response.status = HttpServletResponse.SC_OK
            response.contentType = MediaType.APPLICATION_JSON_VALUE

            objectMapper.writeValue(response.outputStream, authTokensDto)
            return
        }

        // OIDC 인증 실패
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "unauthorized access")
    }
}