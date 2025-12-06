package com.hiusers.mc.oweconomy.core.impl.economy

import com.hiusers.mc.oweconomy.core.impl.economy.CurrencyLangHelper
import net.milkbowl.vault.economy.Economy as LegacyEconomy
import net.milkbowl.vault2.economy.Economy as Vault2Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang
import taboolib.platform.util.bukkitPlugin

/**
 * Economy 服务注册器（Vault API）
 * 根据 VaultUnlockedAPI 文档，服务应在 onEnable() 阶段注册
 * @author iiabc
 * @since 2025/1/1
 */
object EconomyLoader {

    private lateinit var economyServiceImpl: EconomyServiceImpl
    private lateinit var economyLegacyAdapter: EconomyLegacyAdapter

    /**
     * 设置 EconomyServiceImpl 实例（由 APILoader 调用）
     */
    internal fun setEconomyService(service: EconomyServiceImpl) {
        economyServiceImpl = service
    }

    @Awake(LifeCycle.ENABLE)
    fun register() {
        // 确保 economyService 已初始化
        if (!::economyServiceImpl.isInitialized) {
            console().sendLang("console_error_service_not_initialized")
            return
        }

        // 在 ENABLE 阶段加载货币配置（此时 ConfigReader.config 已初始化）
        CurrencyConfigService.load()
        if (!CurrencyConfigService.isLoaded()) {
            console().sendLang("console_error_no_currencies")
            return
        }

        // 创建 Legacy Adapter
        economyLegacyAdapter = EconomyLegacyAdapter(economyServiceImpl)

        // 注册 VaultUnlockedAPI (vault2) - 使用 Higher 优先级确保被优先选择
        Bukkit.getServicesManager().register(
            Vault2Economy::class.java,
            economyServiceImpl,
            bukkitPlugin,
            ServicePriority.High
        )
        console().sendLang("console_success_vault2_registered")

        // 注册旧版 Vault API
        Bukkit.getServicesManager().register(
            LegacyEconomy::class.java,
            economyLegacyAdapter,
            bukkitPlugin,
            ServicePriority.High
        )
        console().sendLang("console_success_vault_legacy_registered")

        // 显示默认货币信息
        val defaultCurrency = CurrencyConfigService.getDefaultCurrency()
        val displayName = CurrencyLangHelper.getDisplayNamePlural(defaultCurrency.id)
        console().sendLang("console_info_default_currency", defaultCurrency.id, displayName)
        
        // 显示所有已配置的货币
        val allCurrencies = CurrencyConfigService.getCurrencyIds()
        console().sendLang("console_info_currencies_loaded", allCurrencies.joinToString(", "))
    }

    /**
     * 获取 EconomyServiceImpl 实例
     */
    fun getEconomyService(): EconomyServiceImpl {
        if (!::economyServiceImpl.isInitialized) {
            throw IllegalStateException("EconomyServiceImpl 尚未初始化，请等待插件完全加载")
        }
        return economyServiceImpl
    }
}

