package com.kimpscan.api.exchange.controller

import com.kimpscan.api.exchange.service.ExchangeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exchange")
class ExchangeController(
    private val exchangeService: ExchangeService
) {

    @GetMapping("/world")
    suspend fun hello(): String {
        exchangeService.getKimp()

        return "hello"
    }

}