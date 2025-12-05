@file:Suppress("PropertyName", "SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption

val exposedVersion: String by project

plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":plugin"))
}

tasks {
    val pluginShadow = project(":plugin").tasks.named("shadowJar", ShadowJar::class.java)

    shadowJar {
        dependsOn(pluginShadow)

        // 不直接打进任何坐标依赖，由下方 from 引入已产出的构件
        dependencies {
            exclude(dependency(".*:.*"))
        }

        // 引入 plugin 的 shadow 产物
        from(pluginShadow.get().archiveFile)

        // 如果存在 database 子模块，则一起聚合
        val dbProject = findProject(":project:module-database")
        if (dbProject != null) {
            val dbShadow = project(":project:module-database").tasks.named("shadowJar", ShadowJar::class.java)
            dependsOn(dbShadow)
            from(dbShadow.get().archiveFile) {
                exclude("META-INF")
            }
        }

        // 使用 spigot 的 libraries 载入
        doLast {
            val finalJarFile = archiveFile.get().asFile
            val jarUri = URI.create("jar:${finalJarFile.toURI()}")
            FileSystems.newFileSystem(jarUri, emptyMap<String, Any>()).use { fs ->
                val pluginYmlPath = fs.getPath("plugin.yml")
                if (Files.exists(pluginYmlPath)) {
                    Files.write(
                        pluginYmlPath,
                        """
                            
                            libraries:
                              - org.jetbrains.exposed:exposed-core:$exposedVersion
                              - org.jetbrains.exposed:exposed-dao:$exposedVersion
                              - org.jetbrains.exposed:exposed-jdbc:$exposedVersion
                              - org.jetbrains.exposed:exposed-java-time:$exposedVersion
                        """.trimIndent().toByteArray(Charsets.UTF_8),
                        StandardOpenOption.APPEND
                    )
                }
            }
        }

        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("bundle")
    }

    build {
        dependsOn(shadowJar)
    }
}


