package com.kimpscan.api.exchange.client

import com.kimpscan.api.exchange.dto.client.Binance24hTickerApiResDto
import com.kimpscan.api.exchange.dto.client.BinanceTickerApiResDto
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Component
class BinanceClient(private val webClient: WebClient) {
    suspend fun getTickers(): List<BinanceTickerApiResDto> {
        return try {
            val response = webClient.get()
                .uri("https://api.binance.com/api/v3/ticker/price")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()

            return Json.decodeFromString<List<BinanceTickerApiResDto>>(response)

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

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun get24hTickers(): List<Binance24hTickerApiResDto> {
        return try {
            val responseFlux = webClient.get()
                .uri("https://api.binance.com/api/v3/ticker/24hr?type=MINI")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(DataBuffer::class.java)

            // 바이트 배열을 임시로 저장할 ByteArrayOutputStream
            val byteArrayOutputStream = ByteArrayOutputStream()

            responseFlux.collect { dataBuffer ->
                val byteArrayBuffer = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(byteArrayBuffer)
                byteArrayOutputStream.write(byteArrayBuffer) // ByteArrayOutputStream 에 기록
            }

            // ByteArrayOutputStream 에서 바이트 배열로 변환
            val byteArray = byteArrayOutputStream.toByteArray()

            // 바이트 배열을 ByteArrayInputStream 으로 변환하여 Json 파싱
            val byteArrayInputStream = ByteArrayInputStream(byteArray)

            return Json.decodeFromStream<List<Binance24hTickerApiResDto>>(byteArrayInputStream)

        } catch (e: WebClientResponseException) {
            e.printStackTrace()
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