package com.kimpscan.api.global.config

import com.kimpscan.api.auth.KimpscanOAuth2UserService
import com.kimpscan.api.auth.OidcLoginSuccessHandler
import com.kimpscan.api.auth.KimpscanOidcUserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val kimpscanOAuth2UserService: KimpscanOAuth2UserService,
    private val kimpscanOidcUserService: KimpscanOidcUserService,
    private val oidcLoginSuccessHandler: OidcLoginSuccessHandler,
) {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/exchange/**").permitAll()
                    .requestMatchers("/auth/success").permitAll()
                    .requestMatchers("/hello.html").permitAll()
                    .requestMatchers("/auth.html").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2Login { l ->
                l.userInfoEndpoint{ userInfo ->
                    userInfo.userService(kimpscanOAuth2UserService)
                    userInfo.oidcUserService(kimpscanOidcUserService)
                }
                l.successHandler(oidcLoginSuccessHandler)
            }

        return http.build()
    }

}