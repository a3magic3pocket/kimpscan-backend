package com.kimpscan.api.auth

import com.kimpscan.api.user.entity.User
import com.kimpscan.api.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
) {

    fun getUser(sub: String, provider: String, name: String): User {
        var user = userRepository.findByOauth2SubAndIsActive(
            oauth2Sub = sub,
        )
        if (user == null) {
            val newUser = User(
                name = name,
                oauth2Sub = sub,
                oauth2Provider = provider
            )

            user = userRepository.save(newUser)
        }

        return user
    }

    fun getAuthTokenDto(sub: String): AuthTokenDto {
        val authTokenDto = jwtProvider.createToken(
            sub = sub,
        )

        return authTokenDto
    }
}