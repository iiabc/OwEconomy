package com.hiusers.mc.mimic.kether

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.util.VariableReader
import taboolib.common5.Coerce
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptOptions
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

object ActionUtil {

    fun Player.runAction(script: String, variable: Map<String, Any> = mutableMapOf()): CompletableFuture<Any> {
        return try {
            KetherShell.eval(
                script,
                ScriptOptions.builder()
                    .sender(adaptCommandSender(this))
                    .vars(variable)
                    .namespace(listOf("quest_engine"))
                    .build()
            ).thenApply { it ?: false }
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    fun Player.intActionAsync(
        script: String,
        variable: Map<String, Any> = mutableMapOf()
    ): CompletableFuture<Int> {
        if (script.isEmpty()) return CompletableFuture.completedFuture(0)
        return runAction(script, variable)
            .thenApply { Coerce.toInteger(it) }
            .exceptionally { 0 }
    }

    @Deprecated("Use intActionAsync instead to avoid blocking")
    fun Player.intAction(script: String, variable: Map<String, Any> = mutableMapOf()): Int {
        if (script.isEmpty()) return 0
        return runAction(script, variable).thenApply {
            Coerce.toInteger(it)
        }.get()
    }

    fun Player.booleanActionAsync(
        script: String,
        variable: Map<String, Any> = mutableMapOf()
    ): CompletableFuture<Boolean> {
        if (script.isEmpty()) return CompletableFuture.completedFuture(true)
        return runAction(script, variable)
            .thenApply { Coerce.toBoolean(it) }
            .exceptionally { false }
    }

    /**
     * 返回 Boolean 结果
     */
    @Deprecated("Use booleanActionAsync instead to avoid blocking")
    fun Player.booleanAction(script: String, variable: Map<String, Any> = mutableMapOf()): Boolean {
        if (script.isEmpty()) return true
        return runAction(script, variable).thenApply {
            Coerce.toBoolean(it)
        }.get()
    }

    private val variableReader = VariableReader(start = "{{", end = "}}")

    fun Player.parseScriptAsync(script: String, variable: Map<String, Any> = mapOf()): CompletableFuture<String> {
        // 解析所有变量部分
        val parts = variableReader.readToFlatten(script)
        val variableParts = parts.filter { it.isVariable }

        // 收集所有变量的解析任务
        val futures = variableParts.map { part ->
            runAction(part.text, variable)
                .thenApply { part.text to it.toString() }
                .exceptionally { part.text to it.localizedMessage }
        }

        // 所有变量解析完成后进行替换
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply {
                val variableMap = futures.associate { it.join() }
                // 重建最终字符串
                buildString {
                    parts.forEach { part ->
                        if (part.isVariable) {
                            // 替换为解析后的值，找不到时保留原始变量
                            append(variableMap[part.text] ?: "{{${part.text}}}")
                        } else {
                            append(part.text)
                        }
                    }
                }
            }
    }

    /**
     * 替换解析内联语句
     */
    @Deprecated("Use parseScriptAsync instead to avoid blocking")
    fun Player.parseScript(script: String, variable: Map<String, Any> = mapOf()): String {
        return VariableReader().replaceNested(script) {
            val text = try {
                runAction(this, variable).thenApply {
                    it.toString()
                }.get()
            } catch (ex: Exception) {
                ex.localizedMessage
            }
            val e = replace(this, text)
            e
        }
    }

    // 异步解析脚本列表
    fun Player.parseScriptListAsync(
        scriptList: List<String>,
        variable: Map<String, Any> = mapOf()
    ): CompletableFuture<List<String>> {
        val parseFutures = scriptList.map { parseScriptAsync(it, variable) }
        return CompletableFuture.allOf(*parseFutures.toTypedArray())
            .thenApply { parseFutures.map { it.join() } }
    }

    @Deprecated("Use parseScriptListAsync instead to avoid blocking")
    fun Player.parseScriptList(scriptList: List<String>, variable: Map<String, Any> = mapOf()): List<String> {
        return scriptList.map { parseScript(it, variable) }
    }

    /**
     * 无执行者
     */
    fun runAction(script: String, variable: Map<String, Any> = mutableMapOf()): CompletableFuture<Any> {
        return try {
            KetherShell.eval(
                script,
                ScriptOptions.builder()
                    .vars(variable)
                    .namespace(listOf("quest_engine"))
                    .build()
            ).thenApply { it }
        } catch (ex: Throwable) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    /**
     * 无执行者解析返回字符串
     */
    @Deprecated("Use stringActionAsync instead to avoid blocking")
    fun String.stringAction(variable: Map<String, Any> = mutableMapOf()): String {
        return runAction(this, variable).thenApply {
            it.toString()
        }.get()
    }

    fun String.stringActionAsync(variable: Map<String, Any> = mutableMapOf()): CompletableFuture<String> {
        return runAction(this, variable)
            .thenApply { it.toString() }
            .exceptionally { it.localizedMessage }
    }

}