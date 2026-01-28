package com.example.mediacraft.utils

/**
 * 全局常量管理对象
 */
object AppConstants {
    // 数据库中存储的任务类型 Key
    const val TASK_TYPE_COMPRESS = "Compression"
    const val TASK_TYPE_EXTRACT_AUDIO = "AudioExtraction"

    // 状态 Key (假设你用了 int，也可以定义在这里方便阅读)
    const val STATUS_PROCESSING = 0
    const val STATUS_SUCCESS = 1
    const val STATUS_FAILURE = 2
}