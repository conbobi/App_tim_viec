package com.example.app_tim_viec.models

open class User(
    var id: String? = null,
    var hoTen: String? = null,
    var email: String? = null,
    var soDienThoai: String? = null,
    var matKhau: String? = null,
    var Role: String? = null
)

class UserProfile(
    var jobTitle: String? = null,
    var location: String? = null
) : User() // 👈 kế thừa