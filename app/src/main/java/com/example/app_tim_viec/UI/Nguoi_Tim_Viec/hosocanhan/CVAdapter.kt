    package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.example.app_tim_viec.databinding.ItemCvBinding // ⚠️ Đảm bảo bạn có file item_cv.xml

    class CVAdapter(
        private val cvList: List<CVModel>,
        private val onDeleteClick: (CVModel) -> Unit
    ) : RecyclerView.Adapter<CVAdapter.CVViewHolder>() {

        // Dùng ViewBinding cho ViewHolder
        inner class CVViewHolder(val binding: ItemCvBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CVViewHolder {
            val binding = ItemCvBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CVViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return cvList.size
        }

        override fun onBindViewHolder(holder: CVViewHolder, position: Int) {
            val cv = cvList[position]

            // Gán dữ liệu (từ file item_cv.xml)
            holder.binding.tvCVName.text = cv.fileName

            // Set sự kiện click cho nút xóa
            holder.binding.ivDeleteCV.setOnClickListener {
                onDeleteClick(cv)
            }
        }
    }