package com.example.mediacraft.ui.compress

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
import com.example.mediacraft.databinding.FragmentCompressBinding
import com.example.mediacraft.utils.PathUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompressFragment : Fragment(R.layout.fragment_compress) {

    // 注意这里是 viewModels()，生命周期跟随 Fragment
    private val viewModel: CompressViewModel by viewModels()
    private var _binding: FragmentCompressBinding? = null
    private val binding get() = _binding!!

    private var currentVideoUri: Uri? = null

    // Fragment中使用 ActivityResultLauncher
    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                currentVideoUri = it
                // 使用 requireContext()
                val pathStr = PathUtils.getRealPathFromUri(requireContext(), it) ?: it.toString()
                binding.tvSelectedPath.text = "已选: $pathStr"
                binding.btnStartCompress.isEnabled = true
                binding.tvStatus.text = "等待压缩..."
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCompressBinding.bind(view)
        initListeners()
        observeState()
    }

    private fun initListeners() {
        binding.btnSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            selectVideoLauncher.launch(intent)
        }

        binding.btnStartCompress.setOnClickListener {
            currentVideoUri?.let { uri ->
                viewModel.compressVideo(uri)
            }
        }
    }

    private fun observeState() {
        // 使用 viewLifecycleOwner.lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CompressViewModel.UiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is CompressViewModel.UiState.Compressing -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnStartCompress.isEnabled = false
                        binding.btnSelectVideo.isEnabled = false
                        binding.tvStatus.text = "正在压缩中... ${state.progress}%"
                    }
                    is CompressViewModel.UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnStartCompress.isEnabled = true
                        binding.btnSelectVideo.isEnabled = true
                        binding.tvStatus.text = "压缩成功！\n路径: ${state.outputPath}"
                        // 使用 requireContext()
                        Toast.makeText(requireContext(), "压缩成功！", Toast.LENGTH_LONG).show()
                    }
                    is CompressViewModel.UiState.Error -> {
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