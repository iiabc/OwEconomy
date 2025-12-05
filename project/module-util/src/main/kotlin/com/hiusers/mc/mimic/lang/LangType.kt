package com.hiusers.mc.mimic.lang

import taboolib.common.platform.ProxyCommandSender


/**
 * @author iiabc
 * @since 2025/8/22 19:46
 */
/**
 * 语言类型接口
 */
interface LangType {
    fun asText(vararg args: Any): String?
    fun send(sender: Any, vararg args: Any)
    fun asTextList(vararg args: Any): List<String>
}

/**
 * 文本类型实现
 */
class LangText(private val text: String?) : LangType {

    override fun asText(vararg args: Any): String? {
        return text?.let {
            val builder = StringBuilder(it)
            args.forEachIndexed { index, arg ->
                val placeholder = "{$index}"
                var start = builder.indexOf(placeholder)
                while (start != -1) {
                    builder.replace(start, start + placeholder.length, arg.toString())
                    start = builder.indexOf(placeholder, start + arg.toString().length)
                }
            }
            builder.toString()
        }
    }

    override fun send(sender: Any, vararg args: Any) {
        val message = asText(*args)
        if (message != null) {
            when (sender) {
                is ProxyCommandSender -> sender.sendMessage(message)
                else -> println("[$sender] $message")
            }
        }
    }

    override fun asTextList(vararg args: Any): List<String> {
        return asText(*args)?.let { listOf(it) } ?: emptyList()
    }

    override fun toString(): String {
        return "LangText(text=$text)"
    }

}
