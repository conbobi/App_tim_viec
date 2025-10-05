    package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

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

            // RecyclerView
            adapter = JobAdapter(jobs) { baiDang ->
                // Khi click vào 1 job -> mở Fragment chi tiết
                val fragment = Fragment_TT_Chi_Tiet_CV()
                val bundle = Bundle().apply {
                    putSerializable("baiDang", baiDang) // truyền object
                }
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment) // container trong MainActivity
                    .addToBackStack(null)
                    .commit()
            }
            binding.recyclerJobs.adapter = adapter
            binding.recyclerJobs.layoutManager = LinearLayoutManager(requireContext())

            // Nút thêm bài đăng mới
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

        private fun loadDataFromRepository(rootView: View) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    Log.d("FragmentQLTinTuyenDung", "loadDataFromRepository: Gọi repository...")
                    val list = repository.getAllBaiDang() // Gọi suspend fun
                    Log.d("FragmentQLTinTuyenDung", "loadDataFromRepository: Lấy được ${list.size} bài đăng")

                    list.forEachIndexed { index, baiDang ->
                Log.d("FragmentQLTinTuyenDung", "Job[$index] = ${baiDang.tieuDe}, ${baiDang.moTa}, ${baiDang.emailLienHe}")
                    }

                    jobs.clear()
                    jobs.addAll(list)
                    adapter.notifyDataSetChanged()

                    Log.d("FragmentQLTinTuyenDung", "RecyclerView đã cập nhật với ${jobs.size} items")
                } catch (e: Exception) {
                    Log.e("FragmentQLTinTuyenDung", "Lỗi tải dữ liệu", e)
                    Snackbar.make(rootView, "Lỗi tải dữ liệu: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }



