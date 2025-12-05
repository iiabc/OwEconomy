package com.hiusers.mc.oweconomy.core.loader

import com.hiusers.mc.oweconomy.OwEconomy
import com.hiusers.mc.oweconomy.core.impl.api.DefaultMimicAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * @author iiabc
 * @since 2025/8/4 11:39
 */
object APILoader {

    @Awake(LifeCycle.INIT)
    fun load() {
        OwEconomy.register(DefaultMimicAPI())
    }

}