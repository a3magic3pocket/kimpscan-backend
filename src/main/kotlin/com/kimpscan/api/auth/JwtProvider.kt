package com.kimpscan.api.auth

import com.kimpscan.api.auth.dto.AuthTokenDto
import com.kimpscan.api.constant.Auth
import com.kimpscan.api.global.config.JwtConfig
import com.kimpscan.api.global.extension.toIsoUtcString
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultJwtBuilder
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class JwtProvider(
    private val jwtConfig: JwtConfig
) {
    // JWT 토큰 생성
    fun createToken(sub: String): AuthTokenDto {
        val now = Date()
        val accessExpiryDate = Date(now.time + jwtConfig.accessExpirationTime * 1000)  // 만료 시간을 밀리초로 변환
        val refreshExpiryDate = Date(now.time + jwtConfig.refreshExpirationTime * 1000)  // 만료 시간을 밀리초로 변환
        val jwtBuilder = DefaultJwtBuilder()

        val key = SecretKeySpec(jwtConfig.secretKey.toByteArray(), SignatureAlgorithm.HS512.jcaName)

        val accessToken = jwtBuilder
            .setSubject(sub)
            .setIssuedAt(now)
            .setExpiration(accessExpiryDate)
            .signWith(key)
            .compact()

        val refreshToken = jwtBuilder
            .setSubject(sub)
            .setIssuedAt(now)
            .setExpiration(refreshExpiryDate)
            .signWith(key)
            .compact()

        return AuthTokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = accessExpiryDate.toIsoUtcString(),
            refreshTokenExpiry = refreshExpiryDate.toIsoUtcString()
        )
    }

    // JWT 에서 유저 아이디 추출
    fun getUserId(token: String): String {
        val claims = Jwts.parserBuilder()  // parser() 대신 parserBuilder 사용
            .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.secretKey.toByteArray()))  // 서명 키 설정
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject
    }

    // JWT 유효성 검사 (Claims 사용)
    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()  // parser() 대신 parserBuilder 사용
                .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.secretKey.toByteArray()))  // 서명 키 설정
                .build()
                .parseClaimsJws(token)
                .body  // Claims 객체로 추출

            // Claims 에서 필요한 정보를 확인할 수 있음 (예: 만료일자 등)
            !claims.expiration.before(Date())  // 만료된 토큰인지 검사
        } catch (e: Exception) {
            false
        }
    }

    // 토큰에서 Authentication 객체 생성
    fun getAuthentication(token: String): Authentication {
        val userDetails = org.springframework.security.core.userdetails.User(
            getUserId(token), "", emptyList()  // 유저 ID를 가지고 UserDetails 생성
        )
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 요청에서 JWT 토큰을 추출
    fun resolveToken(request: HttpServletRequest): String? {
        // Authorization 헤더에서 토큰 추출
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)  // "Bearer "를 제외하고 토큰만 반환
        }

        // 쿠키에서 토큰 추출
        val cookies: Array<Cookie>? = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == Auth.ACCESS_TOKEN_COOKIE_NAME) {  // JWT 가 저장된 쿠키의 이름
                    return cookie.value  // 쿠키의 값을 반환
                }
            }
        }

        // Authorization 헤더와 쿠키 둘 다 없으면 null 반환
        return null
    }
}
