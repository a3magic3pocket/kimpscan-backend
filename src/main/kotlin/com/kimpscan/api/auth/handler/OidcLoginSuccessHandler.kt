package com.kimpscan.api.auth.handler

import com.kimpscan.api.auth.service.AuthService
import com.kimpscan.api.constant.Auth
import com.kimpscan.api.global.config.AuthConfig
import com.kimpscan.api.global.config.JwtConfig
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OidcLoginSuccessHandler(
    private val jwtConfig: JwtConfig,
    private val authConfig: AuthConfig,
    private val authService: AuthService,
) : AuthenticationSuccessHandler {

    // 로그인 성공 후 호출되는 메서드
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication.isAuthenticated) {
            val oidcUser = authentication.principal as OidcUser
            val provider = when {
                oidcUser.issuer.toString().contains(Auth.PROVIDER_GOOGLE) -> Auth.PROVIDER_GOOGLE
                else -> ""
            }

            // kimpscan 유저 로드
            val user = authService.getUser(
                sub = oidcUser.subject,
                provider = provider,
                name = oidcUser.fullName
            )

            val authTokenDto = authService.getAuthTokenDto(sub = user.id.toString())

            // 쿠키 설정
            // production 환경에서는 secure = true 이어야 함
            val accessTokenCookie = Cookie(Auth.ACCESS_TOKEN_COOKIE_NAME, authTokenDto.accessToken).apply {
                isHttpOnly = true
                path = "/"
                secure = false
                maxAge = jwtConfig.accessExpirationTime
            }

            val refreshTokenCookie = Cookie(Auth.REFRESH_TOKEN_COOKIE_NAME, authTokenDto.refreshToken).apply {
                isHttpOnly = true
                path = "/"
                secure = false
                maxAge = jwtConfig.refreshExpirationTime
            }

            val dummyAccessTokenCookie = Cookie("dummy-access-token", "1").apply {
                isHttpOnly = false
                path = "/"
                secure = false
                maxAge = jwtConfig.accessExpirationTime
            }

            val dummyRefreshTokenCookie = Cookie("dummy-refresh-token", "2").apply {
                isHttpOnly = false
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
                response.addCookie(cookie)
            }

            response.sendRedirect(authConfig.successUrl)
            return
        }

        // OIDC 인증 실패
        response.sendRedirect(authConfig.failureUrl)
    }
}