package com.hiusers.mc.oweconomy.database.table

import com.hiusers.mc.oweconomy.api.config.reader.ConfigReader
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import java.math.BigDecimal

/**
 * 玩家余额表
 * @author iiabc
 * @since 2025/1/1
 */
object PlayerBalanceTable : LongIdTable("${ConfigReader.tableName}_balances") {
    val uuid = uuid("uuid").index()
    val currencyId = varchar("currency_id", 64).index()
    val balance = decimal("balance", 20, 4).default(BigDecimal.ZERO)
    val lastReset = long("last_reset").nullable()
}

