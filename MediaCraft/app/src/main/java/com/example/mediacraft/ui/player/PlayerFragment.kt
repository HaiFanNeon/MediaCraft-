package com.example.mediacraft.ui.player

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mediaPath = it.getString("mediaPath")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)
        binding.btnClose.setOnClickListener {
            // 返回上一页
            findNavController().navigateUp()
        }

        initializePlayer()

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