package com.kimpscan.api.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class KimpscanOAuth2FailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: org.springframework.security.core.AuthenticationException?
    ) {
        exception?.printStackTrace()
        println("OAuth2 로그인 실패! 에러: ${exception?.message}"+ exception)
    }
}
