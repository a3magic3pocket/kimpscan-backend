package com.kimpscan.api.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController {

    @GetMapping("/private")
    fun getInitTicker2(): ResponseEntity<String> {
        return ResponseEntity.ok().body(
            "private"
        )
    }

    @GetMapping("/success")
    fun getInitTicker(): ResponseEntity<String> {
        return ResponseEntity.ok().body(
            "success"
        )
    }

}
