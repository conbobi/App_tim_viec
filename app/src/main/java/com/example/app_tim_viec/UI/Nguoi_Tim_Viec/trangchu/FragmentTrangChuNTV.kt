package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.DoTuoi
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.TT_Chi_Tiet_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.thoiGianLamViec
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Dung.JobHorizontalAdapter
// 🔥 Import Adapter tìm kiếm mới (đảm bảo package đúng với nơi bạn tạo file Adapter)
import com.example.app_tim_viec.UI.Nguoi_Dung.JobSearchAdapter

import com.example.app_tim_viec.databinding.FragmentTrangChuNtvBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class FragmentTrangChuNTV : Fragment() {

    private var _binding: FragmentTrangChuNtvBinding? = null
    private val binding get() = _binding!!
    private val TAG = "TrangChuNTV"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // search vars
    private var fullJobList = mutableListOf<Bai_Dang_CV>()
    private var filteredJobList = mutableListOf<Bai_Dang_CV>()

    // 🔥 Biến cho adapter tìm kiếm
    private lateinit var searchAdapter: JobSearchAdapter
    private var searchList = mutableListOf<Bai_Dang_CV>()

    // Biến lưu danh sách ngành nghề user quan tâm
    private var userInterestedIndustries: List<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrangChuNtvBinding.inflate(inflater, container, false)

        setupBottomMenu()

        // 🔥 Gọi hàm setup recyclerview (bao gồm cả cái tìm kiếm)
        setupRecyclerViews()

        // Load sở thích trước -> rồi mới load Job
        loadUserInterests()

        // 🔥 Gọi hàm setup tìm kiếm (xử lý ẩn hiện nút bấm)
        setupSearch()

        return binding.root
    }

    // 1. Load ngành nghề quan tâm
    private fun loadUserInterests() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            loadJobsFromFirestore()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileDetail = document.get("profileDetail") as? HashMap<String, Any>
                    if (profileDetail != null) {
                        val list = profileDetail["nganhNgheQuanTam"] as? List<String>
                        if (list != null) {
                            userInterestedIndustries = list
                        }
                    }
                }
                loadJobsFromFirestore()
            }
            .addOnFailureListener {
                loadJobsFromFirestore()
            }
    }

    // 2. Load Job từ Firestore
    private fun loadJobsFromFirestore() {
        db.collection("BaiDangCV")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
                fullJobList.clear()

                for (doc in snapshot) {
                    val job = Bai_Dang_CV()
                    job.maBaiDang = doc.id
                    val data = doc.data

                    job.hinhAnh = when (val img = data["hinhAnh"]) {
                        is String -> listOf(img)
                        is List<*> -> img.filterIsInstance<String>()
                        else -> emptyList()
                    }

                    val maNTD = data["maNhaTuyenDung"] as? String ?: ""
                    job.maNhaTuyenDung = maNTD

                    val coBanTask = db.collection("BaiDangCV").document(doc.id).collection("thongTinCoBan").get()
                    val chiTietTask = db.collection("BaiDangCV").document(doc.id).collection("thongTinChiTiet").get()

                    val congTyTask = if (maNTD.isNotEmpty()) {
                        db.collection("users").document(maNTD).get()
                    } else {
                        null
                    }

                    val subTaskList = mutableListOf<com.google.android.gms.tasks.Task<*>>()
                    subTaskList.add(coBanTask)
                    subTaskList.add(chiTietTask)
                    if (congTyTask != null) subTaskList.add(congTyTask)

                    val processingTask = com.google.android.gms.tasks.Tasks.whenAllComplete(subTaskList)
                        .continueWith { task ->
                            if (coBanTask.isSuccessful && !coBanTask.result.isEmpty) {
                                for (subDoc in coBanTask.result) {
                                    val subData = subDoc.data
                                    job.tieuDe = subData["Tieu_De"] as? String ?: ""
                                    job.moTa = subData["Mo_Ta"] as? String ?: ""
                                    job.emailLienHe = subData["Email_Lien_He"] as? String ?: ""
                                }
                            }

                            if (chiTietTask.isSuccessful && !chiTietTask.result.isEmpty) {
                                for (ctDoc in chiTietTask.result) {
                                    val ctData = ctDoc.data
                                    job.thongTinChiTiet = TT_Chi_Tiet_CV(
                                        diaChiNoiLamViec = ctData["Dia_Chi_Noi_Lam_Viec"] as? String ?: "",
                                        tinhThanhPho = ctData["Tinh_Thanh_Pho"] as? String ?: "",
                                        chucVu = ctData["Chuc_Vu"] as? String ?: "",
                                        mucLuong = (ctData["mucLuong"] as? Number)?.toInt() ?: 0,
                                        soLuongCanTuyen = (ctData["So_Luong_Can_Tuyen"] as? Long)?.toInt() ?: 0,
                                        tinhChatCongViec = ctData["Tinh_Chat_Cong_Viec"] as? String ?: "",
                                        ngonNguYeuCau = ctData["Ngon_Ngu_Yeu_Cau"] as? String ?: "",
                                        thoiGianLamViec = thoiGianLamViec(
                                            tu = ctData["Thoi_Gian_Bat_Dau"] as? String ?: "",
                                            den = ctData["Thoi_Gian_Ket_Thuc"] as? String ?: ""
                                        ),
                                        kyNangChuyenMon = (ctData["Ky_Nang_Chuyen_Mon"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                        chungChiYeuCau = (ctData["Chung_Chi_Yeu_Cau"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                        yeuCauGioiTinh = ctData["Yeu_Cau_Gioi_Tinh"] as? String ?: "",
                                        gioLamViec = ctData["Gio_Lam_Viec"] as? String ?: "",
                                        yeuCauDoTuoi = DoTuoi(
                                            min = (ctData["Do_Tuoi_Min"] as? Long)?.toInt() ?: 0,
                                            max = (ctData["Do_Tuoi_Max"] as? Long)?.toInt() ?: 0
                                        ),
                                        anhCongViec = (ctData["Anh_Cong_Viec"] as? List<*>)?.filterIsInstance<String>()
                                    )
                                }
                            }

                            if (congTyTask != null && congTyTask.isSuccessful && congTyTask.result.exists()) {
                                val userDoc = congTyTask.result
                                val tenCty = userDoc.getString("companyName")
                                    ?: userDoc.getString("TenCongTy")
                                    ?: userDoc.getString("hoTen")
                                    ?: "Công ty chưa cập nhật tên"
                                job.tenCongTy = tenCty
                            } else {
                                job.tenCongTy = if(job.tenCongTy.isNullOrEmpty()) "Đang cập nhật" else job.tenCongTy
                            }

                            synchronized(fullJobList) {
                                fullJobList.add(job)
                            }
                            null
                        }
                    tasks.add(processingTask)
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        filteredJobList = fullJobList.toMutableList()

                        // Lọc việc gấp
                        val viecGapList = fullJobList.filter { job ->
                            val title = (job.tieuDe ?: "").lowercase().removeVietnameseAccent()
                            title.contains("gap")
                        }.toMutableList()

                        android.util.Log.d(TAG, "Tổng số job load được: ${fullJobList.size}")

                        // Set adapter
                        binding.recyclerAllJobs.adapter = JobHorizontalAdapter(viecGapList) { job -> openChiTiet(job) }
                        binding.recyclerTopJobs.adapter = JobHorizontalAdapter(fullJobList.toMutableList()) { job -> openChiTiet(job) }

                        val suggestedList = filterSuggestedJobs()
                        binding.recyclerSuggestedJobs.adapter = JobHorizontalAdapter(suggestedList) { job -> openChiTiet(job) }
                    }
            }
    }

    // 3. Hàm lọc gợi ý
    private fun filterSuggestedJobs(): MutableList<Bai_Dang_CV> {
        if (userInterestedIndustries.isEmpty()) {
            return fullJobList.shuffled().take(10).toMutableList()
        }
        val matchingJobs = mutableListOf<Bai_Dang_CV>()
        for (job in fullJobList) {
            val title = (job.tieuDe ?: "").lowercase().removeVietnameseAccent()
            val position = (job.thongTinChiTiet?.chucVu ?: "").lowercase().removeVietnameseAccent()
            val desc = (job.moTa ?: "").lowercase().removeVietnameseAccent()

            for (interest in userInterestedIndustries) {
                val interestClean = interest.lowercase().removeVietnameseAccent()
                if (title.contains(interestClean) || position.contains(interestClean) || desc.contains(interestClean)) {
                    matchingJobs.add(job)
                    break
                }
            }
        }
        return if (matchingJobs.isEmpty()) {
            fullJobList.shuffled().take(10).toMutableList()
        } else {
            matchingJobs.shuffled().take(10).toMutableList()
        }
    }

    private fun setupBottomMenu() {
        binding.ivHoSo.setOnClickListener {
            if (isAdded) {
                val intent = android.content.Intent(requireContext(), com.example.app_tim_viec.UI.hosocanhan.ActivityHoSoNTV::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
        binding.ivChat.setOnClickListener {
            if (isAdded) {
                val intent = android.content.Intent(requireContext(), com.example.app_tim_viec.UI.Nguoi_Tim_Viec.tinnhan.ActivityTinNhan::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
        binding.ivTienIch.setOnClickListener {
            val intent = android.content.Intent(requireContext(), com.example.app_tim_viec.UI.Nguoi_Tim_Viec.donungtuyen.ActivityDonUngTuyenNTV::class.java)
            startActivity(intent)
        }
    }

    // 🔥 HÀM SETUP RECYCLERVIEW (Đã gộp làm 1, xóa cái bị trùng)
    private fun setupRecyclerViews() {
        // Setup các recycler cũ (Ngang)
        binding.recyclerTopJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerSuggestedJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Việc tuyển gấp (Ngang)
        binding.recyclerAllJobs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 🔥 SETUP RECYCLERVIEW TÌM KIẾM MỚI (Dọc)
        searchAdapter = JobSearchAdapter(searchList) { job ->
            Log.d(TAG, "Job click callback: ${job.tieuDe} | ID=${job.maBaiDang}")
            openChiTiet(job) }
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearchResults.adapter = searchAdapter
    }
    // 🔥 HÀM SETUP SEARCH (Xử lý logic tìm kiếm và ẩn hiện giao diện)
    private fun setupSearch() {
        // 1. Bấm icon kính lúp để mở ô nhập liệu
        binding.ivSearch.setOnClickListener {
            binding.edtSearch.visibility = View.VISIBLE
            binding.ivDoSearch.visibility = View.VISIBLE
            binding.ivCloseSearch.visibility = View.VISIBLE // Hiện nút hủy
            binding.edtSearch.requestFocus()
        }

        // 2. XỬ LÝ NÚT TÌM KIẾM
        binding.ivDoSearch.setOnClickListener {
            val keyword = binding.edtSearch.text.toString().trim().lowercase().removeVietnameseAccent()

            if (keyword.isEmpty()) return@setOnClickListener

            // Ẩn trang chủ, Hiện kết quả tìm kiếm
            binding.scrollViewMain.visibility = View.GONE
            binding.recyclerSearchResults.visibility = View.VISIBLE
            binding.tvNoResult.visibility = View.GONE

            // Logic tìm kiếm: Tiêu đề OR Công ty OR Lương
            searchList.clear()
            val results = fullJobList.filter { job ->
                val title = (job.tieuDe ?: "").lowercase().removeVietnameseAccent()
                val company = (job.tenCongTy ?: "").lowercase().removeVietnameseAccent()
                val salary = (job.thongTinChiTiet?.mucLuong ?: 0).toString()

                // Kiểm tra trùng khớp
                title.contains(keyword) || company.contains(keyword) || salary.contains(keyword)
            }

            if (results.isEmpty()) {
                binding.tvNoResult.visibility = View.VISIBLE
            }

            searchAdapter.updateData(results)

            // Ẩn bàn phím
            val imm = activity?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        }

        // 3. XỬ LÝ NÚT HỦY TÌM KIẾM (dấu X)
        binding.ivCloseSearch.setOnClickListener {
            // Xóa text
            binding.edtSearch.setText("")
            binding.edtSearch.visibility = View.GONE
            binding.ivDoSearch.visibility = View.GONE
            binding.ivCloseSearch.visibility = View.GONE
            binding.tvNoResult.visibility = View.GONE

            // Quay về trang chủ
            binding.recyclerSearchResults.visibility = View.GONE
            binding.scrollViewMain.visibility = View.VISIBLE

            // Ẩn bàn phím
            val imm = activity?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        }
    }

    // Thay thế hàm cũ bằng hàm này
    private fun openChiTiet(job: Bai_Dang_CV) {
        // 1. Kiểm tra log xem hàm này có được gọi không
        android.util.Log.d(TAG, "🟢 openChiTiet được gọi với Job: ${job.tieuDe}")

        // 2. Kiểm tra xem Job có null không
        if (job == null) {
            android.util.Log.e(TAG, "🔴 Lỗi: Job bị null")
            return
        }

        val fragment = FragmentChiTietViec()
        val bundle = Bundle().apply {
            putSerializable("baiDang", job)
        }
        fragment.arguments = bundle

        // 3. SỬA QUAN TRỌNG: Dùng requireActivity().supportFragmentManager thay vì parentFragmentManager
        // Điều này đảm bảo việc thay thế fragment diễn ra ở cấp Activity cha, không bị lỗi context.
        try {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer1, fragment) // Đảm bảo ID này đúng là của Activity chứa
                .addToBackStack(null)
                .commit()
            android.util.Log.d(TAG, "🟢 Đã gọi lệnh commit chuyển trang")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "🔴 Lỗi khi chuyển fragment: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun String.removeVietnameseAccent(): String {
        val regex = Regex("\\p{M}")
        val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return regex.replace(temp, "")
    }


}