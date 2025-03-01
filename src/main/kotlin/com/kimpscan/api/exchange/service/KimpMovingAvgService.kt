package com.kimpscan.api.exchange.service

import com.kimpscan.api.exchange.dto.ExchangeTickerDto
import com.kimpscan.api.exchange.dto.KimpTickerDto
import com.kimpscan.api.exchange.handler.WebSocketKimpMovingAvgHandler
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class KimpMovingAvgService(
    private val webSocketKimpMovingAvgHandler: WebSocketKimpMovingAvgHandler
) {

    // 김프를 저장하는 슬라이딩 윈도우 (종목별로 관리)
    private val kimpData: MutableMap<String, MutableList<Double>> = ConcurrentHashMap()
    private var beforeKimpMovingAvgMap = mutableMapOf<String, MutableList<List<Double>>>()
    private var isInit = false

    fun consume(exchangeTicker: ExchangeTickerDto, beforeKimpTickerMap: MutableMap<String, KimpTickerDto>) {
        val result: MutableMap<String, List<Double>> = mutableMapOf()

        for ((symbol, beforeKimpTicker) in beforeKimpTickerMap) {
            // kimpData에 해당 symbol이 없으면 빈 리스트로 초기화
            val kimpList = kimpData.computeIfAbsent(symbol) { mutableListOf() }

            // kimpList 크기가 20이면 첫 번째 요소를 제거
            if (kimpList.size == 20) {
                kimpList.removeFirst()
            }

            // kimp 값을 가져오고, 없으면 beforeKimpTicker.kimp 사용
            val kimp = exchangeTicker.kimpTickerMap[symbol]?.kimp ?: beforeKimpTicker.kimp
            kimpList.add(kimp.toDouble())

            val movingAvg5 = if (kimpList.size >= 5) {
                kimpList.take(5).average()
            } else {
                0.0
            }

            val movingAvg20 = if (kimpList.size == 20) {
                kimpList.average()
            } else {
                0.0
            }

            val movingAvgList = listOf(kimp.toDouble(), movingAvg5, movingAvg20)
            result[symbol] = movingAvgList

            // beforeKimpMovingAvgMap 갱신
            updateBeforeKimpMovingAvgMap(
                symbol = symbol,
                movingAvgList = movingAvgList
            )

        }

        isInit = true

        // 브로드 캐스트
        webSocketKimpMovingAvgHandler.broadcast(result)

    }

    fun getBeforeKimpMovingAvg(symbol: String): MutableList<List<Double>> {
        return beforeKimpMovingAvgMap[symbol] ?: mutableListOf()
    }

    private fun updateBeforeKimpMovingAvgMap(symbol: String, movingAvgList: List<Double>) {
        val beforeMovingAvgList = beforeKimpMovingAvgMap.getOrPut(symbol) { mutableListOf() }

        // 리스트 크기가 7 이상이면 가장 오래된 값 제거
        if (beforeMovingAvgList.size >= 7) {
            beforeMovingAvgList.removeAt(0)
        }

        // 새 값 추가
        beforeMovingAvgList.add(movingAvgList)
    }
}