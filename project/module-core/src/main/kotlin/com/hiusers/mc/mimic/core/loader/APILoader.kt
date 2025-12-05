package com.hiusers.mc.mimic.core.loader

import com.hiusers.mc.mimic.Mimic
import com.hiusers.mc.mimic.core.impl.api.DefaultMimicAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * @author iiabc
 * @since 2025/8/4 11:39
 */
object APILoader {

    @Awake(LifeCycle.INIT)
    fun load() {
        Mimic.register(DefaultMimicAPI())
    }

}