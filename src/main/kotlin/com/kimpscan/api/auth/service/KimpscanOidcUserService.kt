package com.kimpscan.api.auth.service

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KimpscanOidcUserService : OidcUserService() {

    @Transactional(rollbackFor = [Exception::class])
    override fun loadUser(oidcUserRequest: OidcUserRequest): OidcUser {
        return super.loadUser(oidcUserRequest)
    }

}