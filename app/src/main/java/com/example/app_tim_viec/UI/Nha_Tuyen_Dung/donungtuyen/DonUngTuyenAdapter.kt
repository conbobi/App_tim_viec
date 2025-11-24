package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.donungtuyen

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUngTuyen
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.TrangThaiDon
import com.example.app_tim_viec.R

class DonUngTuyenAdapter(
    private var danhSach: List<DonUngTuyen>,
    private val onActionClick: (DonUngTuyen, String) -> Unit
) : RecyclerView.Adapter<DonUngTuyenAdapter.ViewHolder>() { // Dùng RecyclerView.Adapter

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ view theo layout item_don_ung_tuyen.xml MỚI
        val tvTen: TextView = itemView.findViewById(R.id.tvTenUngVien)
        val tvViTri: TextView = itemView.findViewById(R.id.tvViTriUngTuyen)
        val tvTrangThai: TextView = itemView.findViewById(R.id.tvTrangThai)

        val btnXemCV: Button = itemView.findViewById(R.id.btnXemCV)
        val btnEmail: ImageView = itemView.findViewById(R.id.btnEmail)
        val btnChat: ImageView = itemView.findViewById(R.id.btnChat)
        val btnDuyet: Button = itemView.findViewById(R.id.btnDuyet)
        val btnTuChoi: Button = itemView.findViewById(R.id.btnTuChoi)

        fun bind(don: DonUngTuyen) {
            tvTen.text = don.tenNguoiTV ?: "Ứng viên ẩn danh"
            tvViTri.text = don.tieuDe
            tvTrangThai.text = don.trangThai

            // Đổi màu trạng thái
            when(don.trangThai) {
                TrangThaiDon.DA_DUYET.value -> tvTrangThai.setTextColor(Color.GREEN)
                TrangThaiDon.TU_CHOI.value -> tvTrangThai.setTextColor(Color.RED)
                else -> tvTrangThai.setTextColor(Color.parseColor("#FFA500"))
            }

            // Click events
            btnXemCV.setOnClickListener { onActionClick(don, "VIEW_CV") }
            btnEmail.setOnClickListener { onActionClick(don, "MAIL") }
            btnChat.setOnClickListener { onActionClick(don, "CHAT") }
            btnDuyet.setOnClickListener { onActionClick(don, "APPROVE") }
            btnTuChoi.setOnClickListener { onActionClick(don, "REJECT") }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_don_ung_tuyen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(danhSach[position])
    }

    override fun getItemCount() = danhSach.size

    fun updateData(newList: List<DonUngTuyen>) {
        danhSach = newList
        notifyDataSetChanged()
    }
}