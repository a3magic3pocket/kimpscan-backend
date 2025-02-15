package com.kimpscan.api.exchange.client

import com.kimpscan.api.exchange.dto.client.UsdWonExRateDto
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class ExRateClient(private val webClient: WebClient) {
    suspend fun getCurrentExRate(): Double {
        return try {
            val response = webClient.get()
                .uri("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()

            val result = Json.decodeFromString<UsdWonExRateDto>(response)

            return result.usd["krw"] ?: 0.0

        } catch (e: WebClientResponseException) {
            // HTTP 에러 처리 (예: 4xx, 5xx)
            println("HTTP Status: ${e.statusCode}, Response Body: ${e.responseBodyAsString}")
            0.0
        } catch (e: Exception) {
            // 그 외 일반적인 에러 처리
            println("Unexpected Error: ${e.message}")
            0.0
        }

    }
}