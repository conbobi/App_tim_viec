package com.example.app_tim_viec.UI.Nguoi_Dung

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ItemJobHorizontalBinding

class JobHorizontalAdapter(
    private var jobs: MutableList<Bai_Dang_CV>, // đổi từ List -> MutableList
    private val onItemClick: (Bai_Dang_CV) -> Unit
) : RecyclerView.Adapter<JobHorizontalAdapter.JobViewHolder>() {

    inner class JobViewHolder(private val binding: ItemJobHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Bai_Dang_CV) {
            val title = job.tieuDe?:"".ifEmpty { "Không có tiêu đề" }
            val moTa = job.moTa?:"".ifEmpty { "Không có mô tả" }
            val email = job.emailLienHe?:"".ifEmpty { "Chưa cập nhật email" }
            val luongValue = job.thongTinChiTiet?.mucLuong ?: 0
            val company = job.tenCongTy?:"".ifEmpty { "chưa có tên cty" }

            binding.tvTitle.text = title
            binding.tvMoTa.text = moTa
            binding.tvEmail.text = email
            binding.tvLuong.text = if (luongValue > 0) {
                String.format("%,d VND", luongValue)
            } else {
                "Thỏa thuận"
            }
            binding.tvCompany.text = company

            val imageUrl = job.hinhAnh?.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.sample_company)
                    .into(binding.imgJob)
            } else {
                binding.imgJob.setImageResource(R.drawable.sample_company)
            }

            binding.root.setOnClickListener { onItemClick(job) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJobHorizontalBinding.inflate(inflater, parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount() = jobs.size

    // 🔥 Hàm update data khi tìm kiếm
    fun updateData(newJobs: List<Bai_Dang_CV>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }
}
