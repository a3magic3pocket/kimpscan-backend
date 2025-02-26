package com.kimpscan.api.global.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

@Configuration
class AppConfig {
    var containerId = UUID.randomUUID().toString()

    @PostConstruct
    private fun initContainerId() {
        try {
            // 'hostname' 명령어 실행을 위한 ProcessBuilder 사용
            val processBuilder = ProcessBuilder("hostname")
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val containerIdString = reader.readLine()  // hostname 명령어의 결과

            // hostname 값이 비어 있지 않으면 containerId 업데이트
            if (!containerIdString.isNullOrBlank()) {
                containerId = containerIdString
            }
        } catch (e: Exception) {
            // 예외 발생 시 아무 일도 일어나지 않음
            println("Failed to retrieve container ID. Using default value.")
            e.printStackTrace()
        }
    }

}