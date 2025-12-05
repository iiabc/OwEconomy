# Mimic

## 构建与使用

### 快速开始（IDEA）

- 不带数据库构建：在 Gradle 面板点击 `:plugin:build`
- 带数据库聚合构建：点击 `:plugin-bundle:build`
- 重命名预览：点击 `:tools:renameProjectPreview`
- 重命名执行：点击 `:tools:renameProject`

### 快速开始（命令行）

```bash
# 仅插件（不带数据库）
./gradlew :plugin:build

# 聚合构建（存在 module-database 时）
./gradlew :plugin-bundle:build

# 预览重命名（不写入）
./gradlew :tools:renameProjectPreview -q

# 执行重命名（基于 rename.properties 或 -PnewName/-PnewGroup/-PnewPackage）
./gradlew :tools:renameProject -q
```

### 关于 database 模块

- 不独立（当前默认）：
    - `:plugin` 产出为纯 TabooLib 插件（无数据库）
    - `:plugin-bundle` 产出为聚合版，若仓库包含 `project:module-database` 则自动合并，无需参数

- 独立 database（可选做法）：
    1. 在 `project:module-database` 中配置 `maven-publish`，发布 `shadowJar`（`-all.jar`）到 `mavenLocal` 或私服；
    2. `:plugin-bundle` 可选择：
        - 将 database 作为运行时库由 Libby 拉取；或
        - 继续在本地存在时拼接（当前脚本已支持本地存在自动拼接）。

> 说明：`plugin.yml` 由 TabooLib 自动生成。聚合构建（`:plugin-bundle`）在最终产物中追加 `libraries` 段以声明 Exposed
> 依赖，无需手动维护。

### 重命名模板项目

1. 编辑根目录 `rename.properties`：
    - `newName`：新项目名（写入 `settings.gradle.kts` 的 `rootProject.name`）
    - `newGroup`：新 Maven Group（写入 `gradle.properties` 的 `group`）
    - `newPackage`：可省略，省略时默认等于 `newGroup`
2. 预览变更（不写入）：
   ```bash
   ./gradlew :tools:renameProjectPreview -q
   ```
3. 应用变更：
   ```bash
   ./gradlew :tools:renameProject -q
   ```