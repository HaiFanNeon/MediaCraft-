package com.example.mediacraft.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediacraft.data.local.dao.RecordDao
import com.example.mediacraft.data.local.entity.ProcessingRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val recordDao: RecordDao
) : ViewModel() {

    // 内部维护一个过滤状态：null 代表“全部”，其他代表具体的 taskType
    private val _filterType = MutableStateFlow<String?>(null)

    // 【核心黑科技】flatMapLatest
    // 这里的逻辑是：每当 _filterType 发生变化，就执行括号里的代码，返回一个新的 Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyList: StateFlow<List<ProcessingRecord>> = _filterType.flatMapLatest { type ->
        if (type == null) {
            // 如果没选分类，查所有
            recordDao.getAllRecords()
        } else {
            // 如果选了分类，查特定类型
            recordDao.getRecordsByType(type)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * UI层调用这个方法来切换分类
     * @param index Tab的索引 (0: 全部, 1: 视频压缩, 2: 音频提取)
     */
    fun setCategoryIndex(index: Int) {
        when (index) {
            0 -> _filterType.value = null // 全部
            1 -> _filterType.value = "Compression" // 对应 Repository 里存的字符串
            2 -> _filterType.value = "AudioExtraction" // 对应 Repository 里存的字符串
        }
    }

    fun deleteRecord(record: ProcessingRecord) {
        viewModelScope.launch {
            recordDao.delete(record)
        }
    }

    fun toggleFavorite(record: ProcessingRecord) {
        viewModelScope.launch {
            // 取反当前收藏状态
            val newRecord = record.copy(isFavorite = !record.isFavorite)
            recordDao.update(newRecord)
        }
    }
}