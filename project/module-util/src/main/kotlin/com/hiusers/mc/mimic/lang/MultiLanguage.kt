package com.hiusers.mc.mimic.lang

import com.hiusers.mc.mimic.util.parseConfigToFlatNodes
import taboolib.common.platform.ProxyPlayer
import taboolib.module.configuration.Type
import taboolib.module.lang.event.PlayerSelectLocaleEvent
import taboolib.module.lang.event.SystemSelectLocaleEvent
import top.maplex.arim.tools.folderreader.releaseResourceFolderAndRead
import java.util.*

/**
 * @author iiabc
 * @since 2025/8/22 19:45
 */
object MultiLanguage {

    /**
     * 语言文件路径列表
     */
    private val paths = mutableListOf<String>()

    /**
     * 语言文件缓存
     */
    val fileLanguage = HashMap<String, FileLanguage>()

    /**
     * 语言文件代码转换
     */
    val languageCodeTransfer = hashMapOf(
        "zh_hans_cn" to "zh_cn",
        "zh_hant_cn" to "zh_tw",
        "en_ca" to "en_us",
        "en_au" to "en_us",
        "en_gb" to "en_us",
        "en_nz" to "en_us",
    )

    /**
     * 添加新的语言路径并加载
     */
    fun addLanguagePath(path: String) {
        if (!paths.contains(path)) {
            paths.add(path)
            loadLanguageFromPath(path)
        }
    }

    /**
     * 从指定路径加载语言文件
     */
    private fun loadLanguageFromPath(path: String) {
        releaseResourceFolderAndRead(path) {
            // 设置读取类型为YAML
            setReadType(Type.YAML)

            // 遍历处理每个配置文件
            walk {
                val fileName = file?.name ?: return@walk
                val languageCode = fileName.substringBeforeLast(".").lowercase()

                // 解析配置文件内容到语言节点
                val nodes = parseConfigToFlatNodes(this)

                // 合并到缓存中（如果已存在则覆盖）
                if (fileLanguage.containsKey(languageCode)) {
                    fileLanguage[languageCode]?.nodes?.putAll(nodes)
                } else {
                    fileLanguage[languageCode] = FileLanguage(nodes)
                }
            }
        }
    }

    /**
     * 获取玩家语言
     */
    fun getLocale(player: ProxyPlayer): String? {
        return try {
            val clientLocale = player.locale.lowercase()
            val transferredLocale = languageCodeTransfer[clientLocale] ?: clientLocale
            PlayerSelectLocaleEvent(player, transferredLocale).run {
                call()
                if (fileLanguage.containsKey(locale)) locale else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取控制台语言
     */
    fun getLocale(): String? {
        return try {
            val code = Locale.getDefault().toLanguageTag().replace("-", "_").lowercase()
            val transferredCode = languageCodeTransfer[code] ?: code
            SystemSelectLocaleEvent(transferredCode).run {
                call()
                val lowerLocale = locale.lowercase()
                if (fileLanguage.containsKey(lowerLocale)) lowerLocale else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取语言文件
     */
    fun getLanguageFile(locale: String): FileLanguage? {
        val normalizedLocale = locale.lowercase()

        // 尝试通过转换表匹配
        val transferredCode = languageCodeTransfer[normalizedLocale]
        if (transferredCode != null && fileLanguage.containsKey(transferredCode)) {
            return fileLanguage[transferredCode]
        }

        // 尝试大小写不敏感匹配
        val caseInsensitiveMatch = fileLanguage.keys.firstOrNull {
            it.lowercase() == normalizedLocale
        }
        if (caseInsensitiveMatch != null) {
            return fileLanguage[caseInsensitiveMatch]
        }

        return null
    }

}
