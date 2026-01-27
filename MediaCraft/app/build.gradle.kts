import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)  // 启用 KSP (用于 Room 和 Hilt)
    alias(libs.plugins.hilt) // 启用 Hilt
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.example.mediacraft" // 修改为你的包名
    compileSdk = 34 // 编译 SDK 版本，建议用最新的

    defaultConfig {
        applicationId = "com.example.mediacraft"
        minSdk = 26 // 最低支持 Android 8.0 (为了更好的协程和 API 支持)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 开启 Jetpack Compose 支持
        vectorDrawables {
            useSupportLibrary = true
        }

        // 配置 Room 的 Schema 输出位置 (可选，方便查看数据库结构变化)
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        // 指定 Room 导出 Schema 的位置
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 生产环境通常开启混淆，开发阶段先关闭方便调试
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 编译选项：使用 Java 17 (现代 Android 开发标准)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin 选项
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true // 允许使用 BuildConfig 字段 (比如存 API Key)
        dataBinding = true // 允许使用 DataBinding
        viewBinding = true // 允许使用 ViewBinding
        compose = false
    }

    // 配置 Compose 编译器
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10" // 需与 Kotlin 版本匹配
    }

    // 排除重复的依赖文件
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
dependencies {
    // --- 核心基础 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- UI 基础 (XML开发必备) ---
    implementation(libs.androidx.appcompat) // 必须
    implementation(libs.google.material)    // Material Design 控件 (CardView, Button等)
    implementation(libs.androidx.constraintlayout) // 约束布局 (你的布局里用了)

    // --- Hilt (依赖注入) ---
    implementation(libs.hilt.android)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    ksp(libs.hilt.compiler)

    // --- Room (数据库) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- 网络 ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // --- 协程 ---
    implementation(libs.kotlinx.coroutines.android)

    // --- 测试 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation(files("libs/ffmpeg-kit-audio-6.0-2.aar"))
    implementation ("com.arthenica:smart-exception-java:0.2.1")

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // 侧滑菜单库
    implementation("com.github.chthai64:SwipeRevealLayout:1.4.0")

}