package com.example.mediacraft.ui.main

import android.os.Bundle
import android.view.View
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
        // 1. 修改跳转逻辑：使用 Navigation
        binding.cardCompress.setOnClickListener {
            // 原来是: startActivity(Intent(this, CompressActivity::class.java))
            // 现在是:
            findNavController().navigate(R.id.action_mainFragment_to_compressFragment)
        }

        // (稍后实现) 绑定提取音频按钮的跳转
        // binding.cardExtractAudio.setOnClickListener {
        //     findNavController().navigate(R.id.action_mainFragment_to_extractAudioFragment)
        // }

        // 2. 设置 RecyclerView (注意 Context 要用 requireContext())
        adapter = HistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
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
}