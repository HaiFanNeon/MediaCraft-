package com.example.mediacraft.ui.audio

import android.content.Context
import android.net.Uri
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

@HiltViewModel
class ExtractAudioViewModel @Inject constructor(
    private val ffmpegHelper: FFmpegHelper,
    private val repository: VideoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 复用一下压缩的状态定义，也可以单独定义
    // 这里为了简单演示直接用字符串表示状态，实际项目中建议用 Sealed Class
    sealed class UiState {
        object Idle : UiState()
        object Processing : UiState() // 提取通常很快，不需要进度条
        data class Success(val outputPath: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun extractAudio(inputUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Processing

            // 1. 复制文件到 Cache (解决权限问题)
            val cacheFile = File(context.cacheDir, "temp_extract_input.mp4")
            try {
                if (cacheFile.exists()) cacheFile.delete()
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("文件读取失败: ${e.message}")
                return@launch
            }

            // 2. 生成输出路径 (保存为 mp3)
            val outputDir = context.getExternalFilesDir("extracted_audio")
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            // 注意后缀名是 .mp3
            val outputPath = "${outputDir?.absolutePath}/audio_$timestamp.mp3"

            // 3. 构建 FFmpeg 命令 (核心)
            // -i 输入
            // -vn : Disable Video (不处理视频流，只处理音频)
            // -c:a libmp3lame : 使用 MP3 编码器 (需要 GPL 版本的 ffmpeg-kit)
            // -q:a 2 : 音频质量 (0-9, 0最好, 2是标准高质量)
            // 如果你使用的是 LGPL 版本，libmp3lame 可能不可用，可以改用 aac 编码器输出 .m4a 文件:
            // val command = "-i \"${cacheFile.absolutePath}\" -vn -c:a aac \"$outputPath_m4a\""

            // 这里假设你已经切换到了 GPL 版本
            val command = "-i \"${cacheFile.absolutePath}\" -vn -c:a libmp3lame -q:a 2 \"$outputPath\""

            // 4. 执行
            ffmpegHelper.executeCommand(command, outputPath).collect { state ->
                when (state) {
                    is FFmpegHelper.State.Success -> {
                        // 记录任务类型为 "AudioExtraction"
                        repository.saveProcessingRecord(inputUri.toString(), outputPath, true, "AudioExtraction")
                        _uiState.value = UiState.Success(state.outputPath)
                        if (cacheFile.exists()) cacheFile.delete()
                    }
                    is FFmpegHelper.State.Failure -> {
                        repository.saveProcessingRecord(inputUri.toString(), outputPath, false, "AudioExtraction")
                        _uiState.value = UiState.Error(state.error)
                        if (cacheFile.exists()) cacheFile.delete()
                    }
                    // 提取音频通常很快，不需要进度，所以忽略 Progress 状态
                    else -> {}
                }
            }
        }
    }
}