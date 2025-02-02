package com.kimpscan.api.config

import com.kimpscan.api.exchange.handler.WebSocketMessageHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val webSocketMessageHandler: WebSocketMessageHandler
) : WebSocketConfigurer {

    // WebSocket 핸들러를 등록하는 메서드
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSocketMessageHandler, "/ws")
            .setAllowedOrigins("*") // CORS 설정
    }

}