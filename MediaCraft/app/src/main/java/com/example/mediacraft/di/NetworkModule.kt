package com.example.mediacraft.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 这里的组件决定了依赖的生命周期，SingletonComponent 表示跟 App 同生共死
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/" // 模拟器访问本地电脑 localhost 的特殊地址
    private const val TIME_OUT = 60L // 秒

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true // 接口返回多余字段时不报错，增强兼容性
            coerceInputValues = true // 容错处理
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 日志拦截器，用于 Debug 模式下查看请求详情
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS) // 连接超时
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)    // 读取超时（上传视频时很重要）
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)   // 写入超时
            .retryOnConnectionFailure(true)             // 失败重连
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // 使用 Kotlin Serialization 替代 Gson，性能更好，更适合 Kotlin
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // TODO: 后面等我们定义了 ApiService 接口，再加一个 provideApiService 方法
}