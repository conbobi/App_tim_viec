// Tạo một class mới trong package 'com.example.app_tim_viec.models' của bạn
// Đây là mô hình dữ liệu (Data Model) cho mỗi Document trong Collection 'cvs'
data class CVFile(
    // Tên trường trong Firestore: userId
    var userId: String? = null,

    // Tên trường trong Firestore: fileName
    var fileName: String? = null,

    // Tên trường trong Firestore: storagePath (Đường dẫn trên Supabase)
    var storagePath: String? = null,

    // Tên trường trong Firestore: downloadUrl (Link công khai của file)
    var downloadUrl: String? = null,

    // Tên trường trong Firestore: uploadedAt
    // Sử dụng Long hoặc Date/Timestamp tùy thuộc vào cách bạn lưu (Kotlin Long hoặc Firebase Timestamp)
    var uploadedAt: Long? = null
)