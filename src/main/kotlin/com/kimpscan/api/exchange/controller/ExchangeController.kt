package com.kimpscan.api.exchange.controller

import com.kimpscan.api.exchange.client.UpbitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exchange")
class ExchangeController(private val upbitClient: UpbitClient) {

    @GetMapping("/world")
    fun hello(): String {

        runBlocking {
            val result = listOf(
                async { upbitClient.getTicker() }
            ).map { it.await() }

            println("result" + result)
        }
        return "kim"
    }

}