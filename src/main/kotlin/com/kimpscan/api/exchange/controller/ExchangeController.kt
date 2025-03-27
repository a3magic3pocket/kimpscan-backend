package com.kimpscan.api.exchange.controller

import com.kimpscan.api.exchange.dto.ExchangeTickerDto
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
) {

    @GetMapping("/tickers/init")
    fun getInitTicker(): ResponseEntity<ExchangeTickerDto> {
        return ResponseEntity.ok().body(
            exchangeService.getBeforeExchangeDto()
        )
    }

    @GetMapping("/moving-avgs/init")
    fun getInitMovingAvg(
        @RequestParam(name = "symbol") symbol: String
    ): ResponseEntity<MutableList<List<Double>>> {
        return ResponseEntity.ok().body(
            exchangeService.getBeforeKimpMovingAvg(symbol.uppercase())
        )
    }


}