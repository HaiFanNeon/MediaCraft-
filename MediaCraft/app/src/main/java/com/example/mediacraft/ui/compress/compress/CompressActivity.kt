package com.example.mediacraft.ui.compress

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mediacraft.R
import com.example.mediacraft.utils.PathUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // 必须加这个注解，Hilt 才能注入 ViewModel
class CompressActivity : AppCompatActivity() {

    private val viewModel: CompressViewModel by viewModels()

    // UI 控件变量
    private lateinit var tvSelectedPath: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnStartCompress: Button
    private lateinit var btnSelectVideo: Button

    private var currentVideoUri: Uri? = null
    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                currentVideoUri = it // 保存 Uri

                // 仅用于显示文件名，不影响核心逻辑
                // 假如 PathUtils 还是获取不到，就显示 Uri 字符串
                val pathStr = PathUtils.getRealPathFromUri(this, it) ?: it.toString()
                tvSelectedPath.text = "已选: $pathStr"

                btnStartCompress.isEnabled = true
                tvStatus.text = "等待压缩..."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress)

        initViews()
        initListeners()
        observeState()
    }

    private fun initViews() {
        tvSelectedPath = findViewById(R.id.tvSelectedPath)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnStartCompress = findViewById(R.id.btnStartCompress)
        btnSelectVideo = findViewById(R.id.btnSelectVideo)
    }

    private fun initListeners() {
        btnSelectVideo.setOnClickListener {
            // 打开系统文件选择器，只显示视频
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            selectVideoLauncher.launch(intent)
        }

        btnStartCompress.setOnClickListener {
            currentVideoUri?.let { uri ->
                viewModel.compressVideo(uri) // 直接传 Uri
            }
        }
    }

    private fun observeState() {
        // 使用协程收集 ViewModel 的状态流
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CompressViewModel.UiState.Idle -> {
                        progressBar.visibility = View.GONE
                    }
                    is CompressViewModel.UiState.Compressing -> {
                        progressBar.visibility = View.VISIBLE
                        btnStartCompress.isEnabled = false
                        btnSelectVideo.isEnabled = false
                        tvStatus.text = "正在压缩中... (这是一个耗时操作)"
                    }
                    is CompressViewModel.UiState.Success -> {
                        progressBar.visibility = View.GONE
                        btnStartCompress.isEnabled = true
                        btnSelectVideo.isEnabled = true
                        tvStatus.text = "压缩成功！\n保存路径: ${state.outputPath}"
                        Toast.makeText(this@CompressActivity, "压缩成功！", Toast.LENGTH_LONG).show()
                    }
                    is CompressViewModel.UiState.Error -> {
                        progressBar.visibility = View.GONE
                        btnStartCompress.isEnabled = true
                        btnSelectVideo.isEnabled = true
                        tvStatus.text = "失败: ${state.message}"
                    }
                }
            }
        }
    }
}