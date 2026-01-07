// 根目录 build.gradle.kts
@Suppress("DSL_SCOPE_VIOLATION") // 忽略版本目录的一些 IDE 警告
plugins {
    // 引用 libs.versions.toml 中定义的插件，apply = false 表示仅在根目录声明，不在根目录应用
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false // KSP 插件
    alias(libs.plugins.hilt) apply false // Hilt 插件
    alias(libs.plugins.kotlinSerialization) apply false
}

true // 防止 Groovy 脚本残留问题，Kotlin DSL 中通常加上这个返回值
