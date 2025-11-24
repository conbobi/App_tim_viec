package com.example.app_tim_viec.UI.hosocanhan

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout // Import quan trọng
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.app_tim_viec.MainActivity
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ActivityEditProfile
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ActivityeditInfcontact
import com.example.app_tim_viec.databinding.ActivityHoSoNtvBinding
import com.example.app_tim_viec.models.Experience
import com.example.app_tim_viec.models.ProfileDetail
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // Import quan trọng để lưu đè
import com.example.app_tim_viec.UI.Xac_Thuc.dangnhap.ManHinhDangNhap
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ManageCVsActivity

class ActivityHoSoNTV : AppCompatActivity() {
    private lateinit var binding: ActivityHoSoNtvBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Biến chứa dữ liệu ProfileDetail
    private var currentProfileDetail: ProfileDetail = ProfileDetail()

    // Dữ liệu mẫu
    private val listNganhNghe = arrayOf("Công nghệ thông tin", "Marketing", "Kế toán", "Sales", "Nhân sự", "Xây dựng", "Thiết kế", "Ngân hàng")
    private val listKyNang = arrayOf("Java", "Kotlin", "Python", "Giao tiếp", "Làm việc nhóm", "Tiếng Anh", "Photoshop", "Office", "Hỗ trợ kỹ thuật", "Sửa chữa phần cứng")
    private val listHocVan = arrayOf("Trung học phổ thông", "Cao đẳng", "Đại học", "Thạc sĩ", "Khác")
    private val listHinhThuc = arrayOf("Toàn thời gian", "Bán thời gian", "Thực tập", "Freelance")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoSoNtvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        loadUserProfile()
        loadProfileDetail()
    }

    private fun setupButtons() {
        binding.layoutCV.setOnClickListener { startActivity(Intent(this, ManageCVsActivity::class.java)) }
        binding.ivHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "TrangChuNTV")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        binding.btnEditProfile.setOnClickListener { startActivity(Intent(this, ActivityEditProfile::class.java)) }
        binding.btnEditInfcontact.setOnClickListener { startActivity(Intent(this, ActivityeditInfcontact::class.java)) }
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, ManHinhDangNhap::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // CÁC NÚT CHỨC NĂNG CHÍNH
        binding.btnUpdateInterest.setOnClickListener { showDialogChonNganhNghe() }
        binding.btnUpdateJobInfo.setOnClickListener { showDialogThongTinTimViec() }
        binding.btnUpdateSkill.setOnClickListener { showDialogChonKyNang() }
        binding.btnAddExperience.setOnClickListener { showDialogExperience(null) }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadProfileDetail()
    }

    // --- DATABASE ---
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            if (userDoc.exists()) {
                binding.txtName.text = userDoc.getString("hoTen") ?: "Chưa cập nhật"
                binding.txtJobTitle.text = userDoc.getString("jobTitle") ?: "Chưa cập nhật"
                binding.txtLocation.text = userDoc.getString("location") ?: "Chưa cập nhật"
                binding.txtEmail.text = userDoc.getString("email") ?: auth.currentUser?.email
                binding.txtPhone.text = userDoc.getString("soDienThoai") ?: auth.currentUser?.phoneNumber
                binding.txtAddress.text = userDoc.getString("DiaChiNha") ?: "Chưa cập nhật"
                val avatarUrl = userDoc.getString("avatarUrl")
                if (!avatarUrl.isNullOrEmpty()) {
                    Glide.with(this).load(avatarUrl).circleCrop().into(binding.imgAvatar)
                }
            }
        }
    }

    private fun loadProfileDetail() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val detail = doc.get("profileDetail", ProfileDetail::class.java)
                if (detail != null) {
                    currentProfileDetail = detail
                    updateUI_All()
                }
            }
        }
    }

    private fun saveProfileDetailToFirestore() {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf("profileDetail" to currentProfileDetail)
        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                updateUI_All()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi lưu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- UI UPDATE ---
    private fun updateUI_All() {
        // 1. Ngành nghề
        binding.interestList.removeAllViews()
        if (currentProfileDetail.nganhNgheQuanTam.isEmpty()) {
            binding.tvListNganhNghe.text = "Chưa chọn ngành nghề"
            binding.tvListNganhNghe.visibility = View.VISIBLE
        } else {
            binding.tvListNganhNghe.visibility = View.GONE
            currentProfileDetail.nganhNgheQuanTam.forEach { item ->
                val tv = TextView(this)
                tv.text = "• $item"
                tv.textSize = 16f
                tv.setTextColor(resources.getColor(R.color.black, null))
                binding.interestList.addView(tv)
            }
        }

        // 2. Thông tin tìm việc
        binding.txtEducationLevel.text = currentProfileDetail.trinhDoHocVan
        binding.txtExpectedSalary.text = currentProfileDetail.mucLuongMongMuon
        binding.txtWorkType.text = currentProfileDetail.hinhThucLamViec

        // 3. Kỹ năng chuyên môn
        binding.containerKyNang.removeAllViews()
        if (currentProfileDetail.kyNangChuyenMon.isNotEmpty()) {
            currentProfileDetail.kyNangChuyenMon.forEach { skill ->
                val chip = Chip(this)
                chip.text = skill
                chip.setChipBackgroundColorResource(R.color.teal_n4)
                chip.setTextColor(resources.getColor(R.color.black, null))
                binding.containerKyNang.addView(chip)
            }
        }

        // 4. Kinh nghiệm làm việc
        binding.containerExperience.removeAllViews()
        for (exp in currentProfileDetail.kinhNghiemLamViec) {
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_experience, binding.containerExperience, false)
            itemView.findViewById<TextView>(R.id.tvTime).text = "${exp.thoiGianBatDau} → ${exp.thoiGianKetThuc}"
            itemView.findViewById<TextView>(R.id.tvTitle).text = exp.chucVu
            itemView.findViewById<TextView>(R.id.tvCompany).text = exp.congTy
            itemView.findViewById<TextView>(R.id.tvDesc).text = exp.moTa
            itemView.findViewById<View>(R.id.btnEdit).setOnClickListener { showDialogExperience(exp) }
            itemView.findViewById<View>(R.id.btnDelete).setOnClickListener { deleteExperience(exp) }
            binding.containerExperience.addView(itemView)
        }
    }

    // --- DIALOGS ---

    // 1. Dialog Ngành nghề (Checkbox + Nhập thêm)
    // --- SỬA: DIALOG NGÀNH NGHỀ (Cập nhật mục đã nhập vào list Checkbox) ---
    private fun showDialogChonNganhNghe() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_with_input, null)
        val checkboxContainer = dialogView.findViewById<LinearLayout>(R.id.checkboxContainer)
        val edtOther = dialogView.findViewById<EditText>(R.id.edtOther)

        val checkBoxes = ArrayList<CheckBox>()

        // 1. GỘP DANH SÁCH: Lấy list mặc định + list user đã lưu -> Loại bỏ trùng lặp
        // Logic này giúp những mục "Khác" bạn đã nhập trước đó sẽ hiện thành Checkbox
        val allItems = (listNganhNghe.toList() + currentProfileDetail.nganhNgheQuanTam).distinct()

        // 2. Tạo checkbox cho TẤT CẢ các mục
        allItems.forEach { item ->
            val cb = CheckBox(this)
            cb.text = item
            cb.textSize = 16f
            // Nếu mục này có trong dữ liệu đã lưu của user -> Tích chọn
            cb.isChecked = currentProfileDetail.nganhNgheQuanTam.contains(item)

            checkBoxes.add(cb)
            checkboxContainer.addView(cb)
        }

        // (Lưu ý: Không cần điền text vào edtOther nữa vì nó đã biến thành checkbox ở trên rồi)

        AlertDialog.Builder(this)
            .setTitle("Chọn ngành nghề quan tâm")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedList = ArrayList<String>()

                // Lấy các mục được tích
                checkBoxes.forEach { cb ->
                    if (cb.isChecked) selectedList.add(cb.text.toString())
                }

                // Lấy thêm mục mới từ ô nhập "Khác" (nếu có nhập mới)
                val otherText = edtOther.text.toString().trim()
                if (otherText.isNotEmpty()) {
                    val others = otherText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    selectedList.addAll(others)
                }

                // Cập nhật và Lưu vào Database
                currentProfileDetail.nganhNgheQuanTam = selectedList
                saveProfileDetailToFirestore()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // 2. Dialog Kỹ năng (Checkbox + Nhập thêm)
    // --- SỬA: DIALOG KỸ NĂNG (Cập nhật mục đã nhập vào list Checkbox) ---
    private fun showDialogChonKyNang() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_with_input, null)
        val checkboxContainer = dialogView.findViewById<LinearLayout>(R.id.checkboxContainer)
        val edtOther = dialogView.findViewById<EditText>(R.id.edtOther)

        val checkBoxes = ArrayList<CheckBox>()

        // 1. GỘP DANH SÁCH: List mặc định + List User đã lưu
        val allItems = (listKyNang.toList() + currentProfileDetail.kyNangChuyenMon).distinct()

        // 2. Tạo Checkbox
        allItems.forEach { item ->
            val cb = CheckBox(this)
            cb.text = item
            cb.textSize = 16f
            cb.isChecked = currentProfileDetail.kyNangChuyenMon.contains(item)

            checkBoxes.add(cb)
            checkboxContainer.addView(cb)
        }

        AlertDialog.Builder(this)
            .setTitle("Cập nhật kỹ năng chuyên môn")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val selectedList = ArrayList<String>()

                // Lấy từ checkbox
                checkBoxes.forEach { cb ->
                    if (cb.isChecked) selectedList.add(cb.text.toString())
                }

                // Lấy từ ô nhập mới
                val otherText = edtOther.text.toString().trim()
                if (otherText.isNotEmpty()) {
                    val others = otherText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    selectedList.addAll(others)
                }

                currentProfileDetail.kyNangChuyenMon = selectedList
                saveProfileDetailToFirestore()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // 3. Dialog Kinh nghiệm (Giữ nguyên)
    private fun showDialogExperience(expToEdit: Experience?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_experience, null)
        val edtCongTy = dialogView.findViewById<EditText>(R.id.edtCongTy)
        val edtChucVu = dialogView.findViewById<EditText>(R.id.edtChucVu)
        val edtStart = dialogView.findViewById<EditText>(R.id.edtStart)
        val edtEnd = dialogView.findViewById<EditText>(R.id.edtEnd)
        val cbWorking = dialogView.findViewById<CheckBox>(R.id.cbWorkingHere)
        val edtDesc = dialogView.findViewById<EditText>(R.id.edtDesc)

        if (expToEdit != null) {
            edtCongTy.setText(expToEdit.congTy)
            edtChucVu.setText(expToEdit.chucVu)
            edtStart.setText(expToEdit.thoiGianBatDau)
            edtEnd.setText(expToEdit.thoiGianKetThuc)
            edtDesc.setText(expToEdit.moTa)
            if(expToEdit.thoiGianKetThuc == "Hiện tại") {
                cbWorking.isChecked = true; edtEnd.isEnabled = false; edtEnd.setText("Hiện tại")
            }
        }

        cbWorking.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { edtEnd.setText("Hiện tại"); edtEnd.isEnabled = false }
            else { edtEnd.setText(""); edtEnd.isEnabled = true }
        }

        AlertDialog.Builder(this)
            .setTitle(if (expToEdit == null) "Thêm kinh nghiệm" else "Cập nhật kinh nghiệm")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val exp = expToEdit ?: Experience()
                exp.congTy = edtCongTy.text.toString()
                exp.chucVu = edtChucVu.text.toString()
                exp.thoiGianBatDau = edtStart.text.toString()
                exp.thoiGianKetThuc = edtEnd.text.toString()
                exp.moTa = edtDesc.text.toString()

                if (expToEdit == null) {
                    currentProfileDetail.kinhNghiemLamViec.add(exp)
                }
                saveProfileDetailToFirestore()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteExperience(exp: Experience) {
        AlertDialog.Builder(this).setTitle("Xác nhận xóa").setMessage("Xóa kinh nghiệm tại ${exp.congTy}?")
            .setPositiveButton("Xóa") { _, _ ->
                currentProfileDetail.kinhNghiemLamViec.remove(exp)
                saveProfileDetailToFirestore()
            }
            .setNegativeButton("Hủy", null).show()
    }

    // 4. Dialog Thông tin tìm việc (Giữ nguyên)
    private fun showDialogThongTinTimViec() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_job_info, null)
        val spEducation = dialogView.findViewById<Spinner>(R.id.spinnerEducation)
        val spWorkType = dialogView.findViewById<Spinner>(R.id.spinnerWorkType)
        val edtSalary = dialogView.findViewById<EditText>(R.id.edtSalary)

        spEducation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listHocVan)
        spWorkType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listHinhThuc)
        edtSalary.setText(currentProfileDetail.mucLuongMongMuon)

        AlertDialog.Builder(this).setTitle("Cập nhật thông tin").setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                currentProfileDetail.trinhDoHocVan = spEducation.selectedItem.toString()
                currentProfileDetail.hinhThucLamViec = spWorkType.selectedItem.toString()
                currentProfileDetail.mucLuongMongMuon = edtSalary.text.toString()
                saveProfileDetailToFirestore()
            }
            .setNegativeButton("Hủy", null).show()
    }
}