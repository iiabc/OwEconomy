package com.hiusers.mc.mimic.api.config.reader

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

object ConfigReader {

    @Config(migrate = true, autoReload = true)
    lateinit var config: Configuration
        private set

    @ConfigNode("database.table")
    var tableName: String = "qe"

    @ConfigNode("database.enable")
    var databaseEnable: Boolean = false

}