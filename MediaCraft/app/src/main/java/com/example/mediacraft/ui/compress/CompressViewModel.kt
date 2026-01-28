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
import com.example.mediacraft.utils.AppConstants


@HiltViewModel
class CompressViewModel @Inject constructor(
    private val ffmpegHelper: FFmpegHelper,
    private val repository: VideoRepository,
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

            // 1. 定义缓存文件
            val cacheFile = File(context.cacheDir, "temp_compress_input.mp4")

            // 2. 使用 ContentResolver 通过 Uri 读取文件流 (这是 Android 10+ 唯一合法的读取方式)
            try {
                if (cacheFile.exists()) cacheFile.delete()

                // 打开输入流 -> 打开输出流 -> 管道传输
                context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    cacheFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: run {
                    // 如果 openInputStream 返回 null
                    _uiState.value = UiState.Error("无法打开文件流，Uri 可能已失效")
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("文件复制失败: ${e.message}")
                return@launch
            }

            // 3. 后续步骤不变（生成输出路径、FFmpeg 命令）
            val outputDir = context.getExternalFilesDir("compressed_videos")
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            val outputPath = "${outputDir?.absolutePath}/compress_$timestamp.mp4"

            // 使用 cacheFile.absolutePath 作为输入
            val command = "-i \"${cacheFile.absolutePath}\" -c:v mpeg4 -q:v 6 \"$outputPath\""

            // 4. 执行命令
            ffmpegHelper.executeCommand(command, outputPath).collect { state ->
                val taskType = AppConstants.TASK_TYPE_COMPRESS // 替换 "AudioExtraction"

                when (state) {
                    is FFmpegHelper.State.Progress -> {
                        _uiState.value = UiState.Compressing(state.percent)
                    }
                    is FFmpegHelper.State.Success -> {
                        // 记录可以使用 inputUri.toString() 或者维持原来的路径字符串逻辑（如果有的话）
                        repository.saveProcessingRecord(inputUri.toString(), outputPath, true, taskType)
                        _uiState.value = UiState.Success(state.outputPath)
                        if (cacheFile.exists()) cacheFile.delete()
                    }
                    is FFmpegHelper.State.Failure -> {
                        repository.saveProcessingRecord(inputUri.toString(), outputPath, false, taskType)
                        _uiState.value = UiState.Error(state.error)
                        if (cacheFile.exists()) cacheFile.delete()
                    }
                    else -> {}
                }
            }
        }
    }
}