package com.kimpscan.api.exchange.controller

import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.kafka.KimpMovingAvgConsumer
import com.kimpscan.api.exchange.service.ExchangeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exchange")
class ExchangeController(
    private val exchangeService: ExchangeService,
    private val kimpMovingAvgConsumer: KimpMovingAvgConsumer,
) {

    @GetMapping("/tickers/init")
    fun getInitTicker(): ResponseEntity<MutableMap<String, KimpTickerDto>> {
        return ResponseEntity.ok().body(
            exchangeService.getBeforeKimpTickerMap()
        )
    }

    @GetMapping("/moving-avg/init")
    fun getInitMovingAvg(
        @RequestParam(name = "symbol") symbol: String
    ): ResponseEntity<MutableList<List<Double>>> {
        return ResponseEntity.ok().body(
            kimpMovingAvgConsumer.getBeforeKimpMovingAvg(symbol.uppercase())
        )
    }


}