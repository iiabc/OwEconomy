package com.hiusers.mc.oweconomy.core.impl.economy

import com.hiusers.mc.oweconomy.api.config.reader.ConfigReader
import taboolib.common.platform.function.console
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang

/**
 * 货币配置服务
 * 从 yaml 加载货币配置到内存
 * @author iiabc
 * @since 2025/1/1
 */
object CurrencyConfigService {

    private val currencyConfigs: MutableMap<String, CurrencyConfig> = mutableMapOf()
    
    /**
     * 是否已加载配置
     */
    private var loaded: Boolean = false

    /**
     * 加载配置
     */
    fun load() {
        reload()
    }

    /**
     * 重新加载配置
     */
    fun reload() {
        // 检查 ConfigReader.config 是否已初始化
        try {
            val test = ConfigReader.config
        } catch (e: kotlin.UninitializedPropertyAccessException) {
            console().sendLang("console_warn_config_not_initialized")
            loaded = false
            return
        }
        
        currencyConfigs.clear()
        val currenciesSection = ConfigReader.config.getConfigurationSection("currencies")
            ?: run {
                console().sendLang("console_warn_no_currencies_section")
                loaded = false
                return
            }

        currenciesSection.getKeys(false).forEach { currencyId ->
            val currencySection = currenciesSection.getConfigurationSection(currencyId)
                ?: return@forEach

            val config = CurrencyConfig(
                id = currencyId,
                default = currencySection.getBoolean("default", false),
                maxBalance = currencySection.getLong("max-balance", -1),
                fractionalDigits = currencySection.getInt("fractional-digits", 2),
                resetCycle = currencySection.getString("reset-cycle")?.takeIf { it.isNotBlank() }
            )

            currencyConfigs[currencyId] = config
        }

        // 验证默认货币配置
        validateDefaultCurrency()
        
        loaded = true
        console().sendLang("console_info_currencies_count", currencyConfigs.size.toString())
    }
    
    /**
     * 检查配置是否已加载
     */
    fun isLoaded(): Boolean {
        return loaded && currencyConfigs.isNotEmpty()
    }

    /**
     * 获取所有货币配置
     */
    fun getAllCurrencies(): Map<String, CurrencyConfig> {
        return currencyConfigs.toMap()
    }

    /**
     * 获取货币配置
     */
    fun getCurrency(currencyId: String): CurrencyConfig? {
        return currencyConfigs[currencyId]
    }

    /**
     * 获取默认货币
     */
    fun getDefaultCurrency(): CurrencyConfig {
        return currencyConfigs.values.firstOrNull { it.default }
            ?: currencyConfigs.values.firstOrNull()
            ?: throw IllegalStateException("没有配置任何货币")
    }

    /**
     * 检查货币是否存在
     */
    fun hasCurrency(currencyId: String): Boolean {
        return currencyConfigs.containsKey(currencyId)
    }

    /**
     * 获取所有货币ID
     */
    fun getCurrencyIds(): Collection<String> {
        return currencyConfigs.keys
    }

    /**
     * 验证默认货币配置
     */
    private fun validateDefaultCurrency() {
        val defaultCurrencies = currencyConfigs.values.filter { it.default }
        when {
            defaultCurrencies.isEmpty() -> {
                console().sendLang("console_warn_no_default_currency")
            }
            defaultCurrencies.size > 1 -> {
                console().sendLang("console_warn_multiple_default_currency", defaultCurrencies.first().id)
            }
        }
    }
}

