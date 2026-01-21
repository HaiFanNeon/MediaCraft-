package com.example.mediacraft.utils

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 封装 FFmpeg 操作，将回调风格转换为协程 Flow 风格
 * @Singleton 保证全局只有一个 helper
 */
@Singleton
class FFmpegHelper @Inject constructor() {

    // 定义一个简单的密封类来表示处理状态
    sealed class State {
        object Idle : State()
        data class Progress(val percent: Int) : State()
        data class Success(val outputPath: String) : State()
        data class Failure(val error: String) : State()
    }

    /**
     * 执行命令并返回 Flow 状态流
     * 使用 callbackFlow 可以完美地将第三方库的回调（callback）转换成协程流（Flow）
     */
    fun executeCommand(command: String, outputPath: String): Flow<State> = callbackFlow {
        // 1. 发送开始信号
        trySend(State.Progress(0))

        Log.d("FFmpeg", "开始执行命令: $command")

        // 2. 开启异步会话
        val session = FFmpegKit.executeAsync(command,
            { session ->
                // 完成回调
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    trySend(State.Success(outputPath))
                } else {
                    trySend(State.Failure("FFmpeg Error: ${session.failStackTrace}"))
                }
                close() // 关闭流
            },
            { log ->
                // 日志回调，可以在这里分析错误
                // Log.d("FFmpegLog", log.message)
            },
            { statistics ->
                // 进度回调 (这里只是简单示例，真实进度需要根据视频总时长计算)
                // 暂时简单的发送一个活动信号
                // trySend(State.Progress(...))
            }
        )

        // 3. 当 Flow 被取消时（例如用户退出了界面），取消 FFmpeg 任务
        awaitClose {
            session.cancel()
        }
    }
}