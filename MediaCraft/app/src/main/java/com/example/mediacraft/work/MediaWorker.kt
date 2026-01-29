package com.example.mediacraft.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.room.util.copy
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mediacraft.R
import com.example.mediacraft.data.local.dao.RecordDao
import com.example.mediacraft.data.local.entity.ProcessingRecord
import com.example.mediacraft.utils.AppConstants
import com.example.mediacraft.utils.FFmpegHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.last
import java.io.File

@HiltWorker
class MediaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ffmpegHelper: FFmpegHelper, // 注入 FFmpeg 工具
    private val recordDao: RecordDao         // 注入数据库 DAO
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. 获取输入参数
        val inputPath = inputData.getString("input_path") ?: return Result.failure()
        val outputPath = inputData.getString("output_path") ?: return Result.failure()
        val command = inputData.getString("command") ?: return Result.failure()
        val recordId = inputData.getLong("record_id", -1L)

        // 2. 提升为前台服务 (防止被系统查杀)
        setForeground(createForegroundInfo("正在处理视频...", 0))

        return try {
            // 3. 执行 FFmpeg 命令
            // executeCommand 返回的是 Flow，我们收集它
            ffmpegHelper.executeCommand(command, outputPath).collect { state ->
                when (state) {
                    is FFmpegHelper.State.Progress -> {
                        // 更新通知栏进度
                        setForeground(createForegroundInfo("处理中: ${state.percent}%", state.percent))
                        // 也可以 updateProgress() 给 UI 观察
                    }
                    is FFmpegHelper.State.Success -> {
                        // 成功
                    }
                    is FFmpegHelper.State.Failure -> {
                        throw Exception(state.error)
                    }
                    else -> {}
                }
            }

            // 4. 更新数据库状态为成功
            if (recordId != -1L) {
                updateRecordStatus(recordId, AppConstants.STATUS_SUCCESS)
            }

            Result.success(workDataOf("output_path" to outputPath))

        } catch (e: Exception) {
            e.printStackTrace()
            // 5. 更新数据库状态为失败
            if (recordId != -1L) {
                updateRecordStatus(recordId, AppConstants.STATUS_FAILURE)
            }
            // 返回 retry() 可以让 WorkManager 自动重试 (比如因为断网或文件占用)
            // 这里如果是 FFmpeg 报错通常是命令错误，重试没用，所以返回 failure
            Result.failure(workDataOf("error" to e.message))
        }
    }

    private suspend fun updateRecordStatus(id: Long, status: Int) {
        // 这是一个简化的更新逻辑，实际建议在 Dao 加一个 updateStatusById
        // 这里为了简单，我们假设数据库里已经有这条记录，我们只是更新状态
        // 实际上更严谨的做法是先查出来再 copy 更新
        val record = recordDao.getRecordById(id) // 需要在 Dao 加这个方法
        record?.let {
            recordDao.update(it.copy(status = status))
        }
    }

    // 创建前台通知
    private fun createForegroundInfo(content: String, progress: Int): ForegroundInfo {
        val channelId = "media_processing_channel"
        val notificationId = 1001 // 唯一的通知 ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Media Processing", NotificationManager.IMPORTANCE_LOW)
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("MediaCraft 后台任务")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 替换成你的图标
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}