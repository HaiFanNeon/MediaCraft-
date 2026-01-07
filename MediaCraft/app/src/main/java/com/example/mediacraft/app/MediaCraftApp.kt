package com.example.mediacraft.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// 必须加这个注解，Hilt 才能工作
@HiltAndroidApp
class MediaCraftApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 这里以后可以做日志初始化等
    }
}