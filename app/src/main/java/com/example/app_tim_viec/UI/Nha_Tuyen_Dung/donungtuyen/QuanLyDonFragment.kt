package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.donungtuyen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUIState
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.QuanLyDonViewModel
import com.example.app_tim_viec.databinding.FragmentQuanLyDonBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // 🔥 Import cái này
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuanLyDonFragment : Fragment() {

    private var _binding: FragmentQuanLyDonBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuanLyDonViewModel
    private lateinit var adapter: DonUngTuyenAdapter
    private var tenCongTy: String = "Công ty chúng tôi"

    // 🔥 SỬA LỖI 1: Thêm khai báo biến db ở đây
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuanLyDonBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[QuanLyDonViewModel::class.java]

        setupRecyclerView()
        setupWebView()
        observeViewModel()
        loadTenCongTy() // Gọi hàm lấy tên công ty

        val ntdId = FirebaseAuth.getInstance().currentUser?.uid
        if (ntdId != null) {
            viewModel.loadDonUngTuyen(ntdId)
        } else {
            Toast.makeText(context, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = DonUngTuyenAdapter(emptyList()) { don, action ->
            when (action) {
                "VIEW_CV" -> openCVDirectly(don.urlFileCV)
                "MAIL" -> sendEmail(don)
                "CHAT" -> openAppChat(don)
                "APPROVE" -> {
                    Toast.makeText(context, "Đang duyệt...", Toast.LENGTH_SHORT).show()
                    viewModel.approveDon(don.id)
                }
                "REJECT" -> {
                    Toast.makeText(context, "Đã từ chối...", Toast.LENGTH_SHORT).show()
                    viewModel.rejectDon(don.id)
                }
            }
        }

        binding.recyclerViewDonUngTuyen.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewDonUngTuyen.adapter = adapter
    }

    private fun setupWebView() {
        binding.webViewCV.settings.javaScriptEnabled = true
        binding.webViewCV.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressLoadCV.visibility = View.GONE
            }
        }

        binding.btnCloseCV.setOnClickListener {
            binding.layoutXemCV.visibility = View.GONE
            binding.webViewCV.loadUrl("about:blank")
        }
    }

    // 🔥 SỬA LỖI 2: Hàm loadTenCongTy giờ sẽ chạy đúng vì đã có biến db
    private fun loadTenCongTy() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("companyName")
                    if (!name.isNullOrEmpty()) {
                        tenCongTy = name
                    }
                }
            }
    }

    private fun openCVDirectly(url: String) {
        if (url.isEmpty()) {
            Toast.makeText(context, "Ứng viên chưa cập nhật CV", Toast.LENGTH_SHORT).show()
            return
        }
        binding.layoutXemCV.visibility = View.VISIBLE
        binding.progressLoadCV.visibility = View.VISIBLE
        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$url"
        binding.webViewCV.loadUrl(googleDocsUrl)
    }

    // Hàm Gửi Email (Giữ nguyên)
    private fun sendEmail(don: com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUngTuyen) {
        val email = don.emailNguoiTV
        if (email.isNullOrEmpty()) {
            Toast.makeText(context, "Không tìm thấy Email ứng viên", Toast.LENGTH_SHORT).show()
            return
        }

        val subject = "THƯ MỜI PHỎNG VẤN - $tenCongTy"
        val body = """
            Chào bạn ${don.tenNguoiTV ?: ""},
            
            Chúc mừng bạn! Hồ sơ ứng tuyển của bạn cho vị trí "${don.tieuDe}" tại $tenCongTy đã được chúng tôi xem xét và đánh giá cao.
            
            Chúng tôi trân trọng mời bạn tham gia buổi phỏng vấn trao đổi trực tiếp tại văn phòng công ty.
            
            Vui lòng phản hồi email này để chúng ta có thể sắp xếp thời gian cụ thể.
            
            Trân trọng,
            Bộ phận tuyển dụng - $tenCongTy
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không tìm thấy ứng dụng Email", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔥 SỬA LỖI 3: Giữ lại hàm sendSMS có mẫu tin nhắn, XÓA hàm sendSMS cũ (chỉ nhận phone string)
    private fun openAppChat(don: com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUngTuyen) {
        // Kiểm tra ID người tìm việc
        if (don.idNguoiTV.isEmpty()) {
            Toast.makeText(context, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        // Mở màn hình ChatActivity
        val intent = Intent(context, com.example.app_tim_viec.UI.Chat.ChatActivity::class.java)
        // Truyền ID và Tên của người tìm việc sang để ChatActivity biết đang chat với ai
        intent.putExtra("userId", don.idNguoiTV)
        intent.putExtra("userName", don.tenNguoiTV)
        startActivity(intent)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is DonUIState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewEmpty.visibility = View.GONE
                    }
                    is DonUIState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.danhSachDon.isEmpty()) {
                            binding.textViewEmpty.visibility = View.VISIBLE
                            adapter.updateData(emptyList())
                        } else {
                            binding.textViewEmpty.visibility = View.GONE
                            adapter.updateData(state.danhSachDon)
                        }
                    }
                    is DonUIState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}