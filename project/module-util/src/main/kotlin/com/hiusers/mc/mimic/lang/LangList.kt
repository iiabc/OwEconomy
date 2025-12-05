package com.hiusers.mc.mimic.lang

import taboolib.common.platform.ProxyCommandSender

/**
 * @author iiabc
 * @since 2025/8/24 18:32
 */
/**
 * 列表类型语言项实现
 */
class LangList(private val textList: List<String>?) : LangType {

    override fun asText(vararg args: Any): String? {
        return asTextList(*args).joinToString("\n")
    }

    override fun asTextList(vararg args: Any): List<String> {
        return textList?.map { replacePlaceholders(it, *args) } ?: emptyList()
    }

    override fun send(sender: Any, vararg args: Any) {
        asTextList(*args).forEach { message ->
            when (sender) {
                is ProxyCommandSender -> sender.sendMessage(message)
                else -> println("[$sender] $message")
            }
        }
    }

    /**
     * 替换单个文本中的占位符
     */
    private fun replacePlaceholders(text: String, vararg args: Any): String {
        val builder = StringBuilder(text)
        args.forEachIndexed { index, arg ->
            val placeholder = "{$index}"
            var start = builder.indexOf(placeholder)
            while (start != -1) {
                builder.replace(start, start + placeholder.length, arg.toString())
                start = builder.indexOf(placeholder, start + arg.toString().length)
            }
        }
        return builder.toString()
    }

    override fun toString(): String {
        return "LangList(textList=$textList)"
    }
}