package com.example.mediacraft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 记录视频处理历史的表
 * @Entity 注解表示这是一张数据库表
 */
@Entity(tableName = "processing_records")
data class ProcessingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,   // 原视频路径
    val outputPath: String,     // 输出视频路径
    val taskType: String,       // 任务类型：压缩、转码、静音等
    val status: Int,            // 状态：0-进行中, 1-成功, 2-失败
    val timestamp: Long = System.currentTimeMillis() // 创建时间
)