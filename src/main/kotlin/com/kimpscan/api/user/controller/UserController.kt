package com.kimpscan.api.user.controller

import com.kimpscan.api.global.dto.SimpleSuccessResDto
import com.kimpscan.api.user.dto.FcmReqDto
import com.kimpscan.api.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User as SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping(value = ["/fcm"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addFcm(
        @AuthenticationPrincipal securityUser: SecurityUser,
        @RequestBody @Valid fcmReqDto: FcmReqDto,
    ): ResponseEntity<SimpleSuccessResDto> {
        userService.addFcm(
            userId = securityUser.username.toLong(),
            fcmKey = fcmReqDto.key
        )

        return ResponseEntity.ok().body(
            SimpleSuccessResDto(message = "success")
        )
    }
}
