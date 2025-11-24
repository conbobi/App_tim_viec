package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

// 1. SỬA LẠI IMPORT CHO ĐÚNG
import io.github.jan.supabase.SupabaseClient // Không có "_tennert"
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage // Không có "_tennert"

// 2. THAY URL VÀ KEY CỦA BẠN VÀO ĐÂY
// Lấy từ trang API Settings (ảnh 2)
private const val SUPABASE_URL = "https://ujpkisxovpkolqotsouh.supabase.co"

// Lấy key 'anon' (public) trong mục 'API Keys' trên cùng trang đó
private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqcGtpc3hvdnBrb2xxb3Rzb3VoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIwMzk3ODksImV4cCI6MjA3NzYxNTc4OX0.CVSM1HMRsbb4LR8g_EVoO5WkKIypgUnTvL6RCG9y3W4" // <-- BẠN PHẢI THAY KEY NÀY

// 3. ĐỔI TÊN CLASS VÀ DÙNG 'object' (Singleton)
object SupabaseModule {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage) // Chỉ cài module Storage
    }
}