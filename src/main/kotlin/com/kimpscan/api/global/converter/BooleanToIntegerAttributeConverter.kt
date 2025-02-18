package com.kimpscan.api.global.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true) // 모든 Boolean 필드에 자동 적용
class BooleanToIntegerAttributeConverter : AttributeConverter<Boolean, Int> {

    override fun convertToDatabaseColumn(attribute: Boolean?): Int {
        return if (attribute == true) 1 else 0
    }

    override fun convertToEntityAttribute(dbData: Int?): Boolean {
        return dbData == 1
    }
}