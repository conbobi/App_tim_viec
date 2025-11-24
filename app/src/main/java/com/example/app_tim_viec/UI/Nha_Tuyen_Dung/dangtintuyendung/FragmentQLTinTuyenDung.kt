package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.app.AlertDialog
import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory.BaiDangRepository
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.app_tim_viec.databinding.FragmentQlTinTuyenDungBinding
import com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung.FragmentTaoTinMoi
import com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung.Fragment_TT_Chi_Tiet_CV
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class FragmentQLTinTuyenDung : Fragment() {

    private var _binding: FragmentQlTinTuyenDungBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private val jobs = mutableListOf<Bai_Dang_CV>() // danh sách job mẫu
    private val repository = BaiDangRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FragmentQLTinTuyenDung", "onCreate: Fragment được khởi tạo")
    }

    override fun onResume() {
        super.onResume()
        Log.d("FragmentQLTinTuyenDung", "onResume: Fragment hiển thị lên màn hình")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQlTinTuyenDungBinding.inflate(inflater, container, false)
        val view = binding.root

        // ✅ Khởi tạo Adapter
        adapter = JobAdapter(
            jobs,
            onItemClick = { baiDang ->
                // Khi click vào 1 job -> mở Fragment chi tiết
                val fragment = Fragment_TT_Chi_Tiet_CV()
                val bundle = Bundle().apply {
                    putSerializable("baiDang", baiDang)
                }
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { baiDang ->
                // Khi click nút xóa
                showDeleteDialog(baiDang)
            }
        )

        // ✅ Cấu hình RecyclerView
        binding.recyclerJobs.adapter = adapter
        binding.recyclerJobs.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Nút thêm bài đăng mới
        binding.btnAddPost.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentTaoTinMoi())
                .addToBackStack(null)
                .commit()
        }

        Log.d("FragmentQLTinTuyenDung", "onCreateView: Bắt đầu load dữ liệu...")
        loadDataFromRepository(view)

        return view
    }

    private fun showDeleteDialog(baiDang: Bai_Dang_CV) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa bài đăng: ${baiDang.tieuDe}?")
            .setPositiveButton("Xóa") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repository.deleteBaiDang(baiDang.maBaiDang?:"") // 🔴 gọi xóa trong Firestore
                        jobs.remove(baiDang)
                        adapter.notifyDataSetChanged()
                        Snackbar.make(binding.root, "Đã xóa bài đăng", Snackbar.LENGTH_SHORT).show()
                        Log.d("FragmentQLTinTuyenDung", "Đã xóa bài đăng khỏi Firestore: ${baiDang.maBaiDang}")
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Lỗi khi xóa: ${e.message}", Snackbar.LENGTH_LONG).show()
                        Log.e("FragmentQLTinTuyenDung", "Xóa thất bại", e)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }


    private fun loadDataFromRepository(rootView: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Snackbar.make(rootView, "Bạn chưa đăng nhập!", Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                val list = repository.getBaiDangTheoNhaTuyenDung(currentUser.uid)
                jobs.clear()
                jobs.addAll(list)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Snackbar.make(rootView, "Lỗi tải dữ liệu: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



