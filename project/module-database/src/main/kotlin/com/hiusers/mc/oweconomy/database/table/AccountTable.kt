package com.hiusers.mc.oweconomy.database.table

import com.hiusers.mc.oweconomy.api.config.reader.ConfigReader
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

/**
 * 账户表
 * @author iiabc
 * @since 2025/1/1
 */
object AccountTable : UUIDTable("${ConfigReader.tableName}_accounts") {
    val name = varchar("name", 32)
    val isPlayer = bool("is_player").default(true)
}

