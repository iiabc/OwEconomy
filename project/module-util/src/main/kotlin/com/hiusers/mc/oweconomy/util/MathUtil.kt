package com.hiusers.mc.oweconomy.util

import java.text.DecimalFormat
import javax.script.SimpleBindings

object MathUtil {

    /**
     * 将 Double 转换为两位小数
     *
     * @return 格式化后的 Double
     */
    fun Double.toTwoDecimalPlaces(): Double {
        val decimalFormat = DecimalFormat("#.##")  // 格式化为最多保留两位小数
        return decimalFormat.format(this).toDouble()  // 格式化并转回 Double
    }

    fun calculateExpression(formula: String, variables: Map<String, Any>): Any? {
        // 创建变量上下文
        val bindings = SimpleBindings(variables)

        // 执行表达式
        return scriptEngine.eval(formula, bindings)
    }

}