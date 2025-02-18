package com.kimpscan.api.global.config

import com.kimpscan.api.exchange.handler.WebSocketKimpMovingAvgHandler
import com.kimpscan.api.exchange.handler.WebSocketKimpTickerHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val webSocketKimpTickerHandler: WebSocketKimpTickerHandler,
    private val webSocketKimpMovingAvgHandler: WebSocketKimpMovingAvgHandler,
) : WebSocketConfigurer {

    // WebSocket 핸들러를 등록하는 메서드
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSocketKimpTickerHandler, "/ws")
            .addHandler(webSocketKimpMovingAvgHandler, "/ws/moving-avg")
            .setAllowedOrigins("*") // CORS 설정
    }

}