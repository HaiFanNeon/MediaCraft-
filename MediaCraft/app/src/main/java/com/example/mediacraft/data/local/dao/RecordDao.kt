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

    // 查询所有
    @Query("SELECT * FROM processing_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ProcessingRecord>>

    // 【新增】按类型查询 (例如只查 "Compression" 或 "AudioExtraction")
    @Query("SELECT * FROM processing_records WHERE taskType = :type ORDER BY timestamp DESC")
    fun getRecordsByType(type: String): Flow<List<ProcessingRecord>>
}