package com.example.mediacraft.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.R
import androidx.appcompat.app.AppCompatActivity
import com.example.mediacraft.databinding.ActivityMainBinding
import com.example.mediacraft.ui.compress.CompressActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Activity 必须加这个注解，才能注入 ViewModel
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btn = binding.btn
        btn.setOnClickListener {
            val intent1 = Intent(this, CompressActivity::class.java)
            startActivity(intent1)
        }
    }
}