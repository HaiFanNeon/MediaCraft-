package com.example.mediacraft.ui.audio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediacraft.R
import com.example.mediacraft.databinding.FragmentExtractAudioBinding // 假设你创建了这个布局
import com.example.mediacraft.utils.PathUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExtractAudioFragment : Fragment(R.layout.fragment_extract_audio) {

    private val viewModel: ExtractAudioViewModel by viewModels()
    private var _binding: FragmentExtractAudioBinding? = null
    private val binding get() = _binding!!
    private var currentVideoUri: Uri? = null

    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                currentVideoUri = it
                val pathStr = PathUtils.getRealPathFromUri(requireContext(), it) ?: it.toString()
                binding.tvSelectedPath.text = "已选: $pathStr"
                binding.btnStartCompress.isEnabled = true // 布局里ID可能还是叫 btnStartCompress，没关系
                binding.tvStatus.text = "准备提取音频..."
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExtractAudioBinding.bind(view)

        binding.btnSelectVideo.setOnClickListener {
            // 依然是选择视频文件
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            selectVideoLauncher.launch(intent)
        }

        binding.btnStartCompress.text = "开始提取 MP3" // 修改按钮文字
        binding.btnStartCompress.setOnClickListener {
            currentVideoUri?.let { uri ->
                viewModel.extractAudio(uri) // 调用新的 ViewModel 方法
            }
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ExtractAudioViewModel.UiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is ExtractAudioViewModel.UiState.Processing -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnStartCompress.isEnabled = false
                        binding.btnSelectVideo.isEnabled = false
                        binding.tvStatus.text = "正在提取音频..."
                    }
                    is ExtractAudioViewModel.UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnStartCompress.isEnabled = true
                        binding.btnSelectVideo.isEnabled = true
                        binding.tvStatus.text = "提取成功！\n路径: ${state.outputPath}"
                        Toast.makeText(requireContext(), "提取 MP3 成功！", Toast.LENGTH_LONG).show()
                    }
                    is ExtractAudioViewModel.UiState.Error -> {
                        // ... 错误处理同上
                        binding.progressBar.visibility = View.GONE
                        binding.btnStartCompress.isEnabled = true
                        binding.btnSelectVideo.isEnabled = true
                        binding.tvStatus.text = "失败: ${state.message}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}