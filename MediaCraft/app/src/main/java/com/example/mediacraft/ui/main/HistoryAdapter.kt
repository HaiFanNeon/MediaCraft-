package com.example.mediacraft.ui.main

import android.content.Context
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
import com.example.mediacraft.utils.AppConstants

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
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        // 侧滑菜单按钮
        val btnDelete: View = view.findViewById(R.id.btnDelete)
        val btnFavorite: View = view.findViewById(R.id.btnFavorite)
        val btnShare: View = view.findViewById(R.id.btnShare)

        // ✅ 修正：指向 XML 中显示“收藏”文字的 TextView ID
        val tvFavorite: TextView = view.findViewById(R.id.tvFavorite)

        val tvFavoriteMark: TextView = view.findViewById(R.id.tvFavoriteMark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        viewBinderHelper.bind(holder.swipeLayout, item.id.toString())


        // 把 Compression 等英文变成中文
        holder.tvTaskType.text = when (item.taskType) {
            AppConstants.TASK_TYPE_COMPRESS -> context.getString(R.string.task_name_compress)
            AppConstants.TASK_TYPE_EXTRACT_AUDIO -> context.getString(R.string.task_name_audio)
            else -> item.taskType // 未知类型兜底
        }

        // 处理收藏按钮文字
        if (item.isFavorite) {
            holder.tvFavorite.text = context.getString(R.string.btn_unfavorite) // "取消"
            holder.tvFavoriteMark.visibility = View.VISIBLE
        } else {
            holder.tvFavorite.text = context.getString(R.string.btn_favorite) // "收藏"
            holder.tvFavoriteMark.visibility = View.GONE
        }

        holder.tvPath.text = "输出: ${item.outputPath}"

        // 格式化时间
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(item.timestamp))

        // 处理状态颜色 (文字也可以提取到 strings.xml)
        if (item.status == AppConstants.STATUS_SUCCESS) {
            holder.tvStatus.text = context.getString(R.string.status_success) // "处理成功"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.text = context.getString(R.string.status_failure) // "处理失败"
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
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