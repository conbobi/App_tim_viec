package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_tim_viec.R
import android.util.Log
class AnhSliderAdapter(
    private val dsAnh: List<String>
) : RecyclerView.Adapter<AnhSliderAdapter.AnhViewHolder>() {

    inner class AnhViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgSlider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnhViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anh_slider, parent, false)
        return AnhViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnhViewHolder, position: Int) {
        val url = dsAnh[position]
        Log.d("AnhSliderAdapter", "Đang tải ảnh ở vị trí $position: $url")

        Glide.with(holder.itemView.context)
            .load(url)
            .placeholder(R.drawable.ic_image_placeholder)
            .fitCenter() // ✅ hiển thị đúng tỉ lệ, không cắt ảnh
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = dsAnh.size
}
