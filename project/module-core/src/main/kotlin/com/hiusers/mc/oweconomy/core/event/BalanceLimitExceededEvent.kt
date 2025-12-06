package com.hiusers.mc.oweconomy.core.event

import taboolib.platform.type.BukkitProxyEvent
import java.math.BigDecimal
import java.util.*

/**
 * 余额上限超出事件
 * @author iiabc
 * @since 2025/1/1
 */
data class BalanceLimitExceededEvent(
    val accountId: UUID,
    val currencyId: String,
    val attemptedAmount: BigDecimal,
    val maxBalance: Long
) : BukkitProxyEvent()

