package com.hiusers.mc.oweconomy.core.impl.economy

/**
 * 货币配置数据类
 * @author iiabc
 * @since 2025/1/1
 */
data class CurrencyConfig(
    val id: String,  // yaml key
    val default: Boolean = false,  // 是否为默认货币
    val maxBalance: Long,  // -1 表示无限
    val fractionalDigits: Int,
    val resetCycle: String?  // null 表示永不清零
)

