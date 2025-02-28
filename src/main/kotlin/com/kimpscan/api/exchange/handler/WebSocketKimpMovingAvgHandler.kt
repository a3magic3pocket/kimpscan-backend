package com.kimpscan.api.exchange.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

// WebSocket 세션을 관리하고, 메시지를 처리하는 핸들러
@Component
class WebSocketKimpMovingAvgHandler(
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {

    val subscriptions = ConcurrentHashMap<WebSocketSession, String?>()

    // 클라이언트가 WebSocket 에 연결되면 세션을 저장
    override fun afterConnectionEstablished(session: WebSocketSession) {
        subscriptions[session] = ""
    }

    // 클라이언트가 연결을 끊으면 세션에서 제거
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        subscriptions.remove(session)
    }

    // 세션에 심볼 갱신
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload.trim()
        subscriptions[session] = payload
    }

    // 메시지를 브로드캐스트하는 메서드
    fun broadcast(kimpMovingAvgMap: MutableMap<String, List<Double>>) {
        subscriptions.forEach { (session, symbol) ->
            if (symbol == "") {
                return
            }

            val movingAvg = kimpMovingAvgMap[symbol]
            if (session.isOpen && movingAvg != null) {
                val message = objectMapper.writeValueAsString(movingAvg)
                session.sendMessage(
                    TextMessage(message)
                ) // 메시지 전송
            }
        }
    }
}
