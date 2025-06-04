package com.kimpscan.api.exchange.repository

import com.kimpscan.api.exchange.entity.BinanceSymbolStatus
import com.kimpscan.api.exchange.entity.QSymbolInfo
import com.kimpscan.api.exchange.entity.SymbolInfo
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class SymbolInfoQuerydslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : SymbolInfoQuerydslRepository {

    override fun searchSymbolInfo(keyword: String?, isStatusTrading: Boolean, limit: Long): List<SymbolInfo> {
        val symbolInfo = QSymbolInfo.symbolInfo

        val builder = BooleanBuilder()
        if (keyword != null) {
            builder.and(
                symbolInfo.korName.contains(keyword)
                    .or(symbolInfo.symbol.contains(keyword))
            )
        }
        if (isStatusTrading) {
            builder.and(symbolInfo.binanceSymbolStatus.eq(BinanceSymbolStatus.TRADING.toString()))
        }


        return jpaQueryFactory.select(
            symbolInfo
        )
            .from(symbolInfo)
            .where(builder)
            .offset(0)
            .limit(limit)
            .fetch()
    }

}
