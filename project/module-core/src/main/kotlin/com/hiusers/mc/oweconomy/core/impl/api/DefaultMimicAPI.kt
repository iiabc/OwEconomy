package com.hiusers.mc.oweconomy.core.impl.api

import com.hiusers.mc.oweconomy.api.MimicAPI
import com.hiusers.mc.oweconomy.api.QuestAPI
import taboolib.common.platform.PlatformFactory

/**
 * @author iiabc
 * @since 2025/7/25 23:43
 */
class DefaultMimicAPI : MimicAPI {

    override fun getQuestAPI(): QuestAPI {
        return PlatformFactory.getAPI<QuestAPI>()
    }

}