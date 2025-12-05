package com.hiusers.mc.oweconomy.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.expansion.createHelper

@CommandHeader(name = "OwEconomy", aliases = ["oe", "oweco"], permission = "oweconomy.command")
object Command {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

}