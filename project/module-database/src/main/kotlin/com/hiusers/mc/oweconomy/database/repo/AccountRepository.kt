package com.hiusers.mc.oweconomy.database.repo

import com.hiusers.mc.oweconomy.database.table.AccountTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLTransactionRollbackException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * 账户 Repository
 * @author iiabc
 * @since 2025/1/1
 */
object AccountRepository {

    /**
     * 基于 UUID 的同步锁映射，用于避免同一账户的并发操作导致死锁
     */
    private val accountLocks = ConcurrentHashMap<UUID, Any>()
    
    /**
     * 账户存在性缓存，避免频繁查询数据库
     * true = 账户存在，false = 账户不存在，null = 未缓存
     */
    private val accountExistenceCache = ConcurrentHashMap<UUID, Boolean>()
    
    /**
     * 获取或创建账户锁
     */
    private fun getAccountLock(uuid: UUID): Any {
        return accountLocks.computeIfAbsent(uuid) { Any() }
    }
    
    /**
     * 清除账户存在性缓存
     */
    private fun clearAccountCache(uuid: UUID) {
        accountExistenceCache.remove(uuid)
    }
    
    /**
     * 标记账户存在
     */
    private fun markAccountExists(uuid: UUID) {
        accountExistenceCache[uuid] = true
    }
    
    /**
     * 标记账户不存在
     */
    private fun markAccountNotExists(uuid: UUID) {
        accountExistenceCache[uuid] = false
    }

    /**
     * 检查是否是死锁异常
     */
    private fun isDeadlockException(e: Throwable?): Boolean {
        if (e == null) return false
        val message = e.message ?: ""
        return e is SQLTransactionRollbackException || 
               message.contains("Deadlock", ignoreCase = true) ||
               e.javaClass.simpleName.contains("MySQLTransactionRollbackException", ignoreCase = true)
    }

    /**
     * 创建或更新账户
     * 使用基于 UUID 的同步锁避免并发冲突，减少死锁发生
     * 直接尝试插入，如果主键冲突则更新，避免查询-插入/更新的竞态条件
     * 添加重试机制处理 MySQL 死锁
     */
    fun upsertAccount(uuid: UUID, name: String, isPlayer: Boolean = true) {
        // 使用基于 UUID 的锁，确保同一账户的操作是串行的
        synchronized(getAccountLock(uuid)) {
            var retries = 5
            while (retries > 0) {
                try {
                    transaction {
                        try {
                            // 直接尝试插入
                            AccountTable.insert {
                                it[AccountTable.id] = uuid
                                it[AccountTable.name] = name
                                it[AccountTable.isPlayer] = isPlayer
                            }
                        } catch (e: ExposedSQLException) {
                            // 如果插入时发生主键冲突（账户已存在），则执行更新
                            if (e.cause is SQLIntegrityConstraintViolationException) {
                                AccountTable.update({ AccountTable.id eq uuid }) {
                                    it[AccountTable.name] = name
                                    it[AccountTable.isPlayer] = isPlayer
                                }
                            } else {
                                throw e
                            }
                        }
                    }
                    // 成功，更新缓存并退出重试循环
                    markAccountExists(uuid)
                    return
                } catch (e: ExposedSQLException) {
                    // 如果是死锁，重试
                    if (isDeadlockException(e.cause)) {
                        retries--
                        if (retries > 0) {
                            // 使用指数退避策略，避免所有线程同时重试
                            val baseWaitTime = 100L
                            val maxWaitTime = 1000L
                            val waitTime = minOf(
                                baseWaitTime * (1 shl (5 - retries)),
                                maxWaitTime
                            ) + Random.nextLong(0, 100)
                            Thread.sleep(waitTime)
                            continue
                        }
                    }
                    // 其他异常或重试次数用完，抛出异常
                    throw e
                }
            }
        }
    }

    /**
     * 检查账户是否存在
     * 使用缓存避免频繁查询数据库
     */
    fun hasAccount(uuid: UUID): Boolean {
        // 先检查缓存
        accountExistenceCache[uuid]?.let { return it }
        
        // 缓存未命中，查询数据库
        val exists = transaction {
            AccountTable.selectAll().where { AccountTable.id eq uuid }.any()
        }
        
        // 更新缓存
        accountExistenceCache[uuid] = exists
        return exists
    }

    /**
     * 获取账户名称
     */
    fun getAccountName(uuid: UUID): String? {
        return transaction {
            AccountTable.selectAll().where { AccountTable.id eq uuid }.limit(1).firstOrNull()
                ?.get(AccountTable.name)
        }
    }

    /**
     * 重命名账户
     */
    fun renameAccount(uuid: UUID, newName: String): Boolean {
        return transaction {
            val updated = AccountTable.update({ AccountTable.id eq uuid }) {
                it[AccountTable.name] = newName
            }
            updated > 0
        }
    }

    /**
     * 删除账户
     */
    fun deleteAccount(uuid: UUID): Boolean {
        val result = transaction {
            AccountTable.deleteWhere { AccountTable.id eq uuid }
            true
        }
        // 清除缓存
        clearAccountCache(uuid)
        return result
    }

    /**
     * 获取所有账户UUID
     */
    fun getAllAccountUUIDs(): Map<UUID, String> {
        return transaction {
            AccountTable.selectAll().associate { row ->
                val uuid: UUID = row[AccountTable.id].value
                val name: String = row[AccountTable.name]
                uuid to name
            }
        }
    }
}

