package com.example.mediacraft.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.example.mediacraft.R
import com.example.mediacraft.data.local.entity.ProcessingRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.chauthai.swipereveallayout.ViewBinderHelper
class HistoryAdapter : ListAdapter<ProcessingRecord, HistoryAdapter.ViewHolder>(DiffCallback) {

    private val viewBinderHelper = ViewBinderHelper().apply {
        setOpenOnlyOne(true)
    }

    // 可以在这里处理点击事件回调
    var onItemClick: ((ProcessingRecord) -> Unit)? = null
    var onDeleteClick: ((ProcessingRecord) -> Unit)? = null
    var onFavoriteClick: ((ProcessingRecord) -> Unit)? = null
    var onShareClick: ((ProcessingRecord) -> Unit)? = null


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPath: TextView = view.findViewById(R.id.tvPath)
        val swipeLayout: SwipeRevealLayout = view.findViewById(R.id.swipeLayout)
        val mainLayout: View = view.findViewById(R.id.mainLayout)
        val tvTaskType: TextView = view.findViewById(R.id.tvPath)
        val tvTime: TextView = view.findViewById(R.id.tvTime)

        // 侧滑菜单按钮
        val btnDelete: View = view.findViewById(R.id.btnDelete)
        val btnFavorite: View = view.findViewById(R.id.btnFavorite)
        val btnShare: View = view.findViewById(R.id.btnShare)
        val tvFavorite: TextView = view.findViewById(R.id.btnShare)
        val tvFavoriteMark: TextView = view.findViewById(R.id.tvFavoriteMark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        viewBinderHelper.bind(holder.swipeLayout, item.id.toString())


        // 将英文类型转换为中文显示
        holder.tvTaskType.text = when (item.taskType) {
            "Compression" -> "视频压缩"
            "AudioExtraction" -> "音频提取"
            else -> item.taskType // 未知类型显示原文
        }

        if (item.isFavorite) {
            holder.tvFavorite.text = "取消"
            holder.tvFavoriteMark.visibility = View.VISIBLE // 显示标题旁的小星星
        } else {
            holder.tvFavorite.text = "收藏"
            holder.tvFavoriteMark.visibility = View.GONE
        }

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

        holder.mainLayout.setOnClickListener {
            onItemClick?.invoke(item)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick?.invoke(item)
            holder.swipeLayout.close(true)
        }

        holder.btnFavorite.setOnClickListener {
            onFavoriteClick?.invoke(item)
            holder.swipeLayout.close(true)
        }
        holder.btnShare.setOnClickListener {
            onShareClick?.invoke(item)
            holder.swipeLayout.close(true)
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