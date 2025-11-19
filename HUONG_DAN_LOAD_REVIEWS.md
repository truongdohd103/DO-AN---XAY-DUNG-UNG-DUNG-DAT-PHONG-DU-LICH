# Hướng Dẫn: Load Reviews từ Firestore và Hiển Thị lên HotelDetailScreen

## 📋 Tổng Quan

**Cấu trúc Review trong Firestore (chính xác):**
- `comment` (String): Nội dung review
- `hotelId` (String): ID khách sạn
- `rating` (Number): Điểm đánh giá (1-5)
- `userId` (String): ID người dùng
- `id` (String): Document ID (mặc định)

---

## 🔧 Các Bước Thực Hiện

### **Bước 1: Kiểm Tra Review Model** ✅

**File:** `app/src/main/java/com/example/chillstay/domain/model/Review.kt`
**Tin tốt:** Model hiện tại đã đúng, không cần sửa!

### **Bước 2: Kiểm Tra FirestoreReviewRepository** ✅

**File:** `app/src/main/java/com/example/chillstay/data/repository/firestore/FirestoreReviewRepository.kt`
**Vị trí:** Method `getHotelReviews()` (dòng 25-44)
**Tin tốt:** Code hiện tại đã đúng!

### **Bước 3: Thêm Reviews vào HotelDetailUiState**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailUiState.kt`

**Cần thêm:**
```kotlin
package com.example.chillstay.ui.hoteldetail

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.Review  // ✅ THÊM IMPORT

@Immutable
data class HotelDetailUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val rooms: List<Room> = emptyList(),
    val reviews: List<Review> = emptyList(),  // ✅ THÊM FIELD NÀY
    val minPrice: Int? = null,
    val isBookmarked: Boolean = false,
    val error: String? = null
) : UiState {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateHotel(value: Hotel?) = copy(hotel = value)
    fun updateRooms(value: List<Room>) = copy(rooms = value)
    fun updateReviews(value: List<Review>) = copy(reviews = value)  // ✅ THÊM METHOD NÀY
    fun updateMinPrice(value: Int?) = copy(minPrice = value)
    fun updateIsBookmarked(value: Boolean) = copy(isBookmarked = value)
    fun updateError(value: String?) = copy(error = value)
    fun clearError() = copy(error = null)
}
```

---

### **Bước 4: Thêm GetHotelReviewsUseCase vào HotelDetailViewModel**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailViewModel.kt`

**Cần sửa:**

#### **A. Thêm Import và Dependency:**

```kotlin
package com.example.chillstay.ui.hoteldetail

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.review.GetHotelReviewsUseCase  // ✅ THÊM IMPORT NÀY
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HotelDetailViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase,
    private val getHotelReviews: GetHotelReviewsUseCase,  // ✅ THÊM PARAM NÀY
    private val addBookmark: AddBookmarkUseCase,
    private val removeBookmark: RemoveBookmarkUseCase
) : BaseViewModel<HotelDetailUiState, HotelDetailIntent, HotelDetailEffect>(HotelDetailUiState()) {
    // ...
}
```

#### **B. Thêm Method Load Reviews:**

```kotlin
// ✅ THÊM METHOD NÀY (sau loadHotelRooms method)
private suspend fun loadHotelReviews(hotelId: String) {
    try {
        val result = getHotelReviews(hotelId, limit = 3, offset = 0)  // Load 3 reviews mới nhất
        when (result) {
            is com.example.chillstay.core.common.Result.Success -> {
                _state.update { it.updateReviews(result.data) }
            }
            is com.example.chillstay.core.common.Result.Error -> {
                // Reviews không bắt buộc, có thể để trống nếu lỗi
                _state.update { it.updateReviews(emptyList()) }
            }
        }
    } catch (exception: Exception) {
        // Reviews không bắt buộc, có thể để trống nếu lỗi
        _state.update { it.updateReviews(emptyList()) }
    }
}
```

#### **C. Gọi Load Reviews khi Load Hotel:**

```kotlin
private fun handleLoadHotelDetails(hotelId: String) {
    _state.update { it.updateIsLoading(true).clearError() }

    viewModelScope.launch {
        try {
            val result = getHotelById(hotelId)
            when (result) {
                is com.example.chillstay.core.common.Result.Success -> {
                    _state.update { it.updateHotel(result.data) }
                    loadHotelRooms(hotelId)
                    loadHotelReviews(hotelId)  // ✅ THÊM DÒNG NÀY
                }
                is com.example.chillstay.core.common.Result.Error -> {
                    _state.update {
                        it.updateIsLoading(false).updateError(result.throwable.message ?: "Failed to load hotel")
                    }
                }
            }
        } catch (exception: Exception) {
            _state.update {
                it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
            }
            viewModelScope.launch {
                sendEffect { HotelDetailEffect.ShowError(exception.message ?: "Failed to load hotel details") }
            }
        }
    }
}
```

---

### **Bước 5: Cập Nhật DI Module - ViewModelModule**

**File:** `app/src/main/java/com/example/chillstay/di/ViewModelModule.kt`

**Cần sửa:**

```kotlin
val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { 
        HotelDetailViewModel(
            get(),  // GetHotelByIdUseCase
            get(),  // GetHotelRoomsUseCase
            get(),  // GetHotelReviewsUseCase ✅ THÊM PARAM NÀY
            get(),  // AddBookmarkUseCase
            get()   // RemoveBookmarkUseCase
        )
    }
    // ... other viewModels
}
```

---

### **Bước 6: Sửa UI - Hiển Thị Reviews từ State**

**File:** `app/src/main/java/com/example/chillstay/ui/hoteldetail/HotelDetailScreen.kt`

**Vị trí:** ReviewsSection (dòng 544-646)

#### **A. Cập Nhật ReviewsSection Composable:**

```kotlin
@Composable
fun ReviewsSection(
    rating: Double,
    reviewCount: Int,
    reviews: List<Review> = emptyList()  // ✅ THÊM PARAM NÀY
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                color = Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See all",
                color = Color(0xFF1AB6B6),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* TODO: Navigate to all reviews */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("%.1f", rating),
                color = Color(0xFF1AB6B6),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$reviewCount reviews",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Review tags - Có thể giữ hardcode tạm thời hoặc extract từ reviews
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { ReviewTag("Reception and House keeping") }
            item { ReviewTag("Great for activities") }
            item { ReviewTag("Hotel in and rest") }
            item { ReviewTag("Wonderful") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Review cards - THAY HARDCODE BẰNG DATA TỪ STATE
        if (reviews.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(
                        name = "User ${review.userId.takeLast(4)}",  // Hiển thị 4 ký tự cuối userId
                        location = "Recently",  // Firestore không có createdAt, dùng "Recently"
                        rating = review.rating,
                        comment = review.comment  // ✅ Dùng review.comment (không phải review.text)
                    )
                }
            }
        } else {
            // Empty state nếu không có reviews
            Text(
                text = "No reviews yet",
                color = Color(0xFF757575),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}


#### **B. Cập Nhật Call Site của ReviewsSection:**

Tìm dòng gọi `ReviewsSection` trong `HotelDetailScreen` (khoảng dòng 161) và sửa:

```kotlin
item {
    ReviewsSection(
        rating = uiState.hotel?.rating ?: 0.0,
        reviewCount = uiState.hotel?.numberOfReviews ?: 0,
        reviews = uiState.reviews  // ✅ THÊM PARAM NÀY
    )
}
```

---

### **Bước 7: Load User Name (Optional - Nếu muốn hiển thị tên thật)**

**Nếu muốn hiển thị tên user thật thay vì "User xxxx", cần load từ users collection:**

**Cách 1:** Load user name khi load reviews (trong Repository hoặc UseCase)

**Cách 2:** Thêm `userName` vào Review model khi tạo review (denormalize)


---

## 🎯 Tóm Tắt Các File Cần Sửa

1. ✅ **`Review.kt`** - Đã đúng, không cần sửa
2. ✅ **`FirestoreReviewRepository.kt`** - Đã đúng, không cần sửa
3. ⚠️ **`HotelDetailUiState.kt`** - **CẦN SỬA:** Thêm `reviews` field
4. ⚠️ **`HotelDetailViewModel.kt`** - **CẦN SỬA:** Thêm `GetHotelReviewsUseCase` và load reviews
5. ⚠️ **`ViewModelModule.kt`** - **CẦN SỬA:** Cập nhật DI binding
6. ⚠️ **`HotelDetailScreen.kt`** - **CẦN SỬA:** Hiển thị reviews từ state (dùng `review.comment` thay vì `review.text`)

---

## ⚠️ Lưu Ý Quan Trọng

### **1. Field Name Mapping:**
- ✅ **Đã khớp:** Firestore có `comment` và Review model có `comment`
- ✅ Không cần map thủ công, Firestore tự động map

### **2. Date/Timestamp:**
- ⚠️ Firestore **KHÔNG có** `createdAt` field
- Review model cũng không có field `created` → Đúng
- Hiển thị "Recently" hoặc có thể thêm `createdAt` vào Firestore sau nếu cần

### **3. Empty State:**
- Xử lý trường hợp không có reviews
- Hiển thị message phù hợp

### **4. Error Handling:**
- Reviews không bắt buộc
- Nếu load reviews fail, vẫn hiển thị được hotel detail
- Set `reviews = emptyList()` nếu lỗi

### **5. Performance:**
- Chỉ load 3 reviews mới nhất (limit = 3)
- Có thể lazy load thêm khi scroll

---

