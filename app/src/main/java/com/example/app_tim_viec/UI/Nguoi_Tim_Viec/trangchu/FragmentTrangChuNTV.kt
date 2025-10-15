package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Dung.JobHorizontalAdapter
import com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung.Fragment_TT_Chi_Tiet_CV
import com.example.app_tim_viec.databinding.FragmentTrangChuNtvBinding
import com.google.firebase.firestore.FirebaseFirestore

class FragmentTrangChuNTV : Fragment() {

    private var _binding: FragmentTrangChuNtvBinding? = null
    private val binding get() = _binding!!
    private val TAG = "TrangChuNTV"
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrangChuNtvBinding.inflate(inflater, container, false)

        // Thiết lập menu dưới cùng
        setupBottomMenu()

        // Thiết lập layout cho RecyclerView
        setupRecyclerViews()

        // Tải dữ liệu việc làm
        loadJobsFromFirestore()

        return binding.root
    }

    private fun setupBottomMenu() {
        // ✅ Chống null context bằng requireContext()
        binding.ivHoSo.setOnClickListener {
            if (isAdded && context != null) {
                val intent = android.content.Intent(requireContext(), com.example.app_tim_viec.UI.hosocanhan.ActivityHoSoNTV::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }

        binding.ivChat.setOnClickListener {
            if (isAdded && context != null) {
                val intent = android.content.Intent(requireContext(), com.example.app_tim_viec.UI.Nguoi_Tim_Viec.tinnhan.ActivityTinNhan::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.recyclerTopJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerSuggestedJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerAllJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun loadJobsFromFirestore() {
        db.collection("BaiDangCV")
            .get()
            .addOnSuccessListener { snapshot ->
                // 🔒 Kiểm tra fragment còn hoạt động không
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val jobs = snapshot.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val job = Bai_Dang_CV()

                        // Gán ID nếu chưa có
                        job.maBaiDang = doc.id

                        // Gán dữ liệu an toàn
                        job.tieuDe = data["tieuDe"] as? String
                        job.moTa = data["moTa"] as? String
                        job.tieuDe = data["tenCongTy"] as? String
                        job.thongTinCoBan = data["mucLuong"] as? String
                        job.hinhAnh = data["hinhAnh"] as? String


                        // ✅ Xử lý đặc biệt cho trường thời gian
                        val tg = data["thoiGianHetHan"]
                        job.thoiGianHetHan = when (tg) {
                            is com.google.firebase.Timestamp -> tg.toDate().time // convert Timestamp → Long
                            is Long -> tg
                            else -> null
                        }

                        job
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Lỗi khi parse BaiDangCV: ${e.message}")
                        null
                    }
                }

                Log.d(TAG, "✅ Lấy được ${jobs.size} việc từ Firestore")

                if (jobs.isEmpty()) {
                    Log.w(TAG, "⚠️ Không có dữ liệu việc làm trong Firestore!")
                    return@addOnSuccessListener
                }

                // Phân loại dữ liệu
                val topJobs = jobs.take(5)
                val suggestedJobs = jobs.shuffled().take(10)

                // Thiết lập adapter
                binding.recyclerTopJobs.adapter =
                    JobHorizontalAdapter(topJobs) { job -> openChiTiet(job) }

                binding.recyclerSuggestedJobs.adapter =
                    JobHorizontalAdapter(suggestedJobs) { job -> openChiTiet(job) }

                binding.recyclerAllJobs.adapter =
                    JobHorizontalAdapter(jobs) { job -> openChiTiet(job) }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Lỗi lấy BaiDangCV: ${e.message}", e)
            }
    }


    private fun openChiTiet(job: Bai_Dang_CV) {
        // 🔒 Tránh crash khi fragment không còn
        if (!isAdded || context == null) return

        val fragment = Fragment_TT_Chi_Tiet_CV()
        val bundle = Bundle().apply {
            putSerializable("baiDang", job)
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss() // ✅ Dùng commitAllowingStateLoss để tránh crash khi Activity bị destroy
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
