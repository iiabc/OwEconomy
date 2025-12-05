package com.hiusers.mc.mimic.core.impl.api

import com.hiusers.mc.mimic.api.QuestAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory

/**
 * @author iiabc
 * @since 2025/7/31 22:38
 */
class DefaultQuestAPI : QuestAPI {

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<QuestAPI>(DefaultQuestAPI())
        }

    }

}
