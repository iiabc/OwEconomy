package com.hiusers.mc.oweconomy

import com.hiusers.mc.oweconomy.api.EconomyAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import taboolib.platform.util.bukkitPlugin

/**
 * @author iiabc
 * @since 2025/7/25 23:40
 */
object OwEconomy {

    internal var api: EconomyAPI? = null

    fun api(): EconomyAPI {
        return api ?: throw IllegalStateException("OwEconomy API 未完成加载")
    }

    fun register(api: EconomyAPI) {
        this.api = api
        Bukkit.getServicesManager().register(
            EconomyAPI::class.java,
            api,
            bukkitPlugin,
            ServicePriority.Normal
        )
    }

}