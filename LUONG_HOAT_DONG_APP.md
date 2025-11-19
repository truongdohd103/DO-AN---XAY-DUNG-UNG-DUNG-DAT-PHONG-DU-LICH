# Tài Liệu Hướng Dẫn - Luồng Hoạt Động và Cấu Trúc App ChillStay

## 📋 Tổng Quan

**ChillStay** là ứng dụng Android đặt phòng khách sạn du lịch, được xây dựng với:
- **Kiến trúc**: MVVM (Model-View-ViewModel) kết hợp Clean Architecture
- **UI Framework**: Jetpack Compose
- **Cơ sở dữ liệu**: Firebase Firestore
- **Xác thực**: Firebase Authentication
- **Dependency Injection**: Koin
- **Navigation**: Navigation Compose

---

## 🏗️ Kiến Trúc Tổng Thể

App được tổ chức theo **Clean Architecture** với các layer:

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │  ← Màn hình, Components
├─────────────────────────────────────┤
│      ViewModel Layer (MVVM)         │  ← Quản lý state, logic
├─────────────────────────────────────┤
│      Domain Layer (Use Cases)       │  ← Business logic
├─────────────────────────────────────┤
│       Data Layer (Repository)       │  ← Data source (Firestore)
└─────────────────────────────────────┘
```

---

## 📁 Cấu Trúc Thư Mục và Chức Năng

### 1. **`core/`** - Core Components

#### `core/base/`
- **`BaseViewModel.kt`**: Base class cho tất cả ViewModel
  - Quản lý `UiState`, `UiEvent`, `UiEffect`
  - Pattern: StateFlow cho state, Channel cho effects
  
- **`UiState.kt`**: Base interface cho UI state
  
- **`UiEvent.kt`**: Base interface cho UI events (user actions)
  
- **`UiEffect.kt`**: Base interface cho side effects (navigate, show snackbar)

#### `core/common/`
- **`Result.kt`**: Wrapper cho Success/Error results
- **`OnboardingManager.kt`**: Quản lý onboarding flow (lưu trạng thái welcome screen đã xem)

---

### 2. **`data/`** - Data Layer

#### `data/repository/firestore/`
Chứa các repository implementation kết nối với Firestore:

- **`FirestoreHotelRepository.kt`**: 
  - `getHotels()`: Lấy danh sách hotels từ Firestore collection "hotels"
  - `searchHotels()`: Tìm kiếm hotels theo query, country, city, rating, price
  - `getHotelById()`: Lấy chi tiết hotel theo ID
  - `getHotelRooms()`: Lấy danh sách phòng của hotel

- **`FirestoreUserRepository.kt`**: Quản lý user data (profile, settings)

- **`FirestoreBookingRepository.kt`**: Quản lý bookings (tạo, hủy, lấy danh sách)

- **`FirestoreBookmarkRepository.kt`**: Quản lý bookmarks (thêm, xóa, lấy danh sách)

- **`FirestoreReviewRepository.kt`**: Quản lý reviews của hotels

- **`FirestoreVoucherRepository.kt`**: Quản lý vouchers

- **`FirestoreBillRepository.kt`**: Quản lý bills/hoá đơn

#### `data/api/`
- **`ChillStayApi.kt`**: Interface định nghĩa các API methods
- **`FirebaseChillStayApi.kt`**: Implementation sử dụng Firestore
  - `getPopularHotels()`, `getRecommendedHotels()`, `getTrendingHotels()`
  - `getUserBookings()`, `getUserBookmarks()`

---

### 3. **`domain/`** - Domain Layer (Business Logic)

#### `domain/model/`
Chứa các data models:
- **`Hotel.kt`**: Model hotel (id, name, country, city, rating, imageUrl, rooms, detail...)
- **`Room.kt`**: Model phòng (type, price, capacity, availability...)
- **`Booking.kt`**: Model đặt phòng (userId, hotelId, roomId, dates, status...)
- **`User.kt`**: Model người dùng
- **`Bookmark.kt`**: Model bookmark
- **`Review.kt`**: Model đánh giá
- **`Voucher.kt`**: Model voucher
- **`Bill.kt`**: Model hoá đơn

#### `domain/repository/`
Interfaces định nghĩa contracts cho repositories:
- **`HotelRepository.kt`**: Interface cho hotel operations
- **`BookingRepository.kt`**: Interface cho booking operations
- **`BookmarkRepository.kt`**: Interface cho bookmark operations
- Và các repository interfaces khác...

#### `domain/usecase/`
Chứa các use cases (business logic):
- **`hotel/`**: 
  - `GetHotelsUseCase.kt`: Lấy danh sách hotels
  - `GetHotelByIdUseCase.kt`: Lấy chi tiết hotel
  - `SearchHotelsUseCase.kt`: Tìm kiếm hotels
  - `GetHotelRoomsUseCase.kt`: Lấy phòng của hotel
  
- **`booking/`**: 
  - `CreateBookingUseCase.kt`: Tạo booking mới
  - `GetUserBookingsUseCase.kt`: Lấy bookings của user
  - `CancelBookingUseCase.kt`: Hủy booking
  
- **`bookmark/`**: 
  - `AddBookmarkUseCase.kt`: Thêm bookmark
  - `RemoveBookmarkUseCase.kt`: Xóa bookmark
  - `GetUserBookmarksUseCase.kt`: Lấy bookmarks của user

- **`review/`**: Use cases cho reviews

- **`voucher/`**: Use cases cho vouchers

- **`SignInUseCase.kt`**, **`SignUpUseCase.kt`**: Authentication use cases

---

### 4. **`ui/`** - UI Layer (Jetpack Compose)

#### `ui/navigation/`
- **`Routes.kt`**: Định nghĩa tất cả routes trong app
  - `WELCOME`, `CAROUSEL`, `MAIN`, `HOME`
  - `AUTHENTICATION`, `SIGN_IN`, `SIGN_UP`
  - `HOTEL_DETAIL`, `ROOM`, `BOOKING`, `BOOKING_DETAIL`
  - `SEARCH`, `BOOKMARK`, `MY_TRIPS`, `PROFILE`
  - `VOUCHER`, `VOUCHER_DETAIL`, `REVIEW`, `BILL`

- **`AppNavHost.kt`**: Navigation graph chính
  - Định nghĩa tất cả composable routes
  - Xử lý navigation logic
  - Kiểm tra authentication trước khi navigate

#### `ui/main/`
- **`MainScreen.kt`**: Màn hình chính với bottom navigation
  - Tab 0: Home
  - Tab 1: Voucher
  - Tab 2: Bookmark
  - Tab 3: My Trips
  - Tab 4: Profile
  - Xử lý authentication check cho các tab yêu cầu login

#### `ui/home/`
- **`HomeScreen.kt`**: Màn hình home hiển thị hotels
- **`HomeViewModel.kt`**: ViewModel quản lý home state
  - Load hotels theo categories (Popular, Recommended, Trending)
  - Quản lý bookmarks
  - Toggle bookmark với optimistic UI update
- **`HomeUiState.kt`**: State cho home screen
- **`HomeIntent.kt`**: Events từ UI (change category, refresh, toggle bookmark)
- **`HomeEffect.kt`**: Side effects (show error, show bookmark message)

#### `ui/welcome/`
- **`WelcomeScreen.kt`**: Màn hình welcome đầu tiên (splash screen)
- **`CarouselScreen.kt`**: Onboarding carousel

#### `ui/auth/`
- **`AuthenticationScreen.kt`**: Màn hình chọn đăng nhập/đăng ký
- **`SignInScreen.kt`**: Màn hình đăng nhập (email/password)
- **`SignUpScreen.kt`**: Màn hình đăng ký
  - Sử dụng Firebase Auth
  - Tạo user document trong Firestore sau khi sign up

#### `ui/hoteldetail/`
- **`HotelDetailScreen.kt`**: Chi tiết hotel
- **`HotelDetailViewModel.kt`**: Load hotel detail, rooms, reviews
- **`hotelDetailRoutes()`**: Navigation extension functions

#### `ui/room/`
- **`RoomScreen.kt`**: Danh sách phòng của hotel
- **`RoomViewModel.kt`**: Load rooms, filter theo dates/guests

#### `ui/booking/`
- **`BookingScreen.kt`**: Màn hình đặt phòng
- **`BookingViewModel.kt`**: Tạo booking mới
- **`BookingDetailScreen.kt`**: Chi tiết booking

#### `ui/bookmark/`
- **`MyBookmarkScreen.kt`**: Danh sách bookmarks của user
- **`MyBookmarkViewModel.kt`**: Load bookmarks

#### `ui/trip/`
- **`MyTripScreen.kt`**: Danh sách trips (bookings) của user
  - Tab: Pending, Completed
- **`MyTripViewModel.kt`**: Load bookings, filter theo status

#### `ui/voucher/`
- **`VoucherScreen.kt`**: Danh sách vouchers
- **`VoucherDetailScreen.kt`**: Chi tiết voucher

#### `ui/review/`
- **`ReviewScreen.kt`**: Viết đánh giá cho booking

#### `ui/bill/`
- **`BillScreen.kt`**: Xem hoá đơn của booking

#### `ui/profile/`
- **`ProfileScreen.kt`**: Thông tin profile, logout

#### `ui/components/`
- **`BottomNavigationBar.kt`**: Bottom navigation bar component
- **`ImageLoaderConfig.kt`**: Coil image loader configuration

#### `ui/theme/`
- **`Color.kt`**: Color scheme
- **`Theme.kt`**: Material3 theme
- **`Type.kt`**: Typography

---

### 5. **`di/`** - Dependency Injection (Koin)

- **`RepositoryModule.kt`**: 
  - Cung cấp Firebase instances (Firestore, Auth)
  - Cung cấp repository implementations
  
- **`UseCaseModule.kt`**: Cung cấp use cases
  
- **`ViewModelModule.kt`**: Cung cấp ViewModels

---

## 🔄 Luồng Hoạt Động Chi Tiết

### 1. **App Startup Flow**

```
ChillStayApplication (onCreate)
    ↓
- Initialize Firebase
- Initialize Koin DI
    ↓
MainActivity
    ↓
- Set up Navigation
    ↓
AppNavHost (startDestination = WELCOME)
    ↓
WelcomeScreen → CarouselScreen → MainScreen
```

### 2. **Authentication Flow**

```
User clicks "Sign In" hoặc "Sign Up"
    ↓
AuthenticationScreen
    ↓
SignInScreen / SignUpScreen
    ↓
FirebaseAuth.signInWithEmailAndPassword() / createUserWithEmailAndPassword()
    ↓
- Success: Tạo user document trong Firestore (nếu sign up)
- Navigate to MainScreen
- Failure: Hiển thị error message
```

### 3. **Home Screen Flow**

```
MainScreen (Tab 0: Home)
    ↓
HomeScreen
    ↓
HomeViewModel
    ↓
- Load Popular Hotels (category 0)
- Load user bookmarks (nếu đã login)
    ↓
FirebaseChillStayApi.getPopularHotels()
    ↓
Firestore.collection("hotels").orderBy("rating").limit(5)
    ↓
Display hotels in UI
```

### 4. **Hotel Detail Flow**

```
User clicks hotel card
    ↓
Navigate to HotelDetailScreen(hotelId)
    ↓
HotelDetailViewModel
    ↓
- Load hotel detail: FirestoreHotelRepository.getHotelById()
- Load rooms: Firestore.collection("rooms").whereEqualTo("hotelId")
- Load reviews: FirestoreReviewRepository.getHotelReviews()
    ↓
Display hotel detail, rooms, reviews
```

### 5. **Booking Flow**

```
User clicks "Choose Room" → RoomScreen
    ↓
User selects room, dates → Clicks "Book Now"
    ↓
Check authentication:
    - Nếu chưa login → Navigate to AuthenticationScreen
    - Nếu đã login → Navigate to BookingScreen
    ↓
BookingScreen
    ↓
BookingViewModel
    ↓
CreateBookingUseCase
    ↓
FirestoreBookingRepository.createBooking()
    ↓
Firestore.collection("bookings").add(bookingData)
    ↓
Success → Navigate to BookingDetailScreen
```

### 6. **Bookmark Flow**

```
User clicks bookmark icon
    ↓
HomeViewModel.handleToggleBookmark()
    ↓
- Optimistic UI update (toggle ngay lập tức)
- AddBookmarkUseCase / RemoveBookmarkUseCase
    ↓
FirestoreBookmarkRepository.addBookmark() / removeBookmark()
    ↓
Firestore.collection("bookmarks").add() / delete()
    ↓
- Success: Show success message
- Failure: Revert UI change, show error
```

### 7. **My Trips Flow**

```
MainScreen (Tab 3: My Trips)
    ↓
MyTripScreen
    ↓
MyTripViewModel
    ↓
GetUserBookingsUseCase
    ↓
FirestoreBookingRepository.getUserBookings(userId)
    ↓
Firestore.collection("bookings")
    .whereEqualTo("userId", userId)
    .orderBy("createdAt", DESC)
    ↓
Filter bookings theo status (Pending/Completed)
    ↓
Display bookings
```

---

## 🔥 Firebase Firestore Structure

### Collections trong Firestore:

1. **`hotels`**
   ```json
   {
     "id": "hotel123",
     "name": "Grand Hotel",
     "country": "Vietnam",
     "city": "Ho Chi Minh",
     "rating": 4.5,
     "numberOfReviews": 120,
     "imageUrl": "https://...",
     "priceRange": { "min": 100, "max": 500 }
   }
   ```

2. **`rooms`**
   ```json
   {
     "id": "room456",
     "hotelId": "hotel123",
     "type": "Deluxe",
     "price": 150.0,
     "capacity": 2,
     "isAvailable": true,
     "imageUrl": "https://...",
     "detail": {
       "name": "Deluxe Room",
       "size": 30.0,
       "view": "Ocean View"
     }
   }
   ```

3. **`bookings`**
   ```json
   {
     "id": "booking789",
     "userId": "user123",
     "hotelId": "hotel123",
     "roomId": "room456",
     "checkIn": "2024-01-15",
     "checkOut": "2024-01-17",
     "status": "pending",
     "totalPrice": 300.0,
     "createdAt": "2024-01-10T10:00:00Z"
   }
   ```

4. **`bookmarks`**
   ```json
   {
     "id": "bookmark001",
     "userId": "user123",
     "hotelId": "hotel123",
     "createdAt": "2024-01-10T10:00:00Z"
   }
   ```

5. **`users`**
   ```json
   {
     "id": "user123",
     "email": "user@example.com",
     "fullName": "John Doe",
     "gender": "Male",
     "photoUrl": "https://...",
     "dateOfBirth": "1990-01-01"
   }
   ```

6. **`reviews`**: Chứa reviews của hotels
7. **`vouchers`**: Chứa vouchers
8. **`bills`**: Chứa hoá đơn

---

## 🎯 Các Tính Năng Chính

1. **Xem danh sách hotels**: Popular, Recommended, Trending
2. **Tìm kiếm hotels**: Theo tên, địa điểm, rating, giá
3. **Xem chi tiết hotel**: Thông tin, phòng, đánh giá
4. **Đặt phòng**: Chọn phòng, dates, tạo booking
5. **Quản lý bookmarks**: Thêm/xóa bookmark
6. **Quản lý trips**: Xem bookings (Pending/Completed)
7. **Đánh giá**: Viết review cho booking đã hoàn thành
8. **Vouchers**: Xem danh sách và chi tiết voucher
9. **Profile**: Xem thông tin, logout

---

## 🔐 Authentication & Authorization

- **Firebase Authentication**: Xử lý sign in/sign up
- **Firestore Security Rules**: Kiểm soát quyền truy cập data
- **UI Guards**: Kiểm tra authentication trước khi navigate đến các màn hình yêu cầu login (Bookmark, My Trips, Profile)

---

## 📊 State Management Pattern

App sử dụng **MVVM pattern** với:
- **State**: `StateFlow<UiState>` - Quản lý UI state
- **Events**: `UiIntent` - User actions
- **Effects**: `Channel<UiEffect>` - Side effects (navigation, snackbar)

Ví dụ trong `HomeViewModel`:
```kotlin
_state: MutableStateFlow<HomeUiState>
onEvent(event: HomeIntent) // Handle events
sendEffect { HomeEffect.ShowError(...) } // Side effects
```

---

## 🚀 Entry Points

1. **`ChillStayApplication`**: App initialization
2. **`MainActivity`**: Main activity, setup navigation
3. **`AppNavHost`**: Navigation graph entry point

---

## 📝 Lưu Ý Quan Trọng

1. **Firestore Indexes**: Một số queries cần composite index (ví dụ: search với nhiều filters)
   - Firebase Console sẽ tự động gợi ý khi cần

2. **Error Handling**: 
   - Repository layer catch exceptions và return empty list/null
   - ViewModel handle errors và update state
   - UI hiển thị error messages

3. **Offline Support**: 
   - Firestore có offline persistence mặc định
   - App có thể hoạt động offline với cached data

4. **Performance**:
   - Sử dụng Paging3 cho large lists (trong dependencies)
   - Optimistic UI updates cho bookmarks
   - Background thread cho network operations (Dispatchers.IO)

---

## 🔄 Data Flow Summary

```
UI (Compose Screen)
    ↓ User Action
ViewModel (onEvent)
    ↓ Call UseCase
UseCase
    ↓ Call Repository
Repository (Firestore)
    ↓ Query Firestore
Firestore Database
    ↓ Return Data
Repository → UseCase → ViewModel (update state) → UI (recompose)
```

---

Chúc bạn thành công với dự án ChillStay! 🎉

