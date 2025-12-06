package com.hiusers.mc.oweconomy.database.repo

import com.hiusers.mc.oweconomy.database.table.PlayerBalanceTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

/**
 * 余额 Repository
 * @author iiabc
 * @since 2025/1/1
 */
object BalanceRepository {

    /**
     * 获取余额
     */
    fun getBalance(uuid: UUID, currencyId: String): BigDecimal {
        return transaction {
            PlayerBalanceTable.selectAll()
                .where {
                    (PlayerBalanceTable.uuid eq uuid) and (PlayerBalanceTable.currencyId eq currencyId)
                }
                .limit(1)
                .firstOrNull()
                ?.get(PlayerBalanceTable.balance)
                ?: BigDecimal.ZERO
        }
    }

    /**
     * 设置余额
     * 使用 try-catch 处理并发情况下的主键冲突
     */
    fun setBalance(uuid: UUID, currencyId: String, balance: BigDecimal) {
        transaction {
            try {
                // 先尝试插入
                PlayerBalanceTable.insert {
                    it[PlayerBalanceTable.uuid] = uuid
                    it[PlayerBalanceTable.currencyId] = currencyId
                    it[PlayerBalanceTable.balance] = balance
                }
            } catch (e: ExposedSQLException) {
                // 如果是主键冲突（并发情况），则执行更新
                if (e.cause is SQLIntegrityConstraintViolationException) {
                    PlayerBalanceTable.update({
                        (PlayerBalanceTable.uuid eq uuid) and (PlayerBalanceTable.currencyId eq currencyId)
                    }) {
                        it[PlayerBalanceTable.balance] = balance
                    }
                } else {
                    // 其他异常继续抛出
                    throw e
                }
            }
        }
    }

    /**
     * 增加余额
     */
    fun addBalance(uuid: UUID, currencyId: String, amount: BigDecimal): BigDecimal {
        val currentBalance = getBalance(uuid, currencyId)
        val newBalance = currentBalance + amount
        setBalance(uuid, currencyId, newBalance)
        return newBalance
    }

    /**
     * 减少余额
     */
    fun subtractBalance(uuid: UUID, currencyId: String, amount: BigDecimal): BigDecimal {
        val currentBalance = getBalance(uuid, currencyId)
        val newBalance = (currentBalance - amount).coerceAtLeast(BigDecimal.ZERO)
        setBalance(uuid, currencyId, newBalance)
        return newBalance
    }

    /**
     * 获取玩家所有货币余额
     */
    fun getAllBalances(uuid: UUID): Map<String, BigDecimal> {
        return transaction {
            PlayerBalanceTable.selectAll()
                .where { PlayerBalanceTable.uuid eq uuid }
                .associate { row ->
                    row[PlayerBalanceTable.currencyId] to row[PlayerBalanceTable.balance]
                }
        }
    }

    /**
     * 获取货币的所有余额
     */
    fun getAllBalancesByCurrency(currencyId: String): Map<UUID, BigDecimal> {
        return transaction {
            PlayerBalanceTable.selectAll()
                .where { PlayerBalanceTable.currencyId eq currencyId }
                .associate { row ->
                    row[PlayerBalanceTable.uuid] to row[PlayerBalanceTable.balance]
                }
        }
    }

    /**
     * 清零货币余额
     */
    fun resetCurrencyBalances(currencyId: String): Int {
        return transaction {
            val now = System.currentTimeMillis()
            val updated = PlayerBalanceTable.update({ PlayerBalanceTable.currencyId eq currencyId }) {
                it[PlayerBalanceTable.balance] = BigDecimal.ZERO
                it[PlayerBalanceTable.lastReset] = now
            }
            updated
        }
    }

    /**
     * 更新最后清零时间
     */
    fun updateLastReset(uuid: UUID, currencyId: String, timestamp: Long) {
        transaction {
            PlayerBalanceTable.update({
                (PlayerBalanceTable.uuid eq uuid) and (PlayerBalanceTable.currencyId eq currencyId)
            }) {
                it[PlayerBalanceTable.lastReset] = timestamp
            }
        }
    }

    /**
     * 检查账户是否支持货币
     */
    fun accountSupportsCurrency(uuid: UUID, currencyId: String): Boolean {
        return transaction {
            PlayerBalanceTable.selectAll()
                .where {
                    (PlayerBalanceTable.uuid eq uuid) and (PlayerBalanceTable.currencyId eq currencyId)
                }
                .any()
        }
    }
}

