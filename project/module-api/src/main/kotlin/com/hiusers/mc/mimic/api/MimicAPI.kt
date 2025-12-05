package com.hiusers.mc.mimic.api

/**
 * MimicAPI 是一个提供与系统交互的接口
 * 其他插件或模块应通过此接口来访问相关的功能，以保持代码的解耦性
 */
interface MimicAPI {

    fun getQuestAPI(): QuestAPI

}