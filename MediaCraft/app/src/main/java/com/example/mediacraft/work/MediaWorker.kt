package com.example.mediacraft.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Config
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
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
import android.util.Log


@HiltWorker
class MediaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ffmpegHelper: FFmpegHelper, // 注入 FFmpeg 工具
    private val recordDao: RecordDao         // 注入数据库 DAO
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val inputPath = inputData.getString("input_path") ?: return failure("输入路径为空")
        val outputPath = inputData.getString("output_path") ?: return failure("输出路径为空")
        val command = inputData.getString("command") ?: return failure("命令为空")
        val recordId = inputData.getLong("record_id", -1L)

        // 2. 【调试】检查输入文件是否存在
        val inputFile = File(inputPath)
        if (!inputFile.exists() || inputFile.length() == 0L) {
            Log.e("MediaWorker", "文件不存在或大小为0: $inputPath")
            updateRecordStatus(recordId, AppConstants.STATUS_FAILURE)
            return Result.failure(workDataOf("error" to "Input file not found"))
        }

        Log.d("MediaWorker", "开始执行命令: $command")
        Log.d("MediaWorker", "输入文件大小: ${inputFile.length()} 字节")

        setForeground(createForegroundInfo("正在处理视频...", 0))

        return try {
            // 3. 执行
            ffmpegHelper.executeCommand(command, outputPath).collect { state ->
                when (state) {
                    is FFmpegHelper.State.Progress -> {
                        setForeground(createForegroundInfo("处理中: ${state.percent}%", state.percent))
                    }
                    is FFmpegHelper.State.Success -> {
                        Log.d("MediaWorker", "FFmpeg 执行成功")
                    }
                    is FFmpegHelper.State.Failure -> {
                        // 这里会抛出异常被 catch 捕获
                        Log.e("MediaWorker", "FFmpeg 执行失败: ${state.error}")
                        throw Exception(state.error)
                    }
                    else -> {}
                }
            }

            if (recordId != -1L) {
                updateRecordStatus(recordId, AppConstants.STATUS_SUCCESS)
            }

            // 记得执行完删除缓存文件
            if (inputFile.exists()) {
                inputFile.delete()
            }

            Result.success(workDataOf("output_path" to outputPath))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaWorker", "Worker 发生异常: ${e.message}")

            if (recordId != -1L) {
                updateRecordStatus(recordId, AppConstants.STATUS_FAILURE)
            }

            // 尝试删除缓存文件
            if (inputFile.exists()) inputFile.delete()

            Result.failure(workDataOf("error" to e.message))
        }
    }

    // 辅助方法：快速返回失败
    private fun failure(msg: String): Result {
        Log.e("MediaWorker", msg)
        return Result.failure(workDataOf("error" to msg))
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