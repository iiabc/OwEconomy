package com.hiusers.mc.oweconomy.core.impl.economy

import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyLangHelper
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import taboolib.platform.util.bukkitPlugin
import java.math.BigDecimal
import java.util.*

/**
 * 旧版 Vault API 适配器
 * 所有操作都使用默认货币
 * @author iiabc
 * @since 2025/1/1
 */
class EconomyLegacyAdapter(
    private val economyService: EconomyServiceImpl
) : AbstractEconomy() {

    private val pluginName = bukkitPlugin.name

    override fun isEnabled(): Boolean {
        return economyService.isEnabled()
    }

    override fun getName(): String {
        return economyService.getName()
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return defaultCurrency.fractionalDigits
    }

    override fun format(amount: Double): String {
        return economyService.format(pluginName, BigDecimal.valueOf(amount))
    }

    override fun currencyNamePlural(): String {
        return CurrencyLangHelper.getDisplayNamePlural(CurrencyConfigService.getDefaultCurrency().id)
    }

    override fun currencyNameSingular(): String {
        return CurrencyLangHelper.getDisplayNameSingular(CurrencyConfigService.getDefaultCurrency().id)
    }

    override fun hasAccount(playerName: String): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.hasAccount(uuid)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return economyService.hasAccount(player.uniqueId)
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.hasAccount(uuid, worldName)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String): Boolean {
        return economyService.hasAccount(player.uniqueId, worldName)
    }

    override fun getBalance(playerName: String): Double {
        val uuid = getUUIDFromName(playerName) ?: return 0.0
        return economyService.balance(pluginName, uuid).toDouble()
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return economyService.balance(pluginName, player.uniqueId).toDouble()
    }

    override fun getBalance(playerName: String, world: String): Double {
        val uuid = getUUIDFromName(playerName) ?: return 0.0
        return economyService.balance(pluginName, uuid, world).toDouble()
    }

    override fun getBalance(player: OfflinePlayer, world: String): Double {
        return economyService.balance(pluginName, player.uniqueId, world).toDouble()
    }

    override fun has(playerName: String, amount: Double): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.has(pluginName, uuid, BigDecimal.valueOf(amount))
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return economyService.has(pluginName, player.uniqueId, BigDecimal.valueOf(amount))
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.has(pluginName, uuid, worldName, BigDecimal.valueOf(amount))
    }

    override fun has(player: OfflinePlayer, worldName: String, amount: Double): Boolean {
        return economyService.has(pluginName, player.uniqueId, worldName, BigDecimal.valueOf(amount))
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        val uuid = getUUIDFromName(playerName) ?: return EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "玩家不存在")
        val response = economyService.withdraw(pluginName, uuid, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val response = economyService.withdraw(pluginName, player.uniqueId, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        val uuid = getUUIDFromName(playerName) ?: return EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "玩家不存在")
        val response = economyService.withdraw(pluginName, uuid, worldName, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        val response = economyService.withdraw(pluginName, player.uniqueId, worldName, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        val uuid = getUUIDFromName(playerName) ?: return EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "玩家不存在")
        val response = economyService.deposit(pluginName, uuid, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val response = economyService.deposit(pluginName, player.uniqueId, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        val uuid = getUUIDFromName(playerName) ?: return EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "玩家不存在")
        val response = economyService.deposit(pluginName, uuid, worldName, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun depositPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse {
        val response = economyService.deposit(pluginName, player.uniqueId, worldName, BigDecimal.valueOf(amount))
        return convertResponse(response)
    }

    override fun createBank(name: String, player: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun createBank(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun deleteBank(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun bankBalance(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun isBankOwner(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun isBankMember(name: String, player: OfflinePlayer): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行")
    }

    override fun getBanks(): List<String> {
        return emptyList()
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.createAccount(uuid, playerName, true)
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        val name = player.name ?: return false
        return economyService.createAccount(player.uniqueId, name, true)
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        val uuid = getUUIDFromName(playerName) ?: return false
        return economyService.createAccount(uuid, playerName, worldName, true)
    }

    override fun createPlayerAccount(player: OfflinePlayer, worldName: String): Boolean {
        val name = player.name ?: return false
        return economyService.createAccount(player.uniqueId, name, worldName, true)
    }

    /**
     * 从玩家名获取 UUID
     */
    private fun getUUIDFromName(playerName: String): UUID? {
        // 尝试从账户表查找
        val uuidNameMap = economyService.getUUIDNameMap()
        return uuidNameMap.entries.firstOrNull { it.value.equals(playerName, ignoreCase = true) }?.key
            ?: run {
                // 如果找不到，尝试从 Bukkit 获取
                val offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName)
                if (offlinePlayer.hasPlayedBefore()) {
                    offlinePlayer.uniqueId
                } else {
                    null
                }
            }
    }

    /**
     * 转换 EconomyResponse
     */
    private fun convertResponse(response: net.milkbowl.vault2.economy.EconomyResponse): EconomyResponse {
        val type = when (response.type) {
            net.milkbowl.vault2.economy.EconomyResponse.ResponseType.SUCCESS -> EconomyResponse.ResponseType.SUCCESS
            net.milkbowl.vault2.economy.EconomyResponse.ResponseType.FAILURE -> EconomyResponse.ResponseType.FAILURE
            net.milkbowl.vault2.economy.EconomyResponse.ResponseType.NOT_IMPLEMENTED -> EconomyResponse.ResponseType.NOT_IMPLEMENTED
        }
        return EconomyResponse(
            response.amount.toDouble(),
            response.balance.toDouble(),
            type,
            response.errorMessage
        )
    }
}

