# Tình Trạng Kết Nối Database - HotelDetail Screen

## 📊 Tổng Quan

**HotelDetail màn hình đã kết nối với Firestore, NHƯNG:**

✅ **ĐÃ kết nối:** Hotel basic info, Rooms  
❌ **CHƯA đúng:** HotelDetail (description, facilities, photoUrls) đang bị hardcode  
❌ **CHƯA kết nối:** Reviews  

---

## ✅ Dữ Liệu ĐÃ Lấy Từ Database

### 1. **Hotel Basic Info** ✅
- **File:** `FirestoreHotelRepository.getHotelById()` (dòng 143-149)
- **Đã load:** name, city, country, rating, numberOfReviews, imageUrl
- **Nguồn:** Firestore collection `hotels`

```kotlin
// Đã làm:
val document = firestore.collection("hotels")
    .document(id)
    .get()
    .await()

val hotel = document.toObject(Hotel::class.java)?.copy(id = document.id)
```

### 2. **Rooms** ✅
- **File:** `FirestoreHotelRepository.getHotelById()` (dòng 152-179)
- **Đã load:** rooms từ Firestore collection `rooms`
- **ViewModel:** `HotelDetailViewModel.loadHotelRooms()` (dòng 103-130)

---

## ❌ Dữ Liệu CHƯA Lấy Từ Database (Đang Hardcode)

### 1. **HotelDetail** ❌ HARDCODE

**Vị trí:** `FirestoreHotelRepository.getHotelById()` (dòng 183-187)

```kotlin
// ❌ ĐANG HARDCODE:
val hotelDetail = HotelDetail(
    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",  // ❌ Hardcode
    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),          // ❌ Hardcode
    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }  // ❌ Hardcode
)
```

**Cần sửa:** Load từ Firestore document fields:
- `description` → từ Firestore
- `facilities` → từ Firestore array
- `photoUrls` → từ Firestore array

---

### 2. **Reviews** ❌ CHƯA LOAD

**Vấn đề:**
- `HotelDetailViewModel` **KHÔNG có** `GetHotelReviewsUseCase`
- `HotelDetailUiState` **KHÔNG có** `reviews` field
- UI đang hiển thị hardcode reviews (Antonio, Julie, John Doe)

**Cần làm:**
1. Thêm `GetHotelReviewsUseCase` vào ViewModel
2. Load reviews khi load hotel details
3. Thêm reviews vào state
4. Hiển thị reviews từ state thay vì hardcode

---

### 3. **Languages, Policies, Location** ❌ CHƯA LOAD

- Languages: Hardcode trong UI (English, Italian, Chinese, Vietnamese)
- Policies: Hardcode text trong UI
- Location map: Hardcode placeholder URL

---

## 🔧 Các File Cần Sửa Để Lấy Đầy Đủ Dữ Liệu Từ Firestore

### **1. `FirestoreHotelRepository.kt`** ⚠️ QUAN TRỌNG NHẤT

**File:** `app/src/main/java/com/example/chillstay/data/repository/firestore/FirestoreHotelRepository.kt`

**Vị trí:** Method `getHotelById()` (dòng 141-196)

**Cần sửa:**
```kotlin
// ❌ DÒNG 183-187 - XÓA HARDCODE:
val hotelDetail = HotelDetail(
    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",
    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),
    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }
)

// ✅ THAY BẰNG - LOAD TỪ FIRESTORE:
val data = document.data
val hotelDetail = HotelDetail(
    description = data?.get("description") as? String ?: "",
    facilities = (data?.get("facilities") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
    photoUrls = (data?.get("photoUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
    location = // Load location object từ Firestore
)
```

**Firestore cần có:**
- `description` field (string)
- `facilities` field (array of strings)
- `photoUrls` field (array of strings)

---

### **2. `HotelDetailViewModel.kt`** ⚠️ QUAN TRỌNG

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailViewModel.kt`

**Cần thêm:**
1. Import `GetHotelReviewsUseCase`
2. Thêm vào constructor
3. Thêm method `loadHotelReviews()`
4. Gọi `loadHotelReviews()` trong `handleLoadHotelDetails()`

```kotlin
// ✅ THÊM VÀO CONSTRUCTOR:
class HotelDetailViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase,
    private val getHotelReviews: GetHotelReviewsUseCase, // ✅ THÊM NÀY
    // ...
)

// ✅ THÊM METHOD:
private suspend fun loadHotelReviews(hotelId: String) {
    try {
        val result = getHotelReviews(hotelId, limit = 3)
        when (result) {
            is Result.Success -> {
                _state.update { it.updateReviews(result.data) }
            }
            // ...
        }
    } catch (e: Exception) {
        // Handle error
    }
}

// ✅ GỌI TRONG handleLoadHotelDetails():
private fun handleLoadHotelDetails(hotelId: String) {
    // ... existing code ...
    when (result) {
        is Result.Success -> {
            _state.update { it.updateHotel(result.data) }
            loadHotelRooms(hotelId)
            loadHotelReviews(hotelId) // ✅ THÊM DÒNG NÀY
        }
        // ...
    }
}
```

---

### **3. `HotelDetailUiState.kt`** ⚠️ CẦN THIẾT

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailUiState.kt`

**Cần thêm:**
```kotlin
data class HotelDetailUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val rooms: List<Room> = emptyList(),
    val reviews: List<Review> = emptyList(), // ✅ THÊM NÀY
    val minPrice: Int? = null,
    val isBookmarked: Boolean = false,
    val error: String? = null
) {
    // ... existing methods ...
    fun updateReviews(value: List<Review>) = copy(reviews = value) // ✅ THÊM NÀY
}
```

---

### **4. `HotelDetailScreen.kt`** ⚠️ CẦN THIẾT

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailScreen.kt`

**Cần sửa phần Reviews (dòng 617-644):**

```kotlin
// ❌ XÓA HARDCODE:
item {
    ReviewCard(name = "Antonio", ...) // ❌ Hardcode
}
item {
    ReviewCard(name = "Julie", ...) // ❌ Hardcode
}
item {
    ReviewCard(name = "John Doe", ...) // ❌ Hardcode
}

// ✅ THAY BẰNG:
items(uiState.reviews) { review ->
    ReviewCard(
        name = review.userName ?: "Anonymous", // Cần load user name
        location = formatDate(review.created),
        rating = review.rating,
        comment = review.text
    )
}
```

---

### **5. `di/UseCaseModule.kt`** ✅ KIỂM TRA

**Cần đảm bảo:**
- `GetHotelReviewsUseCase` đã được provide
- `ReviewRepository` đã được inject

---

### **6. `di/ViewModelModule.kt`** ✅ KIỂM TRA

**Cần đảm bảo:**
- `HotelDetailViewModel` nhận `GetHotelReviewsUseCase` trong constructor

---

## 📋 Checklist Nhanh

### **Để Load HotelDetail từ Firestore:**
- [ ] Sửa `FirestoreHotelRepository.getHotelById()` - Load description, facilities, photoUrls từ Firestore
- [ ] Thêm fields vào Firestore hotel documents (nếu chưa có)

### **Để Load Reviews:**
- [ ] Thêm `GetHotelReviewsUseCase` vào `HotelDetailViewModel` constructor
- [ ] Thêm `reviews` vào `HotelDetailUiState`
- [ ] Thêm method `loadHotelReviews()` trong ViewModel
- [ ] Gọi `loadHotelReviews()` khi load hotel
- [ ] Sửa UI để hiển thị reviews từ state
- [ ] Đảm bảo `GetHotelReviewsUseCase` được inject đúng trong DI

---

## 🎯 Tóm Tắt Ngắn Gọn

### **Đã kết nối:** ✅
1. Hotel basic info (name, rating, etc.)
2. Rooms list

### **Chưa đúng - Cần sửa:** ⚠️
1. **FirestoreHotelRepository.getHotelById()** - HotelDetail đang hardcode (dòng 183-187)
   → Sửa để load description, facilities, photoUrls từ Firestore

### **Chưa kết nối - Cần thêm:** ❌
1. **Reviews** - Chưa load từ Firestore
   → Thêm `GetHotelReviewsUseCase` vào ViewModel
   → Load reviews khi load hotel
   → Hiển thị reviews từ state

---

## 🚀 Thứ Tự Ưu Tiên Sửa

### **1. Ưu tiên CAO** (Bắt buộc):
1. ✅ Sửa `FirestoreHotelRepository.getHotelById()` - Load HotelDetail từ Firestore
   - File: `FirestoreHotelRepository.kt` dòng 183-187
   - Thay hardcode bằng load từ Firestore document

### **2. Ưu tiên TRUNG BÌNH**:
2. ✅ Load Reviews từ Firestore
   - Files: `HotelDetailViewModel.kt`, `HotelDetailUiState.kt`, `HotelDetailScreen.kt`

### **3. Ưu tiên THẤP** (Có thể tạm bỏ):
3. ⚠️ Languages, Policies (có thể giữ hardcode tạm thời)

---

## ✅ Kết Luận

**Trả lời câu hỏi:**

> "Màn này đã kết nối với database để lấy dữ liệu về được chưa?"

**Trả lời:** 
- ✅ **ĐÃ kết nối** cho hotel basic info và rooms
- ❌ **CHƯA đúng** - HotelDetail đang hardcode, cần sửa Repository
- ❌ **CHƯA kết nối** - Reviews chưa load, cần thêm vào ViewModel

**Phải sửa những file nào?**

1. **`FirestoreHotelRepository.kt`** ⚠️ QUAN TRỌNG NHẤT
   - Sửa method `getHotelById()` để load HotelDetail từ Firestore

2. **`HotelDetailViewModel.kt`** ⚠️ QUAN TRỌNG
   - Thêm `GetHotelReviewsUseCase`
   - Thêm method load reviews

3. **`HotelDetailUiState.kt`**
   - Thêm `reviews` field

4. **`HotelDetailScreen.kt`**
   - Sửa Reviews section để hiển thị từ state

5. **Firestore Database**
   - Thêm `description`, `facilities`, `photoUrls` vào hotel documents

---

Chúc bạn thành công! 🎉

