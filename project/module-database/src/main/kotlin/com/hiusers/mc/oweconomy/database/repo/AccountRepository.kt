package com.hiusers.mc.oweconomy.database.repo

import com.hiusers.mc.oweconomy.database.table.AccountTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

/**
 * 账户 Repository
 * @author iiabc
 * @since 2025/1/1
 */
object AccountRepository {

    /**
     * 创建或更新账户
     */
    fun upsertAccount(uuid: UUID, name: String, isPlayer: Boolean = true) {
        transaction {
            val exists = AccountTable.selectAll().where { AccountTable.id eq uuid }.any()
            if (exists) {
                AccountTable.update({ AccountTable.id eq uuid }) {
                    it[AccountTable.name] = name
                    it[AccountTable.isPlayer] = isPlayer
                }
            } else {
                AccountTable.insert {
                    it[AccountTable.id] = uuid
                    it[AccountTable.name] = name
                    it[AccountTable.isPlayer] = isPlayer
                }
            }
        }
    }

    /**
     * 检查账户是否存在
     */
    fun hasAccount(uuid: UUID): Boolean {
        return transaction {
            AccountTable.selectAll().where { AccountTable.id eq uuid }.any()
        }
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
        return transaction {
            AccountTable.deleteWhere { AccountTable.id eq uuid }
            true
        }
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

