package com.hiusers.mc.oweconomy.api

import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*

/**
 * Economy API 接口
 * 提供玩家交易、系统奖励/扣除等功能
 * @author iiabc
 * @since 2025/1/1
 */
interface EconomyAPI {

    /**
     * 玩家之间交易
     * @param from 发送方玩家UUID
     * @param to 接收方玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 交易结果
     */
    fun transfer(from: UUID, to: UUID, currencyId: String, amount: BigDecimal): EconomyResponse

    /**
     * 系统奖励玩家（自动截取超出上限的部分）
     * @param accountId 账户UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 交易结果（实际奖励的金额可能小于请求的金额）
     */
    fun reward(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse

    /**
     * 系统扣除玩家余额
     * @param accountId 账户UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 交易结果
     */
    fun deduct(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse

    /**
     * 获取玩家余额
     * @param accountId 账户UUID
     * @param currencyId 货币ID
     * @return 余额
     */
    fun getBalance(accountId: UUID, currencyId: String): BigDecimal

    /**
     * 设置玩家余额
     * @param accountId 账户UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 交易结果
     */
    fun setBalance(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse
}
