package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ItemJobBinding
class JobAdapter(private val jobs: List<Bai_Dang_CV>) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val baiDang = jobs[position]

        holder.binding.apply {
            tvJobTitle.text = baiDang.tieuDe ?: "Không có tiêu đề"
            tvJobDescription.text = baiDang.moTa ?: "Không có mô tả"
            tvJobEmail.text = baiDang.emailLienHe ?: "Không có email"
        }
    }

    override fun getItemCount(): Int = jobs.size
}

