package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.graphics.Typeface
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R
import android.widget.AdapterView
class Fragment_ChinhSuaKiNang : Fragment() {

    private lateinit var spinnerKyNang: Spinner
    private lateinit var listViewKyNang: ListView
    private lateinit var adapterList: ArrayAdapter<String>
    private val dsKyNang = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chinh_sua_ki_nang, container, false)

        spinnerKyNang = view.findViewById(R.id.spinnerKyNang)
        listViewKyNang = view.findViewById(R.id.listViewKyNang)

        // Danh sách kỹ năng có sẵn (spinner)
        val kyNangArray = arrayOf("Java", "Kotlin", "C++", "Android", "SQL", "React Native")
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kyNangArray)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKyNang.adapter = adapterSpinner

        // Adapter cho listView (danh sách kỹ năng đã chọn)
        adapterList = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, dsKyNang) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.textSize = 16f
                return view
            }
        }
        listViewKyNang.adapter = adapterList
        // Khi chọn kỹ năng từ spinner → thêm vào danh sách
        spinnerKyNang.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val kyNang = parent.getItemAtPosition(position).toString()
                if (!dsKyNang.contains(kyNang)) {
                    dsKyNang.add(kyNang)
                    adapterList.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Đăng ký context menu cho ListView
        registerForContextMenu(listViewKyNang)

        return view
    }

    // Tạo context menu khi nhấn giữ
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.menu_context_ky_nang, menu)
    }
    // Xử lý chọn menu
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = info.position
        return when (item.itemId) {
            R.id.action_delete -> {
                dsKyNang.removeAt(position)
                adapterList.notifyDataSetChanged()
                true             }
            R.id.action_highlight -> {
                val view = listViewKyNang.getChildAt(position) as? TextView
                view?.setTypeface(null, Typeface.BOLD) // tô đậm
                true                }
            else -> super.onContextItemSelected(item)
        }
    }
}
