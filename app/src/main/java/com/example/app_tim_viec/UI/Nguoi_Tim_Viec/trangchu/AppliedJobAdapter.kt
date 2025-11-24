package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_tim_viec.Data.Nguoi_Tim_Viec.DonDaUngTuyen
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ItemJobAppliedBinding // Đảm bảo ViewBinding bật

class AppliedJobAdapter(
    private var list: List<DonDaUngTuyen>
) : RecyclerView.Adapter<AppliedJobAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemJobAppliedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemJobAppliedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvTitle.text = item.tieuDeBaiDang
        holder.binding.tvCompany.text = item.tenCongTy
        holder.binding.tvStatus.text = "Trạng thái: ${item.trangThai}"

        // Đổi màu trạng thái
        when (item.trangThai) {
            "đã duyệt" -> holder.binding.tvStatus.setTextColor(Color.GREEN)
            "từ chối" -> holder.binding.tvStatus.setTextColor(Color.RED)
            else -> holder.binding.tvStatus.setTextColor(Color.parseColor("#FF9800"))
        }

        if (item.logoCongTy.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(item.logoCongTy).into(holder.binding.imgJob)
        }
    }

    override fun getItemCount() = list.size
}