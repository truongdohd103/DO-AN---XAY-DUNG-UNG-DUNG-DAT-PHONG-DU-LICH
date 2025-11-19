# Hướng Dẫn: Load User Info cho Reviews - Hoàn Chỉnh

## 📋 Tổng Quan

Từ hình ảnh Firestore, cấu trúc User:
- `fullName`: "User 1"
- `email`: "user1@chillstay.com"
- `dateOfBirth`: "2025-10-30"
- `gender`: "Male"
- `photoUrl`: ""
- Document ID = userId (ví dụ: "77WoJxAlofa5RMrfjvWmNQnK6mi1")

## 🔧 Các Bước Thực Hiện
### **Bước 1: Sửa User Mode - Map Field "e-mail"** ⚠️ QUAN TRỌNG

**File:** `app/src/main/java/com/example/chillstay/domain/model/User.kt`

### **Bước 2: Sửa FirestoreUserRepository - Map Đúng Field và Thêm Logging**

**File:** `app/src/main/java/com/example/chillstay/data/repository/firestore/FirestoreUserRepository.kt`

**Cần sửa method `getUser()`:**

```kotlin
override suspend fun getUser(id: String): User? {
    return try {
        android.util.Log.d("FirestoreUserRepository", "Getting user: $id")
        
        val document = firestore.collection("users")
            .document(id)
            .get()
            .await()
        
        if (document.exists()) {
            val data = document.data
            Log.d("FirestoreUserRepository", "User document data: $data")
            val user = User(
                id = document.id,
                email = data?.get("email") as? String ?: "",
                password = data?.get("password") as? String ?: "",
                fullName = data?.get("fullName") as? String ?: "",
                gender = data?.get("gender") as? String ?: "",
                photoUrl = data?.get("photoUrl") as? String ?: "",
                dateOfBirth = (data?.get("dateOfBirth") as? String)?.let { dateStr ->
                    try {
                        java.time.LocalDate.parse(dateStr)
                    } catch (e: Exception) {
                        java.time.LocalDate.of(2000, 1, 1)
                    }
                } ?: java.time.LocalDate.of(2000, 1, 1)
            )
            user
        } else {
            Log.w("FirestoreUserRepository", "User document not found: $id")
            null
        }
    } catch (e: Exception) {
        Log.e("FirestoreUserRepository", "Error getting user $id: ${e.message}", e)
        null
    }
}
```

---

### **Bước 3: Kiểm Tra HotelDetailViewModel - Đảm Bảo Load Users**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailViewModel.kt`

**Kiểm tra:** Method `loadUsersForReviews()` đã có và đang gọi đúng chưa
```
private suspend fun loadUsersForReviews(reviews: List<Review>) {
    // ... đã có code load users song song
    // Load users với async/await để parallel
}
```

### **Bước 4: Kiểm Tra UI - Hiển Thị User Name**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailScreen.kt`

**Kiểm tra ReviewsSection:**

```
@Composable
fun ReviewsSection(
    rating: Double,
    reviewCount: Int,
    reviewsWithUser: List<ReviewWithUser> = emptyList()
) {
    // ...
    
    if (reviewsWithUser.isNotEmpty()) {
        LazyRow(...) {
            items(reviewsWithUser) { reviewWithUser ->
                ReviewCard(
                    name = reviewWithUser.userName,
                    location = "Recently",
                    rating = reviewWithUser.review.rating,
                    comment = reviewWithUser.review.comment,
                    photoUrl = reviewWithUser.userPhotoUrl
                )
            }
        }
    }
}
```

### **Bước 5: Kiểm Tra ReviewWithUser - UserName Logic**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/ReviewWithUser.kt`

**Đảm bảo logic đúng:**

```kotlin
val userName: String
    get() = user?.fullName?.takeIf { it.isNotBlank() }  // Ưu tiên 1: fullName
        ?: user?.email?.takeIf { it.isNotBlank() }        // Ưu tiên 2: email
        ?: "User ${review.userId.takeLast(4)}"           // Fallback: User xxxx
```

**Logic:**
1. Nếu có `fullName` và không rỗng → Dùng `fullName`
2. Nếu không có `fullName`, dùng `email` nếu có
3. Nếu không có cả 2 → Dùng "User xxxx" (4 ký tự cuối userId)

---

## 🔍 Debug với Logs

### **Xem Logs trong Logcat:**

1. **Filter theo tag:**
   - `FirestoreUserRepository` - Logs khi load user
   - `HotelDetailViewModel` - Logs khi load reviews và users

2. **Kiểm tra các log quan trọng:**

```
FirestoreUserRepository: Getting user: [USER_ID]
FirestoreUserRepository: User document data: {fullName=..., e-mail=..., ...}
FirestoreUserRepository: Parsed user: id=..., fullName=..., email=...

HotelDetailViewModel: Loading users for X reviews
HotelDetailViewModel: Loaded user: id=..., fullName=..., email=...
HotelDetailViewModel: Loaded X reviews with user info
```



## 💡 Lưu Ý

1. **Date format:**
   - Firestore có thể lưu dateOfBirth là String "2025-10-30"
   - Cần parse sang LocalDate

2. **Performance:**
   - Load users song song (parallel) đã được implement
   - Sử dụng `async/await` để tăng tốc

3. **Fallback:**
   - Nếu user không tìm thấy → vẫn hiển thị review với "User xxxx"
   - Nếu fullName rỗng → dùng email hoặc "User xxxx"

---
