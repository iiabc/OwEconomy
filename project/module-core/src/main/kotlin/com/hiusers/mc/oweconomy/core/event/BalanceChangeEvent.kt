package com.hiusers.mc.oweconomy.core.event

import taboolib.platform.type.BukkitProxyEvent
import java.math.BigDecimal
import java.util.*

/**
 * 余额变更事件
 * @author iiabc
 * @since 2025/1/1
 */
data class BalanceChangeEvent(
    val accountId: UUID,
    val currencyId: String,
    val amount: BigDecimal,
    val newBalance: BigDecimal
) : BukkitProxyEvent()

