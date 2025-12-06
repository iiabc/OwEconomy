package com.hiusers.mc.oweconomy.core.event

import taboolib.platform.type.BukkitProxyEvent

/**
 * 货币清零事件
 * @author iiabc
 * @since 2025/1/1
 */
data class CurrencyResetEvent(
    val currencyId: String,
    val accountCount: Int
) : BukkitProxyEvent()

