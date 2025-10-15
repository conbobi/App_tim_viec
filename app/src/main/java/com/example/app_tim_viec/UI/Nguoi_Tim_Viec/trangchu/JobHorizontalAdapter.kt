package com.example.app_tim_viec.UI.Nguoi_Dung

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ItemJobHorizontalBinding

class JobHorizontalAdapter(
    private val jobs: List<Bai_Dang_CV>,
    private val onItemClick: (Bai_Dang_CV) -> Unit
) : RecyclerView.Adapter<JobHorizontalAdapter.JobViewHolder>() {

    inner class JobViewHolder(val binding: ItemJobHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Bai_Dang_CV) {
            // ✅ Ưu tiên lấy tiêu đề và mô tả từ thuộc tính chính (nếu rỗng thì fallback sang thongTinCoBan)
            val title = if (!job.tieuDe.isNullOrEmpty()) job.tieuDe else job.thongTinCoBan?.Tieu_De ?: "Không có tiêu đề"
            val moTa = if (!job.moTa.isNullOrEmpty()) job.moTa else job.thongTinCoBan?.Mo_Ta ?: "Không có mô tả"
            val email = if (!job.emailLienHe.isNullOrEmpty()) job.emailLienHe else job.thongTinCoBan?.Email_Lien_He ?: "Chưa cập nhật"
            val luong = job.thongTinChiTiet?.mucLuong ?: 0

            binding.tvTitle.text = title
            binding.tvMoTa.text = moTa
            binding.tvEmail.text = email
            binding.tvLuong.text = "$luong VND"

            // ✅ Lấy hình ảnh: ưu tiên hinhAnh trong Bai_Dang_CV, fallback sang thongTinChiTiet.anhCongViec
            val firstImage = job.hinhAnh?.firstOrNull()
                ?: job.thongTinChiTiet?.anhCongViec?.firstOrNull()

            if (!firstImage.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(firstImage)
                    .placeholder(R.drawable.sample_company)
                    .into(binding.imgJob)
            } else {
                binding.imgJob.setImageResource(R.drawable.sample_company)
            }

            // ✅ Sự kiện click
            binding.root.setOnClickListener { onItemClick(job) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJobHorizontalBinding.inflate(inflater, parent, false)
        return JobViewHolder(binding)
    }

    override fun getItemCount() = jobs.size

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }
}
