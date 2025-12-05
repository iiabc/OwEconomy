package com.hiusers.mc.mimic.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hiusers.mc.mimic.lang.LangList
import com.hiusers.mc.mimic.lang.LangText
import com.hiusers.mc.mimic.lang.LangType
import com.hiusers.mc.mimic.serializer.GsonProvider
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration


/**
 * 从 YAML 文件反序列化到 Kotlin 对象
 */
inline fun <reified T> Configuration.deserialize(gson: Gson = GsonProvider.gson): T {
    val json = gson.toJson(this.toMap())
    return gson.fromJson(json, object : TypeToken<T>() {}.type)
}

/**
 * 从 YAML 文件的指定Key的Section反序列化到 Kotlin 对象
 */
inline fun <reified T> Configuration.deserialize(key: String, gson: Gson = GsonProvider.gson): T {
    val section = this.getConfigurationSection(key)
    val json = gson.toJson(section?.toMap())
    return gson.fromJson(json, object : TypeToken<T>() {}.type)
}

inline fun <reified T> ConfigurationSection.deserialize(key: String, gson: Gson = GsonProvider.gson): T {
    val section = this.getConfigurationSection(key)
    val json = gson.toJson(section?.toMap())
    return gson.fromJson(json, object : TypeToken<T>() {}.type)
}

/**
 * 将 YAML Configuration 转换为嵌套 Map
 */
@Suppress("UNCHECKED_CAST")
fun configToNestedMap(config: Configuration): Map<String, Any> {
    return config.toMap().filterValues { it != null } as Map<String, Any>
}

/**
 * 将嵌套 Map 转换为扁平键值对，支持列表类型
 */
fun flattenNestedMap(nestedMap: Map<String, Any>): HashMap<String, LangType> {
    val flatNodes = HashMap<String, LangType>()

    // 递归遍历嵌套 Map，生成扁平键
    fun traverseMap(currentMap: Map<String, Any>, parentKey: String = "") {
        currentMap.forEach { (key, value) ->
            val fullKey = if (parentKey.isEmpty()) key else "$parentKey.$key"

            when (value) {
                // 若值是嵌套 Map（对应 YAML 的 section），继续递归
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    traverseMap(value as Map<String, Any>, fullKey)
                }
                // 处理列表类型
                is List<*> -> {
                    val stringList = value.map { it?.toString() ?: "" }
                    flatNodes[fullKey] = LangList(stringList)
                }
                // 普通文本类型
                else -> {
                    flatNodes[fullKey] = LangText(value.toString())
                }
            }
        }
    }

    traverseMap(nestedMap)
    return flatNodes
}

/**
 * 直接从 Configuration 解析出扁平键值对
 */
fun parseConfigToFlatNodes(config: Configuration, gson: Gson = GsonProvider.gson): HashMap<String, LangType> {
    val nestedMap = configToNestedMap(config)
    // 通过 Gson 序列化后反序列化，确保 Map 结构正确
    val json = gson.toJson(nestedMap)
    val normalizedMap = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)
    return flattenNestedMap(normalizedMap)
}

