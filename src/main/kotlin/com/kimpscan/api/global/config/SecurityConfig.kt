package com.kimpscan.api.global.config

import com.kimpscan.api.auth.handler.KimpscanOAuth2FailureHandler
import com.kimpscan.api.auth.service.KimpscanOAuth2UserService
import com.kimpscan.api.auth.handler.OidcLoginSuccessHandler
import com.kimpscan.api.auth.service.KimpscanOidcUserService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2Properties::class)
class SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource,
    private val kimpscanOAuth2UserService: KimpscanOAuth2UserService,
    private val kimpscanOidcUserService: KimpscanOidcUserService,
    private val oidcLoginSuccessHandler: OidcLoginSuccessHandler,
    private val kimpscanOAuth2FailureHandler: KimpscanOAuth2FailureHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/exchange/**").permitAll()
                    .requestMatchers("/auth/success").permitAll()
                    .requestMatchers("/hello.html").authenticated()
                    .requestMatchers("/auth.html").permitAll()
                    .requestMatchers("/auth2.html").permitAll()
                    .requestMatchers("/login/oauth2/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2Login { l ->
                l.userInfoEndpoint { userInfo ->
//                    userInfo.userService(kimpscanOAuth2UserService)
                    userInfo.oidcUserService(kimpscanOidcUserService)
                }
                l.successHandler(oidcLoginSuccessHandler)
                l.failureHandler(kimpscanOAuth2FailureHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)  // JWT 필터 등록

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:63342", "https://clarify.kr")  // 허용할 도메인
        corsConfiguration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메소드
        corsConfiguration.allowedHeaders = listOf("Authorization", "Content-Type")  // 허용할 헤더
        corsConfiguration.allowCredentials = true  // 쿠키와 인증 정보를 포함할 수 있도록 허용

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)  // 모든 요청에 대해 CORS 설정
        return source
    }


}