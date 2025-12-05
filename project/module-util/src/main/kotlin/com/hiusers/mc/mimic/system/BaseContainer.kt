package com.hiusers.mc.mimic.system

abstract class BaseContainer<K, T> {

    // 使用 MutableMap 存储数据，子类无需重复定义
    private val map = mutableMapOf<K, T>()

    // 插入元素
    fun put(key: K, value: T) {
        map[key] = value
    }

    // 获取元素
    fun get(key: K): T? {
        return map[key]
    }

    // 清空容器
    fun clear() {
        map.clear()
    }

    // 获取所有的 keys
    fun keys(): Set<K> {
        return map.keys
    }

    // 获取所有的 values
    fun values(): Collection<T> {
        return map.values
    }

    fun getAll(): Map<K, T> {
        return map
    }

}
