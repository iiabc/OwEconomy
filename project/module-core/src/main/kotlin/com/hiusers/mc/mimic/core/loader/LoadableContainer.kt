package com.hiusers.mc.mimic.core.loader

import com.hiusers.mc.mimic.system.BaseListContainer
import com.hiusers.mc.mimic.system.Loadable
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.lang.Language

object LoadableContainer : BaseListContainer<Loadable>() {

    @Awake(LifeCycle.ENABLE)
    fun load() {
        // lang 启用复合文本模式
        Language.enableSimpleComponent = true
        getAll().sortedBy {
            it.priority
        }.forEach { it.load() }
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        // 按倒序卸载
        getAll().reversed().forEach {
            it.unload()
        }
    }

    fun reset() {
        unload()
        load()
    }

}