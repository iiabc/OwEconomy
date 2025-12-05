package com.hiusers.mc.mimic.util

/**
 * @author iiabc
 * @since 2025/8/25 11:27
 */

/**
 * 这是一个通用的列表替换函数，支持一次性替换多个字符串或字符串列表
 *
 * @param replacements 包含占位符和替换值的可变参数对，替换值可以是 String 或 List<String>
 * @return 替换后的新列表。
 */
fun List<String>.replaceMulti(vararg replacements: Pair<String, Any>): List<String> {
    // 将替换参数分为列表替换和字符串替换
    val listReplacements = replacements.filter { it.second is List<*> }
    val stringReplacements = replacements.filter { it.second !is List<*> }

    // 第一次遍历：处理所有列表替换
    var currentList = this.flatMap { line ->
        // 找到第一个匹配的列表占位符
        val listMatch = listReplacements.firstOrNull { line.contains(it.first) }

        if (listMatch != null) {
            // 如果找到，展开列表并用其中的每一项替换占位符
            val placeholder = listMatch.first
            val replacementValue = listMatch.second as List<*>

            replacementValue.filterIsInstance<String>().map { line.replace(placeholder, it) }
        } else {
            // 如果没有，返回原行
            listOf(line)
        }
    }

    // 第二次遍历：处理所有字符串替换
    currentList = currentList.map { line ->
        // 逐个处理所有字符串替换
        stringReplacements.fold(line) { currentLine, pair ->
            currentLine.replace(pair.first, pair.second.toString())
        }
    }

    return currentList
}