package com.hiusers.mc.oweconomy.kether.extra

import com.hiusers.mc.oweconomy.util.MathUtil
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.module.kether.KetherParser
import taboolib.module.kether.combinationParser

/**
 * 数学运算解析
 *
 * @author iiabc
 * @since 2025/9/25 15:58
 */
@RuntimeDependencies(
    RuntimeDependency(
        "!org.openjdk.nashorn:nashorn-core:15.4",
        test = "!jdk.nashorn.api.scripting.NashornScriptEngineFactory",
    )
)
object ActionNashorn {

    @KetherParser(["calculate", "math-calculate", "math-nashorn"], namespace = "quest_engine", shared = true)
    fun parser() = combinationParser {
        it.group(
            text(),
        ).apply(it) { text ->
            now {
                MathUtil.calculateExpression(text, variables().toMap())
            }
        }
    }

}