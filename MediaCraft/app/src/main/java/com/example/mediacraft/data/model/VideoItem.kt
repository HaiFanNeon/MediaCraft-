package com.example.mediacraft.data.model

import android.net.Uri

// 这是一个数据类，专门用来存放单个视频的信息
data class VideoItem(
    val id: Long,           // 视频在系统数据库里的 ID
    val uri: Uri,           // 视频的访问地址 (给播放器或图片加载库用的)
    val name: String,       // 视频文件名 (例如: my_cat.mp4)
    val duration: Long,     // 视频时长 (毫秒)
    val size: Long,         // 文件大小 (字节)
    val path: String        // 文件的绝对路径 (FFmpeg 压缩时需要这个路径)
)
