package com.hiusers.mc.oweconomy.command

import com.hiusers.mc.oweconomy.OwEconomy
import com.hiusers.mc.oweconomy.api.EconomyAPI
import com.hiusers.mc.oweconomy.api.config.reader.ConfigReader
import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyConfigService
import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyLangHelper
import com.hiusers.mc.oweconomy.core.impl.economy.EconomyLoader
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptPlayer
import taboolib.expansion.createHelper
import taboolib.module.lang.Language
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.math.BigDecimal

@CommandHeader(name = "OwEconomy", aliases = ["oe", "oweco"], permission = "oweconomy.command")
object Command {

    /**
     * 安全获取 EconomyAPI，如果未初始化则返回 null
     */
    private fun getEconomyAPIOrNull(): EconomyAPI? {
        return try {
            OwEconomy.api()
        } catch (e: IllegalStateException) {
            null
        }
    }

    /**
     * 检查并获取 EconomyAPI，如果未初始化则发送错误消息并返回 null
     */
    private fun ProxyCommandSender.getEconomyAPIOrError(): EconomyAPI? {
        val api = getEconomyAPIOrNull()
        if (api == null) {
            sendLang("error_economy_not_loaded")
        }
        return api
    }

    /**
     * 检查并获取 EconomyAPI，如果未初始化则发送错误消息并返回 null（Player 版本）
     */
    private fun Player.getEconomyAPIOrError(): EconomyAPI? {
        val api = getEconomyAPIOrNull()
        if (api == null) {
            sendLang("error_economy_not_loaded")
        }
        return api
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    // /oe reload - 重载配置
    @CommandBody(permission = "oweconomy.command.admin.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            CurrencyConfigService.reload()
            Language.reload()
            sender.sendLang("command_reload_success")
        }
    }

    // /oe pay <player> <amount> [currency]
    @CommandBody
    val pay = subCommand {
        dynamic("player") {
            suggestion<Player> { sender, _ ->
                Bukkit.getOnlinePlayers().map { it.name }.filter { it != sender.name }
            }
            dynamic("amount") {
                execute<Player> { sender, context, _ ->
                    val targetName = context["player"]
                    val amountStr = context["amount"]
                    val currencyId = CurrencyConfigService.getDefaultCurrency().id

                    val amount = try {
                        BigDecimal(amountStr)
                    } catch (e: Exception) {
                        sender.sendLang("error_invalid_amount", amountStr)
                        return@execute
                    }

                    val target = Bukkit.getPlayer(targetName)
                    if (target == null) {
                        sender.sendLang("error_player_not_online", targetName)
                        return@execute
                    }

                    if (target == sender) {
                        sender.sendLang("error_cannot_pay_self")
                        return@execute
                    }

                    if (amount <= BigDecimal.ZERO) {
                        sender.sendLang("error_amount_must_positive")
                        return@execute
                    }

                    val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                    if (currencyConfig == null) {
                        sender.sendLang("error_currency_not_found", currencyId)
                        return@execute
                    }

                    val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                    val economyService = EconomyLoader.getEconomyService()
                    val response = economyAPI.transfer(sender.uniqueId, target.uniqueId, currencyId, amount)

                    when (response.type) {
                        EconomyResponse.ResponseType.SUCCESS -> {
                            val formatted = economyService.format("OwEconomy", amount, currencyId)
                            sender.sendLang(
                                "transfer_success_sender",
                                target.name ?: target.uniqueId.toString(),
                                formatted
                            )
                            target.sendLang("transfer_success_receiver", sender.name, formatted)
                        }

                        EconomyResponse.ResponseType.FAILURE -> {
                            sender.sendLang("transfer_failed", response.errorMessage ?: "Unknown error")
                        }

                        else -> {
                            sender.sendLang("transfer_failed", "error_unknown")
                        }
                    }
                }
                dynamic("currency") {
                    suggestion<ProxyCommandSender> { _, _ ->
                        CurrencyConfigService.getCurrencyIds().toList()
                    }
                    execute<Player> { sender, context, _ ->
                        val targetName = context["player"]
                        val amountStr = context["amount"]
                        val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id

                        val amount = try {
                            BigDecimal(amountStr)
                        } catch (e: Exception) {
                            sender.sendLang("error_invalid_amount", amountStr)
                            return@execute
                        }

                        val target = Bukkit.getPlayer(targetName)
                        if (target == null) {
                            sender.sendLang("error_player_not_online", targetName)
                            return@execute
                        }

                        if (target == sender) {
                            sender.sendLang("error_cannot_pay_self")
                            return@execute
                        }

                        if (amount <= BigDecimal.ZERO) {
                            sender.sendLang("error_amount_must_positive")
                            return@execute
                        }

                        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                        if (currencyConfig == null) {
                            sender.sendLang("error_currency_not_found", currencyId)
                            return@execute
                        }

                        val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                        val economyService = EconomyLoader.getEconomyService()
                        val response = economyAPI.transfer(sender.uniqueId, target.uniqueId, currencyId, amount)

                        when (response.type) {
                            EconomyResponse.ResponseType.SUCCESS -> {
                                val formatted = economyService.format("OwEconomy", amount, currencyId)
                                sender.sendLang(
                                    "transfer_success_sender",
                                    target.name ?: target.uniqueId.toString(),
                                    formatted
                                )
                                target.sendLang("transfer_success_receiver", sender.name, formatted)
                            }

                            EconomyResponse.ResponseType.FAILURE -> {
                                sender.sendLang("transfer_failed", response.errorMessage ?: "Unknown error")
                            }

                            else -> {
                                sender.sendLang("transfer_failed", "error_unknown")
                            }
                        }
                    }
                }
            }
        }
    }

    // /oe balance [player] [currency]
    @CommandBody
    val balance = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic("currency") {
                suggestion<ProxyCommandSender> { _, _ ->
                    CurrencyConfigService.getCurrencyIds().toList()
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val playerName = context["player"]
                    val target = Bukkit.getOfflinePlayer(playerName)
                    val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id
                    sender.showBalance(target, currencyId)
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                val playerName = context["player"]
                val target = Bukkit.getOfflinePlayer(playerName)
                val currencyId = CurrencyConfigService.getDefaultCurrency().id
                sender.showBalance(target, currencyId)
            }
        }
        dynamic("currency") {
            suggestion<Player> { _, _ ->
                CurrencyConfigService.getCurrencyIds().toList()
            }
            execute<Player> { sender, context, _ ->
                val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id
                adaptPlayer(sender).showBalance(sender, currencyId)
            }
        }
        execute<Player> { sender, _, _ ->
            val currencyId = CurrencyConfigService.getDefaultCurrency().id
            adaptPlayer(sender).showBalance(sender, currencyId)
        }
    }

    /**
     * 格式化余额（只显示数字，不包含货币名称）
     */
    private fun formatBalanceAmount(balance: BigDecimal, fractionalDigits: Int): String {
        return if (fractionalDigits < 0) {
            balance.toPlainString()
        } else {
            balance.setScale(fractionalDigits, java.math.RoundingMode.DOWN).toPlainString()
        }
    }

    /**
     * 显示余额的辅助函数
     */
    private fun ProxyCommandSender.showBalance(target: org.bukkit.OfflinePlayer, currencyId: String) {
        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
        if (currencyConfig == null) {
            sendLang("error_currency_not_found", currencyId)
            return
        }

        val economyAPI = getEconomyAPIOrError() ?: return
        val balance = economyAPI.getBalance(target.uniqueId, currencyId)
        val formattedAmount = formatBalanceAmount(balance, currencyConfig.fractionalDigits)
        val displayName = CurrencyLangHelper.getDisplayNamePlural(this, currencyId)

        val isSelf = when (this) {
            is Player -> target.uniqueId == this.uniqueId
            is taboolib.common.platform.ProxyPlayer -> target.uniqueId == this.uniqueId
            else -> false
        }

        if (currencyConfig.maxBalance != -1L) {
            val maxFormattedAmount = formatBalanceAmount(BigDecimal(currencyConfig.maxBalance), currencyConfig.fractionalDigits)
            if (isSelf) {
                sendLang("balance_show_with_limit", displayName, formattedAmount, maxFormattedAmount)
            } else {
                sendLang(
                    "balance_show_other_with_limit",
                    target.name ?: target.uniqueId.toString(),
                    displayName,
                    formattedAmount,
                    maxFormattedAmount
                )
            }
        } else {
            if (isSelf) {
                sendLang("balance_show", displayName, formattedAmount)
            } else {
                sendLang("balance_show_other", target.name ?: target.uniqueId.toString(), displayName, formattedAmount)
            }
        }
    }

    // /oe currencies - 列出所有货币
    @CommandBody
    val currencies = subCommand {
        execute<Player> { sender, _, _ ->
            val currencies = CurrencyConfigService.getAllCurrencies()
            if (currencies.isEmpty()) {
                sender.sendLang("console_error_no_currencies")
                return@execute
            }
            val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
            val economyService = EconomyLoader.getEconomyService()
            sender.sendLang("balance_currencies_title")
            currencies.values.forEach { config ->
                val balance = economyAPI.getBalance(sender.uniqueId, config.id)
                val formatted = economyService.format("OwEconomy", balance, config.id)
                sender.sendLang("balance_currencies_item", config.id, formatted)
            }
        }
    }

    // /oe give <player> <amount> [currency] - 系统奖励（自动截取超出上限）
    @CommandBody(permission = "oweconomy.command.admin.give")
    val give = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic("amount") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val playerName = context["player"]
                    val target = Bukkit.getOfflinePlayer(playerName)
                    val amountStr = context["amount"]
                    val currencyId = CurrencyConfigService.getDefaultCurrency().id

                    val amount = try {
                        BigDecimal(amountStr)
                    } catch (e: Exception) {
                        sender.sendLang("error_invalid_amount", amountStr)
                        return@execute
                    }

                    if (amount <= BigDecimal.ZERO) {
                        sender.sendLang("error_amount_must_positive")
                        return@execute
                    }

                    val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                    if (currencyConfig == null) {
                        sender.sendLang("error_currency_not_found", currencyId)
                        return@execute
                    }

                    val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                    val economyService = EconomyLoader.getEconomyService()
                    val response = economyAPI.reward(target.uniqueId, currencyId, amount)

                    when (response.type) {
                        EconomyResponse.ResponseType.SUCCESS -> {
                            if (!ConfigReader.hideMessage) {
                                val formatted = economyService.format("OwEconomy", response.amount, currencyId)
                                sender.sendLang("reward_success", target.name ?: target.uniqueId.toString(), formatted)
                                if (response.amount < amount) {
                                    sender.sendLang("reward_truncated", formatted)
                                }
                            }
                        }

                        EconomyResponse.ResponseType.FAILURE -> {
                            sender.sendLang("reward_failed", response.errorMessage ?: "Unknown error")
                        }

                        else -> {
                            sender.sendLang("reward_failed", "error_unknown")
                        }
                    }
                }
                dynamic("currency") {
                    suggestion<ProxyCommandSender> { _, _ ->
                        CurrencyConfigService.getCurrencyIds().toList()
                    }
                    execute<ProxyCommandSender> { sender, context, _ ->
                        val playerName = context["player"]
                        val target = Bukkit.getOfflinePlayer(playerName)
                        val amountStr = context["amount"]
                        val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id

                        val amount = try {
                            BigDecimal(amountStr)
                        } catch (e: Exception) {
                            sender.sendLang("error_invalid_amount", amountStr)
                            return@execute
                        }

                        if (amount <= BigDecimal.ZERO) {
                            sender.sendLang("error_amount_must_positive")
                            return@execute
                        }

                        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                        if (currencyConfig == null) {
                            sender.sendLang("error_currency_not_found", currencyId)
                            return@execute
                        }

                        val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                        val economyService = EconomyLoader.getEconomyService()
                        val response = economyAPI.reward(target.uniqueId, currencyId, amount)

                        when (response.type) {
                            EconomyResponse.ResponseType.SUCCESS -> {
                                if (!ConfigReader.hideMessage) {
                                    val formatted = economyService.format("OwEconomy", response.amount, currencyId)
                                    sender.sendLang("reward_success", target.name ?: target.uniqueId.toString(), formatted)
                                    if (response.amount < amount) {
                                        sender.sendLang("reward_truncated", formatted)
                                    }
                                }
                            }

                            EconomyResponse.ResponseType.FAILURE -> {
                                sender.sendLang("reward_failed", response.errorMessage ?: "Unknown error")
                            }

                            else -> {
                                sender.sendLang("reward_failed", "error_unknown")
                            }
                        }
                    }
                }
            }
        }
    }

    // /oe take <player> <amount> [currency] - 系统扣除
    @CommandBody(permission = "oweconomy.command.admin.take")
    val take = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic("amount") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val playerName = context["player"]
                    val target = Bukkit.getOfflinePlayer(playerName)
                    val amountStr = context["amount"]
                    val currencyId = CurrencyConfigService.getDefaultCurrency().id

                    val amount = try {
                        BigDecimal(amountStr)
                    } catch (e: Exception) {
                        sender.sendLang("error_invalid_amount", amountStr)
                        return@execute
                    }

                    if (amount <= BigDecimal.ZERO) {
                        sender.sendLang("error_amount_must_positive")
                        return@execute
                    }

                    val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                    if (currencyConfig == null) {
                        sender.sendLang("error_currency_not_found", currencyId)
                        return@execute
                    }

                    val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                    val economyService = EconomyLoader.getEconomyService()
                    val response = economyAPI.deduct(target.uniqueId, currencyId, amount)

                    when (response.type) {
                        EconomyResponse.ResponseType.SUCCESS -> {
                            if (!ConfigReader.hideMessage) {
                                val formatted = economyService.format("OwEconomy", amount, currencyId)
                                sender.sendLang("deduct_success", target.name ?: target.uniqueId.toString(), formatted)
                            }
                        }

                        EconomyResponse.ResponseType.FAILURE -> {
                            sender.sendLang("deduct_failed", response.errorMessage ?: "Unknown error")
                        }

                        else -> {
                            sender.sendLang("deduct_failed", "error_unknown")
                        }
                    }
                }
                dynamic("currency") {
                    suggestion<ProxyCommandSender> { _, _ ->
                        CurrencyConfigService.getCurrencyIds().toList()
                    }
                    execute<ProxyCommandSender> { sender, context, _ ->
                        val playerName = context["player"]
                        val target = Bukkit.getOfflinePlayer(playerName)
                        val amountStr = context["amount"]
                        val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id

                        val amount = try {
                            BigDecimal(amountStr)
                        } catch (e: Exception) {
                            sender.sendLang("error_invalid_amount", amountStr)
                            return@execute
                        }

                        if (amount <= BigDecimal.ZERO) {
                            sender.sendLang("error_amount_must_positive")
                            return@execute
                        }

                        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                        if (currencyConfig == null) {
                            sender.sendLang("error_currency_not_found", currencyId)
                            return@execute
                        }

                        val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                        val economyService = EconomyLoader.getEconomyService()
                        val response = economyAPI.deduct(target.uniqueId, currencyId, amount)

                        when (response.type) {
                            EconomyResponse.ResponseType.SUCCESS -> {
                                if (!ConfigReader.hideMessage) {
                                    val formatted = economyService.format("OwEconomy", amount, currencyId)
                                    sender.sendLang("deduct_success", target.name ?: target.uniqueId.toString(), formatted)
                                }
                            }

                            EconomyResponse.ResponseType.FAILURE -> {
                                sender.sendLang("deduct_failed", response.errorMessage ?: "Unknown error")
                            }

                            else -> {
                                sender.sendLang("deduct_failed", "error_unknown")
                            }
                        }
                    }
                }
            }
        }
    }

    // /oe set <player> <amount> [currency] - 设置余额
    @CommandBody(permission = "oweconomy.command.admin.set")
    val set = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic("amount") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val playerName = context["player"]
                    val target = Bukkit.getOfflinePlayer(playerName)
                    val amountStr = context["amount"]
                    val currencyId = CurrencyConfigService.getDefaultCurrency().id

                    val amount = try {
                        BigDecimal(amountStr)
                    } catch (e: Exception) {
                        sender.sendLang("error_invalid_amount", amountStr)
                        return@execute
                    }

                    if (amount < BigDecimal.ZERO) {
                        sender.sendLang("error_amount_must_non_negative")
                        return@execute
                    }

                    val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                    if (currencyConfig == null) {
                        sender.sendLang("error_currency_not_found", currencyId)
                        return@execute
                    }

                    val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                    val economyService = EconomyLoader.getEconomyService()
                    val response = economyAPI.setBalance(target.uniqueId, currencyId, amount)

                    when (response.type) {
                        EconomyResponse.ResponseType.SUCCESS -> {
                            val formatted = economyService.format("OwEconomy", amount, currencyId)
                            sender.sendLang("set_success", target.name ?: target.uniqueId.toString(), formatted)
                        }

                        EconomyResponse.ResponseType.FAILURE -> {
                            sender.sendLang("set_failed", response.errorMessage ?: "Unknown error")
                        }

                        else -> {
                            sender.sendLang("set_failed", "error_unknown")
                        }
                    }
                }
                dynamic("currency") {
                    suggestion<ProxyCommandSender> { _, _ ->
                        CurrencyConfigService.getCurrencyIds().toList()
                    }
                    execute<ProxyCommandSender> { sender, context, _ ->
                        val playerName = context["player"]
                        val target = Bukkit.getOfflinePlayer(playerName)
                        val amountStr = context["amount"]
                        val currencyId = context["currency"] ?: CurrencyConfigService.getDefaultCurrency().id

                        val amount = try {
                            BigDecimal(amountStr)
                        } catch (e: Exception) {
                            sender.sendLang("error_invalid_amount", amountStr)
                            return@execute
                        }

                        if (amount < BigDecimal.ZERO) {
                            sender.sendLang("error_amount_must_non_negative")
                            return@execute
                        }

                        val currencyConfig = CurrencyConfigService.getCurrency(currencyId)
                        if (currencyConfig == null) {
                            sender.sendLang("error_currency_not_found", currencyId)
                            return@execute
                        }

                        val economyAPI = sender.getEconomyAPIOrError() ?: return@execute
                        val economyService = EconomyLoader.getEconomyService()
                        val response = economyAPI.setBalance(target.uniqueId, currencyId, amount)

                        when (response.type) {
                            EconomyResponse.ResponseType.SUCCESS -> {
                                val formatted = economyService.format("OwEconomy", amount, currencyId)
                                sender.sendLang("set_success", target.name ?: target.uniqueId.toString(), formatted)
                            }

                            EconomyResponse.ResponseType.FAILURE -> {
                                sender.sendLang("set_failed", response.errorMessage ?: "Unknown error")
                            }

                            else -> {
                                sender.sendLang("set_failed", "error_unknown")
                            }
                        }
                    }
                }
            }
        }
    }

}