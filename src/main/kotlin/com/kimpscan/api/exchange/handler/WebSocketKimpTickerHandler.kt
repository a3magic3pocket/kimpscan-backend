package com.kimpscan.api.exchange.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

// WebSocket 세션을 관리하고, 메시지를 처리하는 핸들러
@Component
class WebSocketKimpTickerHandler : TextWebSocketHandler() {

    val sessions = mutableSetOf<WebSocketSession>()

    // 클라이언트가 WebSocket 에 연결되면 세션을 저장
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        println("sessions" + sessions)
        println("WebSocket connection established: ${session.id}")
    }

    // 클라이언트가 연결을 끊으면 세션에서 제거
    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        println("WebSocket connection closed: ${session.id}")
        println("before sessions" + sessions)
        sessions.remove(session)
        println("sessions" + sessions)
    }

    // 메시지를 브로드캐스트하는 메서드
    fun broadcast(message: String) {
        println("sessions" + sessions)
        sessions.forEach { session ->
            println("session.isOpen" + session.isOpen)
            if (session.isOpen) {
                session.sendMessage(TextMessage(message)) // 메시지 전송
            }
        }
    }
}
