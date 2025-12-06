package com.hiusers.mc.oweconomy.core.impl.api

import com.hiusers.mc.oweconomy.api.EconomyAPI
import com.hiusers.mc.oweconomy.core.impl.economy.BalanceLimitService
import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyConfigService
import com.hiusers.mc.oweconomy.core.impl.economy.EconomyServiceImpl
import net.milkbowl.vault2.economy.EconomyResponse
import taboolib.platform.util.bukkitPlugin
import java.math.BigDecimal
import java.util.*

/**
 * Economy API 实现
 * @author iiabc
 * @since 2025/1/1
 */
class EconomyAPIImpl(
    private val economyService: EconomyServiceImpl
) : EconomyAPI {

    private val pluginName = bukkitPlugin.name

    override fun transfer(from: UUID, to: UUID, currencyId: String, amount: BigDecimal): EconomyResponse {
        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
            ?: return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "货币不存在: $currencyId")

        // 检查发送方余额
        val fromBalance = economyService.balance(pluginName, from, "", currencyId)
        if (fromBalance < amount) {
            return EconomyResponse(BigDecimal.ZERO, fromBalance, EconomyResponse.ResponseType.FAILURE, "余额不足")
        }

        // 检查接收方余额上限（玩家交易时超出上限则失败）
        val toBalance = economyService.balance(pluginName, to, "", currencyId)
        if (!BalanceLimitService.checkTransactionLimit(toBalance, amount, currencyConfig.maxBalance)) {
            return EconomyResponse(
                BigDecimal.ZERO,
                toBalance,
                EconomyResponse.ResponseType.FAILURE,
                "接收方余额将超出上限（上限: ${currencyConfig.maxBalance}）"
            )
        }

        // 执行交易
        val withdrawResponse = economyService.withdraw(pluginName, from, "", currencyId, amount)
        if (withdrawResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            return withdrawResponse
        }

        val depositResponse = economyService.deposit(pluginName, to, "", currencyId, amount)
        if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
            // 如果存款失败，回滚取款
            economyService.deposit(pluginName, from, "", currencyId, amount)
            return depositResponse
        }

        return EconomyResponse(amount, depositResponse.balance, EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun reward(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse {
        // 系统奖励自动截取超出上限的部分
        return economyService.deposit(pluginName, accountId, "", currencyId, amount)
    }

    override fun deduct(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse {
        return economyService.withdraw(pluginName, accountId, "", currencyId, amount)
    }

    override fun getBalance(accountId: UUID, currencyId: String): BigDecimal {
        return economyService.balance(pluginName, accountId, "", currencyId)
    }

    override fun setBalance(accountId: UUID, currencyId: String, amount: BigDecimal): EconomyResponse {
        return economyService.set(pluginName, accountId, "", currencyId, amount)
    }
}
