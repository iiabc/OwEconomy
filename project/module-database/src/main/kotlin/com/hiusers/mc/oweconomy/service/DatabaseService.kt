package com.hiusers.questengine.service

import com.hiusers.questengine.api.config.reader.ConfigReader
import com.hiusers.questengine.database.table.*
import com.hiusers.questengine.system.Loadable
import com.hiusers.questengine.system.Register
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import taboolib.common.io.newFile
import taboolib.common.platform.function.console
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang

/**
 * @author iiabc
 * @since 2025/6/1 21:45
 */
@RuntimeDependencies(
    RuntimeDependency(
        "!com.zaxxer:HikariCP:4.0.3",
        relocate = ["!com.zaxxer.hikari", "!com.hiusers.oweconomy.libs.hikari"]
    ),
)
@Register
object DatabaseService : Loadable {

    override val priority: Int
        get() = 1

    private var databaseSource: HikariDataSource? = null

    override fun load() {
        val databaseConfig = ConfigReader.config.getConfigurationSection("database") ?: return
        // 配置数据库连接池
        val hikariConfig = HikariConfig().apply {
            if (ConfigReader.databaseEnable) {
                jdbcUrl = buildJdbcUrl(databaseConfig)
                username = databaseConfig.getString("user", "root")
                password = databaseConfig.getString("password", "")

                // 连接池优化参数 - 针对高频查询优化
                // 增加到8个连接
                maximumPoolSize = databaseConfig.getInt("pool-size", 8)
                // 设置最小空闲连接
                minimumIdle = databaseConfig.getInt("minimum-idle", 4)
                // 减少连接超时
                connectionTimeout = databaseConfig.getLong("timeout", 20000)
                // 减少空闲超时
                idleTimeout = databaseConfig.getLong("idle-timeout", 300000)
                // 减少最大生命周期
                maxLifetime = databaseConfig.getLong("max-lifetime", 1200000)

                // 预编译语句缓存优化
                addDataSourceProperty("cachePrepStmts", "true")
                // 增加缓存大小
                addDataSourceProperty("prepStmtCacheSize", "500")
                // 增加SQL限制
                addDataSourceProperty("prepStmtCacheSqlLimit", "4096")
                addDataSourceProperty("useServerPrepStmts", "true")

                // 性能优化参数
                // 批量操作优化
                addDataSourceProperty("rewriteBatchedStatements", "true")
                // 本地会话状态
                addDataSourceProperty("useLocalSessionState", "true")
                // 本地事务状态
                addDataSourceProperty("useLocalTransactionState", "true")
                // 缓存结果集元数据
                addDataSourceProperty("cacheResultSetMetadata", "true")
                // 缓存服务器配置
                addDataSourceProperty("cacheServerConfiguration", "true")
                // 关闭时间统计
                addDataSourceProperty("maintainTimeStats", "false")
            } else {
                // SQLite
                val databaseFile = newFile(getDataFolder(), "database/quests.db")
                jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
                driverClassName = "org.sqlite.JDBC"
                username = null
                password = null

                // SQLite 连接池参数 - 针对单文件数据库优化
                // SQLite 单连接更稳定
                maximumPoolSize = databaseConfig.getInt("pool-size", 1)
                // 更短的超时
                connectionTimeout = databaseConfig.getLong("timeout", 3000)
                // 1分钟空闲超时
                idleTimeout = databaseConfig.getLong("idle-timeout", 60000)
                // 5分钟生命周期
                maxLifetime = databaseConfig.getLong("max-lifetime", 300000)

                // SQLite 性能优化参数
                addDataSourceProperty("foreign_keys", "true")
                // 使用 WAL 模式提升并发性能
                addDataSourceProperty("journal_mode", "WAL")
                // 平衡安全性和性能
                addDataSourceProperty("synchronous", "NORMAL")
                // 增加缓存页数
                addDataSourceProperty("cache_size", "10000")
                // 临时数据存储在内存
                addDataSourceProperty("temp_store", "MEMORY")
                // 256MB 内存映射
                addDataSourceProperty("mmap_size", "268435456")
                // 启用外键支持
                addDataSourceProperty("foreign_keys", "true")
            }
        }

        try {
            databaseSource = HikariDataSource(hikariConfig)
            Database.connect(databaseSource!!, connectionAutoRegistration = ExposedConnectionImpl())
            createTables()
            console().sendLang("success_connect_mysql")
        } catch (e: Exception) {
            console().sendLang("error_connect_mysql")
            disablePlugin()
            error(e.message ?: "Unknow error database")
        }
    }

    override fun unload() {
        databaseSource?.close()
    }

    private fun buildJdbcUrl(config: ConfigurationSection): String {
        val host = config.getString("host", "localhost")
        val port = config.getInt("port", 3306)
        val database = config.getString("database", "minecraft_db")
        val useSSL = config.getBoolean("use-ssl", false)
        val allowPublicKeyRetrieval = config.getBoolean("allow-public-key-retrieval", true)
        val useUnicode = config.getBoolean("use-unicode", true)
        val characterEncoding = config.getString("character-encoding", "UTF-8")

        return "jdbc:mysql://$host:$port/$database?useSSL=$useSSL&" +
                "allowPublicKeyRetrieval=$allowPublicKeyRetrieval&" +
                "useUnicode=$useUnicode&" +
                "characterEncoding=$characterEncoding&"
    }

    /**
     * 自动创建数据库表
     */
    private fun createTables() {
        transaction {
            SchemaUtils.create(
                PlayersTable
            )
        }
    }

}

