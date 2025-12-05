@file:Suppress("PropertyName", "SpellCheckingInspection")

plugins {
    id("com.github.johnrengelman.shadow")
}

taboolib {
    description {
        name(rootProject.name)
        prefix(rootProject.name)
        contributors {
            name("HiUsers")
            desc("Minecraft Quest")
        }
        links {
            name("homepage").url("https://iplugin.hiusers.com/")
        }
        dependencies {
            name("Adyeshach").optional(true)
            name("MythicMobs").optional(true)
        }
    }

    classifier = null
}


dependencies {
    rootProject.subprojects.filter { it.path.startsWith(":project:") }.forEach {
        if (it.name.contains("module-database")) {
            return@forEach
        }
        taboo(project(it.path)) { isTransitive = false }
    }

    configurations.implementation.get().dependencies.forEach {
        configurations.taboo.get().dependencies.add(it)
    }
}

tasks {
    jar {
        // 构件名
        archiveBaseName.set(rootProject.name)
    }
    shadowJar {
        dependsOn(jar)
        dependencies {
            exclude(dependency(".*:.*"))
        }
        from(jar)
        archiveBaseName.set(rootProject.name)
    }
    sourcesJar {
        // 构件名
        archiveBaseName.set(rootProject.name)
        // 打包子项目源代码
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
    build {
        dependsOn(shadowJar)
    }
}