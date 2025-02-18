package com.kimpscan.api.auth

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class KimpscanOAuth2UserService: DefaultOAuth2UserService() {

    @Override
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        return super.loadUser(userRequest)
    }

}