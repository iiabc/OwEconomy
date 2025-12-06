package com.hiusers.mc.oweconomy.core.impl.economy

import org.bukkit.command.CommandSender
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText

/**
 * 货币语言辅助类
 * 用于从语言文件获取货币显示名称
 * @author iiabc
 * @since 2025/1/1
 */
object CurrencyLangHelper {

    /**
     * 获取货币单数显示名称
     * @param sender 命令发送者（用于确定语言）
     * @param currencyId 货币ID
     * @return 货币单数显示名称
     */
    fun getDisplayNameSingular(sender: Any, currencyId: String): String {
        val langKey = "currency-display-singular-$currencyId"
        return when (sender) {
            is CommandSender -> {
                adaptCommandSender(sender).asLangText(langKey)
            }

            is ProxyCommandSender -> {
                sender.asLangText(langKey)
            }

            else -> {
                // 对于其他情况（如控制台），使用默认语言
                console().asLangText(langKey)
            }
        }
    }

    /**
     * 获取货币复数显示名称
     * @param sender 命令发送者（用于确定语言）
     * @param currencyId 货币ID
     * @return 货币复数显示名称
     */
    fun getDisplayNamePlural(sender: Any, currencyId: String): String {
        val langKey = "currency-display-plural-$currencyId"
        return when (sender) {
            is CommandSender -> {
                adaptCommandSender(sender).asLangText(langKey)
            }

            is ProxyCommandSender -> {
                sender.asLangText(langKey)
            }

            else -> {
                // 对于其他情况（如控制台），使用默认语言
                console().asLangText(langKey)
            }
        }
    }

    /**
     * 根据金额获取货币显示名称（单数或复数）
     * @param sender 命令发送者（用于确定语言）
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 货币显示名称
     */
    fun getDisplayName(sender: Any, currencyId: String, amount: java.math.BigDecimal): String {
        return if (amount == java.math.BigDecimal.ONE) {
            getDisplayNameSingular(sender, currencyId)
        } else {
            getDisplayNamePlural(sender, currencyId)
        }
    }

    /**
     * 获取货币单数显示名称（使用控制台语言）
     * @param currencyId 货币ID
     * @return 货币单数显示名称
     */
    fun getDisplayNameSingular(currencyId: String): String {
        val langKey = "currency-display-singular-$currencyId"
        return console().asLangText(langKey)
    }

    /**
     * 获取货币复数显示名称（使用控制台语言）
     * @param currencyId 货币ID
     * @return 货币复数显示名称
     */
    fun getDisplayNamePlural(currencyId: String): String {
        val langKey = "currency-display-plural-$currencyId"
        return console().asLangText(langKey)
    }
}
