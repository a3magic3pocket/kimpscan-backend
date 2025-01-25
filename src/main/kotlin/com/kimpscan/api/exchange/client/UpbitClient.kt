package com.kimpscan.api.exchange.client

import com.kimpscan.api.exchange.dto.UpbitTickerApiResDto
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException


@Component
class UpbitClient(private val webClient: WebClient) {

    suspend fun getTickers(): List<UpbitTickerApiResDto> {
        return try {
            val response = webClient.get()
                .uri("https://api.upbit.com/v1/ticker/all?quote_currencies=KRW")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()

            return Json.decodeFromString<List<UpbitTickerApiResDto>>(response)

        } catch (e: WebClientResponseException) {
            // HTTP 에러 처리 (예: 4xx, 5xx)
            println("HTTP Status: ${e.statusCode}, Response Body: ${e.responseBodyAsString}")
            listOf()
        } catch (e: Exception) {
            // 그 외 일반적인 에러 처리
            println("Unexpected Error: ${e.message}")
            listOf()
        }

    }

}