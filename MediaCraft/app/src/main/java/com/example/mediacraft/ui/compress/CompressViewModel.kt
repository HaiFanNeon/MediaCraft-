package com.example.mediacraft.ui.compress

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediacraft.data.repository.VideoRepository
import com.example.mediacraft.utils.FFmpegHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import android.net.Uri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mediacraft.data.local.entity.ProcessingRecord
import com.example.mediacraft.utils.AppConstants
import com.example.mediacraft.worker.MediaWorker


@HiltViewModel
class CompressViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "COMPRESS"

    // UI状态：空闲、加载中、成功、失败
    // 使用 Sealed Class 定义状态，让 UI 层的逻辑更严谨
    sealed class UiState {
        object Idle : UiState() // 空闲
        data class Compressing(val progress: Int) : UiState() // 压缩中
        data class Success(val outputPath: String) : UiState() // 成功
        data class Error(val message: String) : UiState() // 失败
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    /**
     * 开始压缩视频
     * 使用 Uri + ContentResolver 解决权限被拒绝的问题
     */
    fun compressVideo(inputUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Compressing(0)

            // 1. 复制文件到 Cache (保持不变)
            val cacheFile = File(context.cacheDir, "work_${System.currentTimeMillis()}.mp4")
            try {
                if (cacheFile.exists()) cacheFile.delete()
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: run {
                    _uiState.value = UiState.Error("无法打开文件流")
                    return@launch
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("文件复制失败: ${e.message}")
                return@launch
            }

            // 2. 生成输出路径 (保持不变)
            val outputDir = context.getExternalFilesDir("compressed_videos")
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val outputPath = "${outputDir?.absolutePath}/compress_${System.currentTimeMillis()}.mp4"

            // 3. 构建命令 (保持不变)
            val command = "-i \"${cacheFile.absolutePath}\" -c:v mpeg4 -q:v 6 \"$outputPath\""

            // 4. 【关键】先插入一条 "处理中/排队中" 的记录，并拿到 ID
            val newRecord = ProcessingRecord(
                originalPath = inputUri.toString(),
                outputPath = outputPath,
                taskType = AppConstants.TASK_TYPE_COMPRESS,
                status = AppConstants.STATUS_PROCESSING, // 建议状态定义为: 0-处理中/排队中
                timestamp = System.currentTimeMillis()
            )

            // 调用刚刚写好的 Repository 方法
            val recordId = repository.insertRecordAndGetId(newRecord)

            // 5. 创建 WorkRequest (把 recordId 传进去)
            val workRequest = OneTimeWorkRequestBuilder<MediaWorker>()
                .setInputData(
                    workDataOf(
                        "input_path" to cacheFile.absolutePath,
                        "output_path" to outputPath,
                        "command" to command,
                        "record_id" to recordId // 传入 ID
                    )
                )
                .addTag("compression")
                .build()

            // 6. 提交给 WorkManager
            workManager.enqueue(workRequest)

            // 7. 更新 UI 为“已加入队列”
            _uiState.value = UiState.Success("任务已加入后台队列，您可以去查看历史记录")
        }
    }
}