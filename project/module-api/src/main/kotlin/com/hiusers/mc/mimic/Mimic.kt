package com.hiusers.mc.mimic

import com.hiusers.mc.mimic.api.MimicAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import taboolib.platform.util.bukkitPlugin

/**
 * @author iiabc
 * @since 2025/7/25 23:40
 */
object Mimic {

    internal var api: com.hiusers.mc.mimic.api.MimicAPI? = null

    fun api(): com.hiusers.mc.mimic.api.MimicAPI {
        return api ?: throw IllegalStateException("QuestEngine API 未完成加载")
    }

    fun register(api: com.hiusers.mc.mimic.api.MimicAPI) {
        this.api = api
        Bukkit.getServicesManager().register(
            MimicAPI::class.java,
            api,
            bukkitPlugin,
            ServicePriority.Normal
        )
    }

}