# OwEconomy 使用说明

## 一、玩家交易

### 命令
- `/oe pay <玩家名> <金额> [货币ID]` - 向指定玩家转账
  - 示例：`/oe pay Steve 100 gold` - 向 Steve 转账 100 金币
  - 如果不指定货币ID，使用默认货币
  - 权限：`oweconomy.command`（默认所有玩家）

## 二、系统奖励和扣除（命令）

### 命令

**系统奖励（自动截取超出上限的部分）**：
- `/oe give <玩家名> <金额> [货币ID]` - 系统奖励玩家
  - 示例：`/oe give Steve 1000 gold` - 奖励 Steve 1000 金币
  - 如果玩家余额将超出上限，自动截取到上限
  - 权限：`oweconomy.command.admin.give`

**系统扣除**：
- `/oe take <玩家名> <金额> [货币ID]` - 系统扣除玩家余额
  - 示例：`/oe take Steve 50 gold` - 扣除 Steve 50 金币
  - 权限：`oweconomy.command.admin.take`

**设置余额**：
- `/oe set <玩家名> <金额> [货币ID]` - 设置玩家余额
  - 示例：`/oe set Steve 500 gold` - 设置 Steve 的金币余额为 500
  - 权限：`oweconomy.command.admin.set`

**重载配置**：
- `/oe reload` - 重载配置文件
  - 权限：`oweconomy.command.admin.reload`

### 其他插件使用示例

其他插件可以通过执行命令来奖励/扣除玩家：

```yaml
# 抽奖插件配置示例
rewards:
  - "oweconomy give %player% 100 gold"
  - "oweconomy give %player% 50 diamond"
```

```java
// Java 插件中执行命令
Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "oweconomy give " + player.getName() + " 100 gold");
```

### API 使用
```kotlin
val economyAPI = OwEconomy.api()
val response = economyAPI.transfer(
    from = player1.uniqueId,
    to = player2.uniqueId,
    currencyId = "gold",
    amount = BigDecimal("100")
)

if (response.type == EconomyResponse.ResponseType.SUCCESS) {
    // 交易成功
}
```

## 三、API 使用（编程方式）

### 玩家交易

### API 使用

**系统奖励（自动截取超出上限的部分）**：
```kotlin
val economyAPI = OwEconomy.api()
val response = economyAPI.reward(
    accountId = player.uniqueId,
    currencyId = "gold",
    amount = BigDecimal("1000")
)
// 如果玩家余额上限是 500，实际只会奖励 500
```

**系统扣除**：
```kotlin
val economyAPI = OwEconomy.api()
val response = economyAPI.deduct(
    accountId = player.uniqueId,
    currencyId = "gold",
    amount = BigDecimal("50")
)
```

**设置余额**：
```kotlin
val economyAPI = OwEconomy.api()
val response = economyAPI.setBalance(
    accountId = player.uniqueId,
    currencyId = "gold",
    amount = BigDecimal("100")
)
```

**获取余额**：
```kotlin
val economyAPI = OwEconomy.api()
val balance = economyAPI.getBalance(
    accountId = player.uniqueId,
    currencyId = "gold"
)
```

## 四、PlaceholderAPI 占位符

### 可用占位符

1. **余额相关**
   - `%oweconomy_balance_<货币ID>%` - 获取指定货币的余额（数字）
   - `%oweconomy_balance%` - 获取默认货币的余额
   - `%oweconomy_balanceformatted_<货币ID>%` - 获取格式化的余额（带货币名称）
   - `%oweconomy_balanceformatted%` - 获取默认货币的格式化余额

2. **货币上限**
   - `%oweconomy_maxbalance_<货币ID>%` - 获取货币上限（-1 显示为 ∞）
   - `%oweconomy_maxbalance%` - 获取默认货币的上限

3. **货币名称**
   - `%oweconomy_currencyname_<货币ID>%` - 获取货币名称（单数）
   - `%oweconomy_currencyname%` - 获取默认货币名称（单数）
   - `%oweconomy_currencynameplural_<货币ID>%` - 获取货币名称（复数）
   - `%oweconomy_currencynameplural%` - 获取默认货币名称（复数）

### 使用示例

```
%oweconomy_balance_gold%          # 显示金币余额：1000.00
%oweconomy_balanceformatted_gold% # 显示：1000.00 金币
%oweconomy_maxbalance_gold%       # 显示上限：10000
%oweconomy_currencyname_gold%     # 显示：金币
```

## 五、配置示例

```yaml
currencies:
  gold:
    default: true  # 作为旧版 Vault API 的默认货币
    display-name-singular: "金币"
    display-name-plural: "金币"
    max-balance: 10000  # -1 表示无上限
    fractional-digits: 2
    reset-cycle: "week 1 06 00"  # 每周一 06:00 清零
  diamond:
    display-name-singular: "钻石"
    display-name-plural: "钻石"
    max-balance: -1  # 无上限
    fractional-digits: 0
    # reset-cycle 不填，表示永不清零
```

## 六、事件监听

### BalanceChangeEvent
余额变更时触发
```kotlin
@EventHandler
fun onBalanceChange(event: BalanceChangeEvent) {
    val accountId = event.accountId
    val currencyId = event.currencyId
    val amount = event.amount  // 变更金额（正数为增加，负数为减少）
    val newBalance = event.newBalance
}
```

### CurrencyResetEvent
货币清零时触发
```kotlin
@EventHandler
fun onCurrencyReset(event: CurrencyResetEvent) {
    val currencyId = event.currencyId
    val accountCount = event.accountCount  // 清零的账户数量
}
```

### BalanceLimitExceededEvent
余额上限超出时触发（系统奖励时）
```kotlin
@EventHandler
fun onBalanceLimitExceeded(event: BalanceLimitExceededEvent) {
    val accountId = event.accountId
    val currencyId = event.currencyId
    val attemptedAmount = event.attemptedAmount  // 尝试的金额
    val maxBalance = event.maxBalance  // 上限
}
```
