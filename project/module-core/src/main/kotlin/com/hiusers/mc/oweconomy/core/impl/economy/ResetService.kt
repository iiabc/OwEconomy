package com.hiusers.mc.oweconomy.core.impl.economy

import com.hiusers.mc.oweconomy.core.event.CurrencyResetEvent
import com.hiusers.mc.oweconomy.database.repo.BalanceRepository
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.common5.util.parseTimeCycle
import taboolib.module.lang.sendLang

/**
 * 定期清零服务
 * @author iiabc
 * @since 2025/1/1
 */
object ResetService {

    /**
     * 启动定时清零任务
     */
    @Awake(LifeCycle.ENABLE)
    fun startResetTask() {
        // 每分钟检查一次（20 ticks = 1秒，1200 ticks = 60秒）
        submit(async = false, period = 20L * 60L) {
            checkAndReset()
        }
    }

    /**
     * 检查并执行清零
     */
    private fun checkAndReset() {
        val currencyConfigs = CurrencyConfigService.getAllCurrencies()
        val now = System.currentTimeMillis()

        currencyConfigs.values
            .filter { it.resetCycle != null && it.resetCycle.isNotBlank() }
            .forEach { config ->
                try {
                    val cycle = config.resetCycle!!.parseTimeCycle().start(now)
                    if (cycle.isTimeout) {
                        resetCurrency(config.id)
                    }
                } catch (e: Exception) {
                    console().sendLang("console_error_reset_cycle_invalid", config.id, config.resetCycle ?: "")
                    console().sendLang("console_error_reset_cycle_error", e.message ?: "Unknown error")
                }
            }
    }

    /**
     * 清零指定货币的所有余额
     */
    fun resetCurrency(currencyId: String) {
        val count = BalanceRepository.resetCurrencyBalances(currencyId)
        
        // 触发事件
        val event = CurrencyResetEvent(currencyId, count)
        event.call()
        
        console().sendLang("console_success_currency_reset", currencyId, count.toString())
    }
}

