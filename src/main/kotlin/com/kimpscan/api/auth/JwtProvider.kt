package com.kimpscan.api.auth

import com.kimpscan.api.global.config.JwtConfig
import com.kimpscan.api.global.extension.toIsoUtcString
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultJwtBuilder
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class JwtProvider(
    private val jwtConfig: JwtConfig
) {
    // JWT 토큰 생성
    fun createToken(sub: String): AuthTokensDto {
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

        return AuthTokensDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = accessExpiryDate.toIsoUtcString(),
            refreshTokenExpiry = refreshExpiryDate.toIsoUtcString()
        )
    }
}
