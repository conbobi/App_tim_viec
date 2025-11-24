package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Trạng thái của giao diện
sealed class DonUIState {
    object Loading : DonUIState()
    data class Success(val danhSachDon: List<DonUngTuyen>) : DonUIState()
    data class Error(val message: String) : DonUIState()
}

class QuanLyDonViewModel : ViewModel() {

    private val repository = DonUngTuyenRepository()

    // StateFlow để giao diện "lắng nghe" sự thay đổi dữ liệu
    private val _uiState = MutableStateFlow<DonUIState>(DonUIState.Loading)
    val uiState: StateFlow<DonUIState> = _uiState.asStateFlow()

    // ID của NTD hiện tại, sẽ được set từ Fragment
    private var currentNtdId: String? = null

    /**
     * Tải danh sách đơn khi Fragment khởi tạo
     */
    fun loadDonUngTuyen(ntdId: String) {
        currentNtdId = ntdId
        _uiState.value = DonUIState.Loading
        viewModelScope.launch {
            val result = repository.getDonUngTuyenCuaNTD(ntdId)
            result.onSuccess { danhSach ->
                _uiState.value = DonUIState.Success(danhSach)
            }.onFailure { e ->
                _uiState.value = DonUIState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Duyệt đơn
     */
    fun approveDon(donId: String) {
        updateDonStatus(donId, TrangThaiDon.DA_DUYET)
    }

    /**
     * Từ chối đơn
     */
    fun rejectDon(donId: String) {
        updateDonStatus(donId, TrangThaiDon.TU_CHOI)
    }

    private fun updateDonStatus(donId: String, newStatus: TrangThaiDon) {
        viewModelScope.launch {
            val result = repository.updateTrangThaiDon(donId, newStatus)
            result.onSuccess {
                // Tải lại danh sách sau khi cập nhật thành công
                currentNtdId?.let { loadDonUngTuyen(it) }
            }.onFailure { e ->
                _uiState.value = DonUIState.Error("Lỗi cập nhật: ${e.message}")
            }
        }
    }

    /**
     * Xóa đơn
     */
    fun deleteDon(donId: String) {
        viewModelScope.launch {
            val result = repository.deleteDonUngTuyen(donId)
            result.onSuccess {
                // Tải lại danh sách sau khi xóa thành công
                currentNtdId?.let { loadDonUngTuyen(it) }
            }.onFailure { e ->
                _uiState.value = DonUIState.Error("Lỗi xóa: ${e.message}")
            }
        }
    }

    
}