@file:Suppress("PropertyName", "SpellCheckingInspection")

import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    kotlin("jvm") version "2.1.0"
}

val exposedVersion: String by project

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // TabooLib 配置
    taboolib {
        env {
            install(
                Basic,
                Bukkit,
                CommandHelper,
                Kether,
                BukkitUtil,
                BukkitNavigation,
                BukkitHook,
                JavaScript
            )
        }
        version {
            taboolib = "6.2.4-abd325ee"
        }
        relocate("top.maplex.arim", "${rootProject.group}.arim")
    }

    // 全局仓库
    repositories {
        // mavenLocal()
        mavenCentral()
        maven("https://repo.hiusers.com/releases")
        maven("https://repo.codemc.io/repository/creatorfromhell/")
    }

    // 全局依赖
    dependencies {
        compileOnly("ink.ptms.core:v12104:12104:mapped")
        compileOnly("ink.ptms.core:v12104:12104:universal")

        compileOnly("com.google.code.gson:gson:2.8.7")
        implementation("top.maplex.arim:Arim:1.2.14")
        compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.16")

        compileOnly("com.zaxxer:HikariCP:4.0.3")

        compileOnly("org.jetbrains.exposed:exposed-core:${exposedVersion}")
        compileOnly("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
        compileOnly("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
        compileOnly("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    }
    // 编译配置
    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}
