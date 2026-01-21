package com.example.mediacraft.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mediacraft.data.local.entity.ProcessingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: ProcessingRecord)

    // 使用 Flow 返回数据，能够实现“数据库变动，UI自动刷新”，配合 LiveData/StateFlow 很好用
    @Query("SELECT * FROM processing_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ProcessingRecord>>
}