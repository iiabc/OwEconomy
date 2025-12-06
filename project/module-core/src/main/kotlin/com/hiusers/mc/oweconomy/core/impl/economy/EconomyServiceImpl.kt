package com.hiusers.mc.oweconomy.core.impl.economy

import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyLangHelper

import com.hiusers.mc.oweconomy.core.event.BalanceChangeEvent
import com.hiusers.mc.oweconomy.core.event.BalanceLimitExceededEvent
import com.hiusers.mc.oweconomy.database.repo.AccountRepository
import com.hiusers.mc.oweconomy.database.repo.BalanceRepository
import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import taboolib.platform.util.bukkitPlugin
import java.math.BigDecimal
import java.util.*

/**
 * Economy 服务实现（VaultUnlockedAPI）
 * @author iiabc
 * @since 2025/1/1
 */
class EconomyServiceImpl : Economy {

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getName(): String {
        return "OwEconomy"
    }

    override fun hasSharedAccountSupport(): Boolean {
        return false  // 暂不支持共享账户
    }

    override fun hasMultiCurrencySupport(): Boolean {
        return true
    }

    override fun fractionalDigits(pluginName: String): Int {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return defaultCurrency.fractionalDigits
    }

    override fun fractionalDigits(pluginName: String, currency: String): Int {
        val currencyConfig = CurrencyConfigService.getCurrency(currency)
            ?: return fractionalDigits(pluginName)
        return currencyConfig.fractionalDigits
    }

    @Deprecated("Use format(String, BigDecimal) instead", ReplaceWith("format(pluginName, amount)"))
    override fun format(amount: BigDecimal): String {
        return format(bukkitPlugin.name, amount)
    }

    override fun format(pluginName: String, amount: BigDecimal): String {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return format(pluginName, amount, defaultCurrency.id)
    }

    @Deprecated("Use format(String, BigDecimal, String) instead", ReplaceWith("format(pluginName, amount, currency)"))
    override fun format(amount: BigDecimal, currency: String): String {
        return format(bukkitPlugin.name, amount, currency)
    }

    override fun format(pluginName: String, amount: BigDecimal, currency: String): String {
        val currencyConfig = CurrencyConfigService.getCurrency(currency)
            ?: return "${amount}${currency}"
        
        // 格式化金额，使用配置的小数位数
        val formattedAmount = if (currencyConfig.fractionalDigits < 0) {
            amount.toPlainString()
        } else {
            amount.setScale(currencyConfig.fractionalDigits, java.math.RoundingMode.DOWN).toPlainString()
        }
        
        // 使用控制台语言获取货币显示名称
        val name = if (amount == BigDecimal.ONE) {
            CurrencyLangHelper.getDisplayNameSingular(currency)
        } else {
            CurrencyLangHelper.getDisplayNamePlural(currency)
        }
        
        return "$formattedAmount $name"
    }

    override fun hasCurrency(currency: String): Boolean {
        return CurrencyConfigService.hasCurrency(currency)
    }

    override fun getDefaultCurrency(pluginName: String): String {
        return CurrencyConfigService.getDefaultCurrency().id
    }

    override fun defaultCurrencyNamePlural(pluginName: String): String {
        return CurrencyLangHelper.getDisplayNamePlural(CurrencyConfigService.getDefaultCurrency().id)
    }

    override fun defaultCurrencyNameSingular(pluginName: String): String {
        return CurrencyLangHelper.getDisplayNameSingular(CurrencyConfigService.getDefaultCurrency().id)
    }

    override fun currencies(): Collection<String> {
        return CurrencyConfigService.getCurrencyIds()
    }

    @Deprecated("Use createAccount(UUID, String, boolean) instead", ReplaceWith("createAccount(accountID, name, true)"))
    override fun createAccount(accountID: UUID, name: String): Boolean {
        return createAccount(accountID, name, true)
    }

    override fun createAccount(accountID: UUID, name: String, player: Boolean): Boolean {
        AccountRepository.upsertAccount(accountID, name, player)
        return true
    }

    @Deprecated("Use createAccount(UUID, String, String, boolean) instead", ReplaceWith("createAccount(accountID, name, worldName, true)"))
    override fun createAccount(accountID: UUID, name: String, worldName: String): Boolean {
        return createAccount(accountID, name, worldName, true)
    }

    override fun createAccount(accountID: UUID, name: String, worldName: String, player: Boolean): Boolean {
        // 不支持多世界，使用默认世界
        return createAccount(accountID, name, player)
    }

    override fun getUUIDNameMap(): Map<UUID, String> {
        return AccountRepository.getAllAccountUUIDs()
    }

    override fun getAccountName(accountID: UUID): Optional<String> {
        val name = AccountRepository.getAccountName(accountID)
        return Optional.ofNullable(name)
    }

    override fun hasAccount(accountID: UUID): Boolean {
        return AccountRepository.hasAccount(accountID)
    }

    override fun hasAccount(accountID: UUID, worldName: String): Boolean {
        // 不支持多世界
        return hasAccount(accountID)
    }

    override fun renameAccount(accountID: UUID, name: String): Boolean {
        return AccountRepository.renameAccount(accountID, name)
    }

    override fun renameAccount(plugin: String, accountID: UUID, name: String): Boolean {
        return renameAccount(accountID, name)
    }

    override fun deleteAccount(plugin: String, accountID: UUID): Boolean {
        return AccountRepository.deleteAccount(accountID)
    }

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String): Boolean {
        if (!CurrencyConfigService.hasCurrency(currency)) {
            return false
        }
        // 所有账户都支持所有货币
        return true
    }

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String, world: String): Boolean {
        return accountSupportsCurrency(plugin, accountID, currency)
    }

    @Deprecated("Use balance(String, UUID) instead", ReplaceWith("balance(pluginName, accountID)"))
    override fun getBalance(pluginName: String, accountID: UUID): BigDecimal {
        return balance(pluginName, accountID)
    }

    override fun balance(pluginName: String, accountID: UUID): BigDecimal {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return balance(pluginName, accountID, "", defaultCurrency.id)
    }

    @Deprecated("Use balance(String, UUID, String) instead", ReplaceWith("balance(pluginName, accountID, world)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        return balance(pluginName, accountID, world)
    }

    override fun balance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return balance(pluginName, accountID, world, defaultCurrency.id)
    }

    @Deprecated("Use balance(String, UUID, String, String) instead", ReplaceWith("balance(pluginName, accountID, world, currency)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        return balance(pluginName, accountID, world, currency)
    }

    override fun balance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        // 查询余额不应该负责创建账户，直接查询即可
        // 如果账户不存在，余额查询会返回 0
        return BalanceRepository.getBalance(accountID, currency)
    }
    
    /**
     * 确保账户存在，如果不存在则创建
     * 用于在修改余额的操作前确保账户已创建
     */
    private fun ensureAccountExists(accountID: UUID) {
        // 使用缓存快速检查，避免频繁查询数据库
        if (!AccountRepository.hasAccount(accountID)) {
            try {
                val offlinePlayer = Bukkit.getOfflinePlayer(accountID)
                val name = offlinePlayer.name ?: throw IllegalStateException("无法获取玩家名称: $accountID")
                AccountRepository.upsertAccount(accountID, name, true)
            } catch (e: Exception) {
                // 如果创建失败，可能是并发创建或其他原因，忽略即可
                // 后续操作会再次尝试或返回错误
            }
        }
    }

    override fun has(pluginName: String, accountID: UUID, amount: BigDecimal): Boolean {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return has(pluginName, accountID, "", defaultCurrency.id, amount)
    }

    override fun has(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): Boolean {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return has(pluginName, accountID, worldName, defaultCurrency.id, amount)
    }

    override fun has(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): Boolean {
        val balance = balance(pluginName, accountID, worldName, currency)
        return balance >= amount
    }

    override fun set(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return set(pluginName, accountID, "", defaultCurrency.id, amount)
    }

    override fun set(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return set(pluginName, accountID, worldName, defaultCurrency.id, amount)
    }

    override fun set(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        val currencyConfig = CurrencyConfigService.getCurrency(currency)
            ?: return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "货币不存在: $currency")

        // 确保账户存在
        ensureAccountExists(accountID)
        
        // 检查上限
        val currentBalance = balance(pluginName, accountID, worldName, currency)
        val actualAmount = if (currencyConfig.maxBalance == -1L) {
            amount
        } else {
            val maxBalanceDecimal = BigDecimal(currencyConfig.maxBalance)
            amount.coerceAtMost(maxBalanceDecimal).coerceAtLeast(BigDecimal.ZERO)
        }

        BalanceRepository.setBalance(accountID, currency, actualAmount)
        
        // 触发事件
        val event = BalanceChangeEvent(accountID, currency, actualAmount - currentBalance, actualAmount)
        event.call()

        return EconomyResponse(actualAmount - currentBalance, actualAmount, EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun withdraw(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return withdraw(pluginName, accountID, "", defaultCurrency.id, amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return withdraw(pluginName, accountID, worldName, defaultCurrency.id, amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        val currencyConfig = CurrencyConfigService.getCurrency(currency)
            ?: return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "货币不存在: $currency")

        // 确保账户存在
        ensureAccountExists(accountID)
        
        val currentBalance = balance(pluginName, accountID, worldName, currency)
        if (currentBalance < amount) {
            return EconomyResponse(BigDecimal.ZERO, currentBalance, EconomyResponse.ResponseType.FAILURE, "余额不足")
        }

        val newBalance = BalanceRepository.subtractBalance(accountID, currency, amount)
        
        // 触发事件
        val event = BalanceChangeEvent(accountID, currency, -amount, newBalance)
        event.call()

        return EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun deposit(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return deposit(pluginName, accountID, "", defaultCurrency.id, amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        return deposit(pluginName, accountID, worldName, defaultCurrency.id, amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        val currencyConfig = CurrencyConfigService.getCurrency(currency)
            ?: return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "货币不存在: $currency")

        // 确保账户存在
        ensureAccountExists(accountID)
        
        val currentBalance = balance(pluginName, accountID, worldName, currency)
        
        // 检查上限（系统奖励自动截取）
        val actualAmount = BalanceLimitService.calculateActualDepositAmount(
            currentBalance,
            amount,
            currencyConfig.maxBalance
        )

        if (actualAmount < amount && currencyConfig.maxBalance != -1L) {
            // 触发上限超出事件
            val event = BalanceLimitExceededEvent(accountID, currency, amount, currencyConfig.maxBalance)
            event.call()
        }

        val newBalance = BalanceRepository.addBalance(accountID, currency, actualAmount)
        
        // 触发事件
        val changeEvent = BalanceChangeEvent(accountID, currency, actualAmount, newBalance)
        changeEvent.call()

        return EconomyResponse(actualAmount, newBalance, EconomyResponse.ResponseType.SUCCESS, "")
    }

    // 共享账户相关方法（暂不支持）
    override fun createSharedAccount(pluginName: String, accountID: UUID, name: String, owner: UUID): Boolean {
        return false
    }

    @Deprecated("Use accountsWithOwnerOf(String, UUID) instead")
    override fun accountsOwnedBy(pluginName: String, accountID: UUID): List<String> {
        return emptyList()
    }

    @Deprecated("Use accountsWithMembershipTo(String, UUID) instead")
    override fun accountsMemberOf(pluginName: String, accountID: UUID): List<String> {
        return emptyList()
    }

    @Deprecated("Use accountsWithAccessTo(String, UUID, AccountPermission...) instead")
    override fun accountsAccessTo(pluginName: String, accountID: UUID, vararg permissions: AccountPermission): List<String> {
        return emptyList()
    }

    override fun isAccountOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return false
    }

    override fun setOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return false
    }

    override fun isAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return false
    }

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return false
    }

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID, vararg initialPermissions: AccountPermission): Boolean {
        return false
    }

    override fun removeAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return false
    }

    override fun hasAccountPermission(pluginName: String, accountID: UUID, uuid: UUID, permission: AccountPermission): Boolean {
        return false
    }

    override fun updateAccountPermission(pluginName: String, accountID: UUID, uuid: UUID, permission: AccountPermission, value: Boolean): Boolean {
        return false
    }
}

