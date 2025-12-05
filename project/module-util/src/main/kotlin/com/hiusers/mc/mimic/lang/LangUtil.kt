package com.hiusers.mc.mimic.lang

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer

/**
 * @author iiabc
 * @since 2025/8/22 20:32
 */
/**
 * 发送语言消息的扩展函数
 */
fun Any.sendConfigLang(node: String, default: String, vararg args: Any) {
    val locale = getLocale()
    if (locale == null) {
        when (this) {
            is ProxyCommandSender -> sendMessage(default)
            else -> println(default)
        }
        return
    }
    val file = MultiLanguage.getLanguageFile(locale)
    val langType = file?.nodes?.get(node)
    langType?.send(this, *args)
}

/**
 * 发送列表类型语言消息的扩展函数
 */
fun Any.sendConfigLangList(node: String, default: List<String>, vararg args: Any) {
    val locale = getLocale()
    val messages = if (locale == null) {
        default
    } else {
        val file = MultiLanguage.getLanguageFile(locale)
        file?.nodes?.get(node)?.asTextList(*args) ?: default
    }

    messages.forEach { message ->
        when (this) {
            is ProxyCommandSender -> sendMessage(message)
            else -> println(message)
        }
    }
}

/**
 * 获取语言文本的扩展函数
 */
fun Any.asConfigLangText(node: String, default: String, vararg args: Any): String {
    val locale = getLocale() ?: return default
    val file = MultiLanguage.getLanguageFile(locale)
    val langType = file?.nodes?.get(node)
    return langType?.asText(*args) ?: default
}


/**
 * 获取列表类型语言文本的扩展函数
 */
fun Any.asConfigLangTextList(node: String, default: List<String>, vararg args: Any): List<String> {
    val locale = getLocale() ?: return default
    val file = MultiLanguage.getLanguageFile(locale)
    return file?.nodes?.get(node)?.asTextList(*args) ?: default
}

/**
 * 获取当前对象的语言设置
 */
private fun Any.getLocale(): String? {
    return when (this) {
        is ProxyPlayer -> {
            MultiLanguage.getLocale(this)
        }

        is Player -> {
            MultiLanguage.getLocale(adaptPlayer(this))
        }

        else -> {
            // 控制台使用系统语言
            MultiLanguage.getLocale()
        }
    }
}