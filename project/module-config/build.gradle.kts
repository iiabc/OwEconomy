dependencies {
    compileOnly(project(":project:module-kether"))
    compileOnly(project(":project:module-util"))
}

// 子模块
taboolib { subproject = true }