package com.example.app_tim_viec.ui.hosocanhan

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.ActivityHoSoNtvBinding

class ActivityHoSoNTV : AppCompatActivity() {

    private lateinit var binding: ActivityHoSoNtvBinding
    private lateinit var toggle: ActionBarDrawerToggle  // để quản lý nút hamburger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHoSoNtvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy nội dung từ EditText khi bấm ImageButton
        binding.btnSend.setOnClickListener {
            val name = binding.edtName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show()
            } else {
                binding.txtName.text = "Xin chào $name"
                Toast.makeText(this, "Đã nhận: $name", Toast.LENGTH_SHORT).show()
            }
        }
        // 1. Gắn Toolbar thay ActionBar
        setSupportActionBar(binding.toolbar)


        // 2. Tạo nút hamburger (cho Navigation Drawer)
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState() // đồng bộ trạng thái icon

        // 3. Gán sự kiện click nút chỉnh sửa
        binding.txtName.text = "Xin chào NTV"
        binding.btnChinhSua.setOnClickListener {
            val intent = Intent(this, ActivityChinhSuaHoSo::class.java)
            startActivity(intent)
        }

        // ⚠️ Nếu bạn vẫn muốn xử lý item trong NavigationView (menu trái) thì giữ lại
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    Toast.makeText(this, "Cài đặt trong Navigation Drawer", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // 4. Nạp menu Toolbar (submenu đúng chuẩn sẽ nằm ở đây)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }


    // 5. Xử lý click item trên Toolbar (bao gồm submenu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Xử lý toggle cho Drawer
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return when (item.itemId) {
            R.id.menu_profile -> {
                Toast.makeText(this, "Bạn bấm Hồ sơ cá nhân", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.submenu_view_profile -> {
                Toast.makeText(this, "Xem hồ sơ", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.submenu_edit_profile -> {
                val intent = Intent(this, ActivityChinhSuaHoSo::class.java)
                startActivity(intent)
                true
            }
            R.id.submenu_saved_jobs -> {
                Toast.makeText(this, "Công việc đã lưu", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.submenu_applied_jobs -> {
                Toast.makeText(this, "Công việc đã ứng tuyển", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.submenu_account -> {
                Toast.makeText(this, "Tài khoản", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.submenu_notifications -> {
                Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    }
}
