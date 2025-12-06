package com.hiusers.mc.oweconomy.core.impl.economy

import java.math.BigDecimal

/**
 * 余额上限服务
 * @author iiabc
 * @since 2025/1/1
 */
object BalanceLimitService {

    /**
     * 检查余额是否超出上限
     */
    fun checkBalanceLimit(currentBalance: BigDecimal, amount: BigDecimal, maxBalance: Long): Boolean {
        if (maxBalance == -1L) {
            return true  // 无上限
        }
        val newBalance = currentBalance + amount
        return newBalance <= BigDecimal(maxBalance)
    }

    /**
     * 计算实际可存入金额（自动截取）
     */
    fun calculateActualDepositAmount(currentBalance: BigDecimal, amount: BigDecimal, maxBalance: Long): BigDecimal {
        if (maxBalance == -1L) {
            return amount  // 无上限
        }
        val maxBalanceDecimal = BigDecimal(maxBalance)
        val availableSpace = maxBalanceDecimal - currentBalance
        return amount.coerceAtMost(availableSpace).coerceAtLeast(BigDecimal.ZERO)
    }

    /**
     * 检查玩家交易是否超出上限
     */
    fun checkTransactionLimit(receiverBalance: BigDecimal, amount: BigDecimal, maxBalance: Long): Boolean {
        return checkBalanceLimit(receiverBalance, amount, maxBalance)
    }
}

