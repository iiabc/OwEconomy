package com.hiusers.questengine.metrics

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.pluginVersion
import taboolib.module.metrics.Metrics

/**
 * 统计
 */
object Metrics {

    private val bStats by lazy {
        Metrics(12482, pluginVersion, Platform.BUKKIT)
    }

    @Awake(LifeCycle.ACTIVE)
    fun init() {
        bStats.let {
        }
    }

}