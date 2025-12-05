dependencies {
    compileOnly(project(":project:module-util"))
    compileOnly(project(":project:module-config"))
    compileOnly(project(":project:module-api"))
    compileOnly(project(":project:module-core"))
}

// 子模块
taboolib { subproject = true }