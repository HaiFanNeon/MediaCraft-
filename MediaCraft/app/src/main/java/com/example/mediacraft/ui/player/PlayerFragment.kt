package com.example.mediacraft.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.example.mediacraft.R
import com.example.mediacraft.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding : FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var player : ExoPlayer? = null
    private var mediaPath : String? = null

    private var isLandscape = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mediaPath = it.getString("mediaPath")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        initializePlayer()
        binding.btnClose.setOnClickListener {
            // 返回上一页
            findNavController().navigateUp()
        }

        binding.btnFullscreen.setOnClickListener {
            if (isLandscape) {
                requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            } else {
                requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }
        }

    }

    /**
     * 辅助方法：请求改变屏幕方向
     */
    private fun requestedOrientation(orientation: Int) {
        requireActivity().requestedOrientation = orientation
    }

    /**
     * 【关键】监听屏幕配置变化（由系统自动触发，或者我们要手动调用）
     * 因为我们在 Manifest 里配置了 configChanges，所以旋转时不会走 onDestroy，而是走这里
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // 判断当前真正变成了什么方向
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
            enterFullscreenMode()
        } else {
            isLandscape = false
            exitFullscreenMode()
        }
    }

    /**
     * 进入沉浸式全屏 (隐藏系统栏)
     */
    private fun enterFullscreenMode() {
        // 隐藏 Status Bar 和 Navigation Bar
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 隐藏我们自己写的按钮 (可选，看需求)
        // binding.btnClose.visibility = View.GONE
    }

    /**
     * 退出全屏 (显示系统栏)
     */
    private fun exitFullscreenMode() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())

        // binding.btnClose.visibility = View.VISIBLE
    }



    private fun initializePlayer() {
        if (mediaPath.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "文件路径无效", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(mediaPath!!)
        if (!file.exists()) {
            Toast.makeText(requireContext(), "文件不存在", Toast.LENGTH_SHORT).show()
            return
        }

        player = ExoPlayer.Builder(requireContext()).build()

        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player?.setMediaItem(mediaItem)

        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onStart() {
        super.onStart()
        if (null == player) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}