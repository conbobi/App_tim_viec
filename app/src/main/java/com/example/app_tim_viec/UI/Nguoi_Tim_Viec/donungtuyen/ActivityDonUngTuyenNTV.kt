package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.donungtuyen

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_tim_viec.Data.Nguoi_Tim_Viec.DonDaUngTuyen
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu.AppliedJobAdapter // Adapter bạn đã có
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityDonUngTuyenNTV : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_don_ung_tuyen_ntv)

        // Ánh xạ View
        recycler = findViewById(R.id.recyclerDonUngTuyen)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        // 1. Lấy danh sách đơn của User này
        db.collection("DonUngTuyen")
            .whereEqualTo("ID_NguoiTV", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val listApplied = mutableListOf<DonDaUngTuyen>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in snapshot) {
                    val idBaiDang = doc.getString("ID_BaiDang") ?: ""
                    val trangThai = doc.getString("trang_thai") ?: "Đang xử lý"

                    val don = DonDaUngTuyen(
                        idDon = doc.id,
                        idBaiDang = idBaiDang,
                        trangThai = trangThai
                    )

                    // 🔥 GIẢI PHÁP: Dùng TaskCompletionSource để chờ lấy xong Tên & Cty
                    if (idBaiDang.isNotEmpty()) {
                        val tcs = com.google.android.gms.tasks.TaskCompletionSource<Any>()

                        db.collection("BaiDangCV").document(idBaiDang).get()
                            .addOnSuccessListener { baiDangDoc ->
                                if (baiDangDoc != null && baiDangDoc.exists()) {
                                    // Tạo list task con để lấy Tên bài + Tên công ty
                                    val subTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                                    // A. Lấy Tiêu đề bài đăng (Từ subcollection)
                                    val titleTask = baiDangDoc.reference.collection("thongTinCoBan").get()
                                        .addOnSuccessListener { basicSnap ->
                                            if (!basicSnap.isEmpty) {
                                                don.tieuDeBaiDang = basicSnap.documents[0].getString("Tieu_De") ?: "Không có tiêu đề"
                                            }
                                        }
                                    subTasks.add(titleTask)

                                    // B. Lấy Tên công ty (Từ bảng HoSoNTD)
                                    val maNTD = baiDangDoc.getString("maNhaTuyenDung")
                                    if (!maNTD.isNullOrEmpty()) {
                                        val companyTask = db.collection("HoSoNTD").document(maNTD).get()
                                            .addOnSuccessListener { ntdDoc ->
                                                // Lưu ý: Kiểm tra tên trường trong DB là "TenCongTy" hay "tenCongTy"
                                                don.tenCongTy = ntdDoc.getString("TenCongTy") ?: ntdDoc.getString("tenCongTy") ?: "Công ty"
                                            }
                                        subTasks.add(companyTask)
                                    }

                                    // Khi lấy xong cả Tên + Cty -> Đánh dấu task này xong (tcs.setResult)
                                    Tasks.whenAllComplete(subTasks).addOnCompleteListener {
                                        tcs.setResult(true)
                                    }
                                } else {
                                    don.tieuDeBaiDang = "Bài đăng đã bị xóa"
                                    tcs.setResult(true)
                                }
                            }
                            .addOnFailureListener {
                                tcs.setResult(false) // Vẫn đánh dấu là xong để không treo app
                            }

                        tasks.add(tcs.task) // Thêm task chờ vào danh sách chính
                    }
                    listApplied.add(don)
                }

                // 3. Chờ load xong hết thông tin phụ thì hiển thị
                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    progressBar.visibility = View.GONE

                    // Setup Adapter
                    val adapter = AppliedJobAdapter(listApplied)
                    recycler.layoutManager = LinearLayoutManager(this)
                    recycler.adapter = adapter
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvEmpty.text = "Lỗi tải dữ liệu"
                tvEmpty.visibility = View.VISIBLE
            }
    }
}