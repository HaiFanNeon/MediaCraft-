package com.example.mediacraft.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.mediacraft.data.model.VideoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// @Inject 告诉 Hilt：如果别人需要 VideoRepository，你就帮我造一个
// @ApplicationContext 告诉 Hilt：请给我注入系统的 Context，我要用它查数据库
class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // suspend 表示这是一个耗时操作，必须在协程里跑，不会卡死主线程
    suspend fun getAllVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()

        // 1. 我们想查询哪些列的信息？
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA // DATA 存的是绝对路径
        )

        // 2. 排序方式：按添加时间倒序（最新的在最上面）
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        // 3. 开始查询
        // query() 就像去超市找货架，返回一个 cursor (游标/迭代器)
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null, // selection: null 表示查询所有视频，不筛选
            null,
            sortOrder
        )?.use { cursor ->
            // use {} 会自动关闭 cursor，防止内存泄漏

            // 获取每一列在数据库中的索引位置
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            // 4. 循环读取每一行数据
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn) ?: ""

                // 生成视频的 Uri 地址 (content://media/external/video/media/12345)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // 只有路径有效且文件大小大于0才添加，防止无效文件
                if (path.isNotEmpty() && size > 0) {
                    videoList.add(
                        VideoItem(
                            id = id,
                            uri = contentUri,
                            name = name,
                            duration = duration,
                            size = size,
                            path = path
                        )
                    )
                }
            }
        }
        // 返回最终列表
        return@withContext videoList
    }
}
