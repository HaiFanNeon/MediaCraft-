package com.example.mediacraft.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.R
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediacraft.databinding.ActivityMainBinding
import com.example.mediacraft.ui.compress.CompressActivity
import com.example.mediacraft.ui.main.HistoryAdapter
import com.example.mediacraft.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // Activity 必须加这个注解，才能注入 ViewModel
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        observeData()
    }

    private fun initView() {
        val cardCompress = binding.cardCompress
        cardCompress.setOnClickListener {
            startActivity(Intent(this, CompressActivity::class.java))
        }

        val recyclerView = binding.recyclerView
        adapter = HistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.historyList.collect { list ->
                adapter.submitList(list)
            }
        }
    }
}