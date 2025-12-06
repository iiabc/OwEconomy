package com.hiusers.mc.oweconomy.core.hook

import com.hiusers.mc.oweconomy.OwEconomy
import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyConfigService
import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyLangHelper
import com.hiusers.mc.oweconomy.core.impl.economy.EconomyLoader
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion
import java.math.BigDecimal

/**
 * PlaceholderAPI 扩展
 * @author iiabc
 * @since 2025/1/1
 */
object HookPlaceholderAPI : PlaceholderExpansion {

    override val identifier = "oweconomy"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return ""

        val parts = args.split("_", limit = 2)
        if (parts.isEmpty()) return ""

        val economyAPI = OwEconomy.api()
        val economyService = EconomyLoader.getEconomyService()
        val pluginName = "OwEconomy"

        when (parts[0].lowercase()) {
            "balance" -> {
                // %oweconomy_balance_<currency>% - 获取指定货币余额
                if (parts.size < 2) {
                    // 如果没有指定货币，使用默认货币
                    val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
                    val balance = economyAPI.getBalance(player.uniqueId, defaultCurrency.id)
                    return formatBalance(balance, defaultCurrency.fractionalDigits)
                }
                val currencyId = parts[1]
                if (!CurrencyConfigService.hasCurrency(currencyId)) {
                    return "货币不存在"
                }
                val balance = economyAPI.getBalance(player.uniqueId, currencyId)
                val currencyConfig = CurrencyConfigService.getCurrency(currencyId)!!
                return formatBalance(balance, currencyConfig.fractionalDigits)
            }
            "balanceformatted" -> {
                // %oweconomy_balanceformatted_<currency>% - 获取格式化的余额（带货币名称）
                if (parts.size < 2) {
                    val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
                    val balance = economyAPI.getBalance(player.uniqueId, defaultCurrency.id)
                    return economyService.format(pluginName, balance, defaultCurrency.id)
                }
                val currencyId = parts[1]
                if (!CurrencyConfigService.hasCurrency(currencyId)) {
                    return "货币不存在"
                }
                val balance = economyAPI.getBalance(player.uniqueId, currencyId)
                return economyService.format(pluginName, balance, currencyId)
            }
            "maxbalance" -> {
                // %oweconomy_maxbalance_<currency>% - 获取货币上限
                if (parts.size < 2) {
                    val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
                    if (defaultCurrency.maxBalance == -1L) {
                        return "∞"
                    }
                    return formatBalance(BigDecimal(defaultCurrency.maxBalance), defaultCurrency.fractionalDigits)
                }
                val currencyId = parts[1]
                val currencyConfig = CurrencyConfigService.getCurrency(currencyId) ?: return "货币不存在"
                if (currencyConfig.maxBalance == -1L) {
                    return "∞"
                }
                return formatBalance(BigDecimal(currencyConfig.maxBalance), currencyConfig.fractionalDigits)
            }
            "currencyname" -> {
                // %oweconomy_currencyname_<currency>% - 获取货币名称（单数）
                if (parts.size < 2) {
                    return CurrencyLangHelper.getDisplayNameSingular(player, CurrencyConfigService.getDefaultCurrency().id)
                }
                val currencyId = parts[1]
                if (!CurrencyConfigService.hasCurrency(currencyId)) {
                    return "货币不存在"
                }
                return CurrencyLangHelper.getDisplayNameSingular(player, currencyId)
            }
            "currencynameplural" -> {
                // %oweconomy_currencynameplural_<currency>% - 获取货币名称（复数）
                if (parts.size < 2) {
                    return CurrencyLangHelper.getDisplayNamePlural(player, CurrencyConfigService.getDefaultCurrency().id)
                }
                val currencyId = parts[1]
                if (!CurrencyConfigService.hasCurrency(currencyId)) {
                    return "货币不存在"
                }
                return CurrencyLangHelper.getDisplayNamePlural(player, currencyId)
            }
            else -> return ""
        }
    }

    /**
     * 格式化余额
     */
    private fun formatBalance(balance: BigDecimal, fractionalDigits: Int): String {
        return if (fractionalDigits < 0) {
            balance.toPlainString()
        } else {
            balance.setScale(fractionalDigits, java.math.RoundingMode.DOWN).toPlainString()
        }
    }
}
