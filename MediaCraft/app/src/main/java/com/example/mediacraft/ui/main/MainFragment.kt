package com.example.mediacraft.ui.main
import com.google.android.material.tabs.TabLayout
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediacraft.R
import com.example.mediacraft.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// 使用 ViewBinding 可以让代码更整洁 (推荐在 build.gradle 开启 viewBinding)
@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        initView()
        observeData()
    }

    private fun initView() {
        binding.cardCompress.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_compressFragment)
        }

        binding.cardExtractAudio.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_extractAudioFragment)
        }
        
        adapter = HistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        adapter.onDeleteClick = {
            record ->
            viewModel.deleteRecord(record)
        }
        adapter.onFavoriteClick = { record ->
            viewModel.toggleFavorite(record)
        }

        adapter.onShareClick = { record ->
            shareFile(record.outputPath)
        }

        // TabLayout 监听
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 当 tab 被选中，通知 ViewModel 切换查询条件
                tab?.position?.let { index ->
                    viewModel.setCategoryIndex(index)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 处理点击事件
        adapter.onItemClick = { record ->
            // record.outputPath 是我们存进数据库的绝对路径
            // record.taskType 可以帮我们判断是视频还是音频 (目前播放器通用，暂不需要特殊处理)

            // 使用 Bundle 传递参数
            val bundle = Bundle().apply {
                putString("mediaPath", record.outputPath)
                // 简单的逻辑判断类型
                val type = if (record.taskType.contains("Audio")) "audio" else "video"
                putString("mediaType", type)
            }
            // 跳转
            findNavController().navigate(R.id.action_mainFragment_to_playerFragment, bundle)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyList.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 防止内存泄漏
    }

    // 辅助方法：调用系统分享
    private fun shareFile(path: String) {
        val file = java.io.File(path)
        if (!file.exists()) {
            Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用 FileProvider 分享文件 (这里为了简便演示，假设是 Android 7.0 以下或已配置 FileProvider)
        // 实习生阶段：先用简单方式，真正上线需要配置 FileProvider
        // 这里只是演示 Intent 逻辑
        /*
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*" // 或者 */*
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "分享到"))
        */
        Toast.makeText(requireContext(), "点击了分享 (请完善 FileProvider)", Toast.LENGTH_SHORT).show()
    }
}