package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.trangchu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R

class FragmentTrangChuNTD : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gắn layout XML vào fragment
        return inflater.inflate(R.layout.fragment_trang_chu_ntd, container, false)
    }
}
