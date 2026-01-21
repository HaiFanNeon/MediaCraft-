package com.example.mediacraft.ui.compress

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediacraft.data.repository.VideoRepository
import com.example.mediacraft.utils.FFmpegHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CompressViewModel @Inject constructor(
    private val fFmpegHelper: FFmpegHelper,
    private val repository: VideoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

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

    fun compressVideo(inputPath: String) {
        viewModelScope.launch {
            val outputDir = context.getExternalFilesDir("compressed_videos")
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val timestamp = System.currentTimeMillis()
            val outputPath = "${outputDir?.absolutePath}/compress_$timestamp.mp4"

            val command = "-i \"$inputPath\" -vcodec libx264 -crf 28 -preset ultrafast \"$outputPath\""

            fFmpegHelper.executeCommand(command, outputPath).collect { state ->
                when(state) {
                    is FFmpegHelper.State.Progress -> {
                        _uiState.value = UiState.Compressing(state.percent)
                    }
                    is FFmpegHelper.State.Success -> {
                        repository.saveProcessingRecord(inputPath, outputPath, true)
                        _uiState.value = UiState.Success(state.outputPath)
                    }
                    is FFmpegHelper.State.Failure -> {
                        repository.saveProcessingRecord(inputPath, outputPath, false)
                        _uiState.value = UiState.Error(state.error)
                    }
                    else -> {
                        // TODO: 处理Idle等其他状态
                    }
                }

            }

        }
    }
}