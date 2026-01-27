package com.example.mediacraft.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mediacraft.data.local.dao.RecordDao
import com.example.mediacraft.data.local.entity.ProcessingRecord

@Database(
    entities = [ProcessingRecord::class],
    version = 2, // 【修改1】版本号 +1
    exportSchema = true, // 【修改2】必须为 true
    autoMigrations = [
        // 【修改3】定义自动迁移路径：从 1 到 2
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}