package com.example.mediacraft.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mediacraft.data.local.dao.RecordDao
import com.example.mediacraft.data.local.entity.ProcessingRecord

@Database(entities = [ProcessingRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}