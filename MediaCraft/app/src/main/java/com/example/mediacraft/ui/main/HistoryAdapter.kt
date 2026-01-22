package com.example.mediacraft.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediacraft.R
import com.example.mediacraft.data.local.entity.ProcessingRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ProcessingRecord, HistoryAdapter.ViewHolder>(DiffCallback) {

    // 可以在这里处理点击事件回调
    var onItemClick: ((ProcessingRecord) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPath: TextView = view.findViewById(R.id.tvPath)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvTaskType.text = item.taskType
        holder.tvPath.text = "输出: ${item.outputPath}"

        // 格式化时间
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(item.timestamp))

        // 处理状态颜色
        if (item.status == 1) {
            holder.tvStatus.text = "处理成功"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // 绿色
        } else {
            holder.tvStatus.text = "处理失败"
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // 红色
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    // DiffUtil 用于计算列表差异，实现高效刷新
    object DiffCallback : DiffUtil.ItemCallback<ProcessingRecord>() {
        override fun areItemsTheSame(oldItem: ProcessingRecord, newItem: ProcessingRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProcessingRecord, newItem: ProcessingRecord): Boolean {
            return oldItem == newItem
        }
    }
}