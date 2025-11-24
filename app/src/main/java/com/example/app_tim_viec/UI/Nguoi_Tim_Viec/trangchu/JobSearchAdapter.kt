package com.example.app_tim_viec.UI.Nguoi_Dung

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ItemJobSearchBinding

class JobSearchAdapter(
    private var jobs: MutableList<Bai_Dang_CV>,
    private val onItemClick: (Bai_Dang_CV) -> Unit
) : RecyclerView.Adapter<JobSearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(private val binding: ItemJobSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Bai_Dang_CV) {
            binding.tvTitle.text = job.tieuDe ?: "Tiêu đề đang cập nhật"
            binding.tvCompany.text = job.tenCongTy ?: "Công ty đang cập nhật"

            val luong = job.thongTinChiTiet?.mucLuong ?: 0
            binding.tvLuong.text = if (luong > 0) String.format("%,d VND", luong) else "Thỏa thuận"

            val imgUrl = job.hinhAnh?.firstOrNull()
            if (!imgUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(imgUrl)
                    .placeholder(R.drawable.sample_company)
                    .error(R.drawable.sample_company)
                    .into(binding.imgJob)
            } else {
                binding.imgJob.setImageResource(R.drawable.sample_company)
            }

            // CLICK → gửi job.maBaiDang
            binding.root.setOnClickListener {
                Log.d("JobSearchAdapter", "Clicked job: ${job.tieuDe} | ID=${job.maBaiDang}")
                try {
                    onItemClick(job)
                    Log.d("JobSearchAdapter", "onItemClick callback được gọi thành công")
                } catch (e: Exception) {
                    Log.e("JobSearchAdapter", "Lỗi khi gọi callback: ${e.message}", e)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemJobSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount() = jobs.size

    // hàm dùng để load dữ liêu vào biến qua trang chi tiết vi
    fun updateData(newJobs: List<Bai_Dang_CV>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }
}