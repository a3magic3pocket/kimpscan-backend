package com.kimpscan.api.exchange.controller

import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.SymbolInfoSearchResDto
import com.kimpscan.api.exchange.service.ExchangeService
import com.kimpscan.api.exchange.service.SymbolInfoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exchange")
class ExchangeController(
    private val exchangeService: ExchangeService,
    private val symbolInfoService: SymbolInfoService,
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

    @GetMapping("/symbols/search")
    fun searchSymbols(
        @RequestParam(name = "keyword") keyword: String?,
        @RequestParam(name = "isStatusTrading") isStatusTrading: Boolean = true
    ): ResponseEntity<List<SymbolInfoSearchResDto>?> {
        val symbolInfos = symbolInfoService.searchSymbolInfo(
            keyword = keyword,
            isStatusTrading = isStatusTrading
        )

        return ResponseEntity.ok().body(
            symbolInfos.map { symbolInfo ->
                SymbolInfoSearchResDto(
                    symbol = symbolInfo.symbol,
                    rootSymbol = symbolInfo.symbol.substring(0, symbolInfo.symbol.length - 4),
                    korName = symbolInfo.korName
                )
            }
        )

    }

}