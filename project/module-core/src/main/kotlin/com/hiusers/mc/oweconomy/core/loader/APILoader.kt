package com.hiusers.mc.oweconomy.core.loader

import com.hiusers.mc.oweconomy.OwEconomy
import com.hiusers.mc.oweconomy.core.impl.api.EconomyAPIImpl
import com.hiusers.mc.oweconomy.core.impl.economy.EconomyLoader
import com.hiusers.mc.oweconomy.core.impl.economy.EconomyServiceImpl
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * API 加载器
 * @author iiabc
 * @since 2025/1/1
 */
object APILoader {

    @Awake(LifeCycle.INIT)
    fun load() {
        // 在 INIT 阶段创建服务实例并注册 API
        // 注意：ConfigReader.config 在 INIT 阶段可能还未初始化，配置加载在 ENABLE 阶段进行
        
        // 创建 EconomyServiceImpl 实例
        val economyService = EconomyServiceImpl()
        
        // 创建 EconomyAPI 实例
        val economyAPI = EconomyAPIImpl(economyService)
        
        // 保存 economyService 到 EconomyLoader，供后续使用
        EconomyLoader.setEconomyService(economyService)
        
        // 注册 API
        OwEconomy.register(economyAPI)
    }

}
