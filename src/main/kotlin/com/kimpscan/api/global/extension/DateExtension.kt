package com.kimpscan.api.global.extension

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

// Date 의 확장 함수로 변환
fun Date.toIsoUtcString(): String {
    // Date 를 Instant 로 변환
    val instant = this.toInstant()

    // Instant 를 UTC 시간대의 ZonedDateTime 으로 변환
    val zonedDateTimeUtc = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

    // ZonedDateTime 을 ISO 8601 형식의 문자열로 변환
    return zonedDateTimeUtc.format(DateTimeFormatter.ISO_INSTANT)
}
