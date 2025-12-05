package com.hiusers.mc.mimic.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.expansion.createHelper

@CommandHeader(name = "QuestEngine", aliases = ["qe", "qen"], permission = "questengine.command")
object Command {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

}