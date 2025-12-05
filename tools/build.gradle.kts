import java.util.Properties
import java.io.File

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {}

// 读取 rename.properties 或项目属性，执行一键改名（直接执行）
tasks.register("renameProject") {
    group = "tools"
    description = "Rename project, group and packages across modules."

    doLast {
        val propsFile = rootProject.file("rename.properties")
        val props = Properties()
        if (propsFile.exists()) propsFile.inputStream().use(props::load)

        val newName = (project.findProperty("newName") as String?) ?: props.getProperty("newName")
        val newGroup = (project.findProperty("newGroup") as String?) ?: props.getProperty("newGroup")
        val newPackage = (project.findProperty("newPackage") as String?)
            ?: props.getProperty("newPackage")
            ?: newGroup
        val confirm = true

        require(!newName.isNullOrBlank()) { "newName is required" }
        require(!newGroup.isNullOrBlank()) { "newGroup is required" }
        // newPackage 为空时默认使用 newGroup，减少重复配置
        require(!newPackage.isNullOrBlank()) { "newPackage is required" }

        // 读取当前 group 与待修改内容（先不写入，便于 dry-run）
        val gradleProps = rootProject.file("gradle.properties")
        val text = gradleProps.readText()
        val currentGroup = Regex("^group=(.*)$", RegexOption.MULTILINE).find(text)?.groupValues?.get(1)
            ?: "com.hiusers.mc.mimic"
        val updatedGradleProps = text.replace(Regex("^group=.*$", RegexOption.MULTILINE), "group=${newGroup}")

        // 修改 settings.gradle.kts 的 rootProject.name（先计算结果）
        val settingsFile = rootProject.file("settings.gradle.kts")
        val settingsText = settingsFile.readText()
        val settingsUpdated = settingsText.replace(Regex("rootProject.name = \\\".*\\\""), "rootProject.name = \"${newName}\"")

        // 目录与包名路径信息
        val oldGroup = currentGroup
        val oldPackagePath = oldGroup.replace('.', '/')
        val newPackagePath = newPackage.replace('.', '/')

        // 寻找每个模块的 kotlin 源目录
        val moduleRoots = listOf(rootProject.file("project"), rootProject.file("plugin"))
        val kotlinSrcDirs = mutableListOf<File>()
        moduleRoots.forEach { root ->
            if (root.exists()) {
                root.walkTopDown().maxDepth(6).forEach { f ->
                    if (f.isDirectory && f.path.replace('\\', '/').endsWith("src/main/kotlin")) {
                        kotlinSrcDirs += f
                    }
                }
            }
        }

        // 计划的目录移动与文件修改（用于 dry-run 输出）
        val plannedMoves = mutableListOf<Pair<File, File>>()
        val plannedFileEdits = mutableListOf<File>()

        kotlinSrcDirs.forEach { srcDir ->
            val fromDir = File(srcDir, oldPackagePath)
            val toDir = File(srcDir, newPackagePath)
            if (fromDir.exists() && fromDir.isDirectory && fromDir.path != toDir.path) {
                plannedMoves += fromDir to toDir
            }
            srcDir.walkTopDown().forEach { f ->
                if (f.isFile && f.extension == "kt") {
                    val kt = f.readText()
                    val ktUpdated = kt.replace("package ${oldGroup}", "package ${newPackage}")
                        .replace("${oldGroup}.", "${newPackage}.")
                    if (kt != ktUpdated) plannedFileEdits += f
                }
            }
        }

        // 始终执行

        // 执行写入：gradle.properties 与 settings.gradle.kts
        gradleProps.writeText(updatedGradleProps)
        settingsFile.writeText(settingsUpdated)

        // 执行目录移动与文件内容替换
        plannedMoves.forEach { (fromDir, toDir) ->
            toDir.mkdirs()
            project.copy {
                from(fromDir)
                into(toDir)
            }
            fromDir.deleteRecursively()
        }
        kotlinSrcDirs.forEach { srcDir ->
            srcDir.walkTopDown().forEach { f ->
                if (f.isFile && f.extension == "kt") {
                    val kt = f.readText()
                    val ktUpdated = kt.replace("package ${oldGroup}", "package ${newPackage}")
                        .replace("${oldGroup}.", "${newPackage}.")
                    if (kt != ktUpdated) f.writeText(ktUpdated)
                }
            }
        }

        println("[Applied] Project renamed to ${newName}, group=${newGroup}, package=${newPackage} (defaulted from group if omitted)")
    }
}

// 仅预览（dry-run）任务
tasks.register("renameProjectPreview") {
    group = "tools"
    description = "Preview project rename changes without applying."

    doLast {
        val propsFile = rootProject.file("rename.properties")
        val props = Properties()
        if (propsFile.exists()) propsFile.inputStream().use(props::load)

        val newName = (project.findProperty("newName") as String?) ?: props.getProperty("newName")
        val newGroup = (project.findProperty("newGroup") as String?) ?: props.getProperty("newGroup")
        val newPackage = (project.findProperty("newPackage") as String?)
            ?: props.getProperty("newPackage")
            ?: newGroup

        require(!newName.isNullOrBlank()) { "newName is required" }
        require(!newGroup.isNullOrBlank()) { "newGroup is required" }
        require(!newPackage.isNullOrBlank()) { "newPackage is required" }

        val gradleProps = rootProject.file("gradle.properties")
        val text = gradleProps.readText()
        val currentGroup = Regex("^group=(.*)$", RegexOption.MULTILINE).find(text)?.groupValues?.get(1)
            ?: "com.hiusers.mc.mimic"

        val settingsFile = rootProject.file("settings.gradle.kts")
        val settingsText = settingsFile.readText()

        val oldGroup = currentGroup
        val oldPackagePath = oldGroup.replace('.', '/')
        val newPackagePath = newPackage.replace('.', '/')

        val moduleRoots = listOf(rootProject.file("project"), rootProject.file("plugin"))
        val kotlinSrcDirs = mutableListOf<File>()
        moduleRoots.forEach { root ->
            if (root.exists()) {
                root.walkTopDown().maxDepth(6).forEach { f ->
                    if (f.isDirectory && f.path.replace('\\', '/').endsWith("src/main/kotlin")) {
                        kotlinSrcDirs += f
                    }
                }
            }
        }

        val plannedMoves = mutableListOf<Pair<File, File>>()
        val plannedFileEdits = mutableListOf<File>()
        kotlinSrcDirs.forEach { srcDir ->
            val fromDir = File(srcDir, oldPackagePath)
            val toDir = File(srcDir, newPackagePath)
            if (fromDir.exists() && fromDir.isDirectory && fromDir.path != toDir.path) {
                plannedMoves += fromDir to toDir
            }
            srcDir.walkTopDown().forEach { f ->
                if (f.isFile && f.extension == "kt") {
                    val kt = f.readText()
                    val ktUpdated = kt.replace("package ${'$'}oldGroup", "package ${'$'}newPackage")
                        .replace("${'$'}oldGroup.", "${'$'}newPackage.")
                    if (kt != ktUpdated) plannedFileEdits += f
                }
            }
        }

        println("[Dry-Run] Will update rootProject.name -> ${newName} in settings.gradle.kts")
        if (oldGroup != newGroup) println("[Dry-Run] Will update group: ${oldGroup} -> ${newGroup} in gradle.properties")
        if (oldGroup != newPackage) println("[Dry-Run] Package rename: ${oldGroup} -> ${newPackage}") else println("[Dry-Run] Package equals group: ${newPackage}")
        println("[Dry-Run] Kotlin source roots: ${kotlinSrcDirs.joinToString()} ")
        println("[Dry-Run] Planned directory moves (${plannedMoves.size}):")
        plannedMoves.forEach { (f, t) -> println(" - ${f.path} -> ${t.path}") }
        println("[Dry-Run] Planned file edits (${plannedFileEdits.size}) (package/refs):")
        plannedFileEdits.take(50).forEach { println(" - ${it.path}") }
        if (plannedFileEdits.size > 50) println(" - ... and ${plannedFileEdits.size - 50} more")
    }
}

