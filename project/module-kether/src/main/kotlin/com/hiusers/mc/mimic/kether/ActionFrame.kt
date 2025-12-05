package com.hiusers.mc.mimic.kether

import org.bukkit.entity.Player
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.script

object ActionFrame {

    fun ScriptFrame.questID() = variables().get<Any?>("quest_id")
        .orElse(null)?.toString() ?: error("unknown quest id")

    fun ScriptFrame.objectiveID() = variables().get<Any?>("objective_id")
        .orElse(null)?.toString() ?: error("unknown objective id")

    fun ScriptFrame.player() = script().sender?.castSafely<Player>() ?: error("unknown player")

}