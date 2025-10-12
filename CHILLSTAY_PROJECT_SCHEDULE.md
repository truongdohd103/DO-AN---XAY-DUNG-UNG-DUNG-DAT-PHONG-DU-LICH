# ChillStay App Development Schedule

## 📋 Tổng quan dự án
**Dự án**: ChillStay - Ứng dụng đặt phòng khách sạn  
**Quy mô team**: 2 người  
**Mục tiêu**: Hoàn thiện ứng dụng đặt phòng tương đương BookingApp  
**Kiến trúc**: Clean Architecture + MVVM + Jetpack Compose  
**Dependency Injection**: Koin  

---

## 🎯 **Kế hoạch nhiệm vụ**

| # | Nhiệm vụ | Mô tả | Ưu tiên | Ước lượng (≤ 4h/nhiệm vụ) |
|---|------|-------------|----------|-----------------------------------|
| **1** | **Khởi tạo dự án** | | | |
| 1.1 | Áp dụng cấu trúc Clean Architecture | Khởi tạo dự án với cấu trúc Clean Architecture cho ChillStay | High | 4 |
| 1.2 | Tạo màn Launch | Thiết kế UI màn Launch với Logo và bản quyền | Medium | 3 |
| 1.3 | Bottom Navigation | Tạo Bottom Navigation 5 tab (Home, Search, Bookings, Profile, More) | High | 4 |
| 1.4 | Cấu hình Database | Cấu hình Room Database (hoặc Firebase) cho lưu trữ cục bộ | Medium | 4 |
| **2** | **Welcome & Onboarding** | | | |
| 2.1 | Welcome Screen | UI màn chào mừng, giới thiệu ứng dụng | Medium | 4 |
| 2.2 | Carousel Screen | UI carousel giới thiệu tính năng chính | Medium | 4 |
| 2.3 | Điều hướng Welcome | Điều hướng giữa Welcome và Carousel | Medium | 3 |
| 2.4 | Animation mượt | Thêm animation chuyển cảnh mượt giữa các màn | Medium | 3 |
| **3** | **Xác thực người dùng** | | | |
| 3.1 | Authentication Screen | UI màn xác thực với lựa chọn Đăng nhập/Đăng ký | High | 4 |
| 3.2 | Sign In Screen | UI đăng nhập (email/password) | High | 4 |
| 3.3 | Sign Up Screen | UI đăng ký (form + password rules) | High | 4 |
| 3.4 | Reset Password Screen | UI đặt lại mật khẩu | Medium | 3 |
| 3.5 | Verify Code Screen | UI nhập mã xác minh khi quên mật khẩu | Medium | 3 |
| 3.6 | Xử lý Auth | Triển khai logic xác thực và điều hướng | High | 4 |
| 3.7 | Hoàn thiện hồ sơ | UI điền thông tin hồ sơ sau đăng ký | Medium | 4 |
| **4** | **Home Screen** | | | |
| 4.1 | Tạo UI Home | UI Home với danh sách khách sạn và danh mục | High | 4 |
| 4.2 | Xử lý sự kiện Home | Xử lý click card khách sạn và điều hướng | High | 3 |
| 4.3 | Hiển thị dữ liệu | Lấy và hiển thị dữ liệu khách sạn | High | 4 |
| 4.4 | Tìm kiếm nhanh | Thanh tìm kiếm và filter nhanh ở Home | High | 3 |
| **5** | **Search & Filter** | | | |
| 5.1 | Search Hotel Screen | UI tìm khách sạn kèm bộ lọc | High | 4 |
| 5.2 | Filter Screen | UI bộ lọc nâng cao | Medium | 4 |
| 5.3 | Xử lý tìm kiếm | Triển khai logic search và áp dụng filter | High | 4 |
| 5.4 | Kết quả tìm kiếm | Hiển thị kết quả + phân trang | High | 3 |
| **6** | **Hotel Details** | | | |
| 6.1 | Hotel Detail Screen | UI chi tiết khách sạn (amenities, info) | High | 4 |
| 6.2 | Hotel Gallery Screen | UI thư viện ảnh (carousel) | Medium | 3 |
| 6.3 | Hotel Review Screen | UI đánh giá của khách | Medium | 4 |
| 6.4 | Contact Property Screen | UI liên hệ khách sạn | Medium | 3 |
| 6.5 | Xử lý sự kiện Detail | Bắt đầu flow booking và điều hướng | High | 3 |
| **7** | **Chọn phòng (Room Selection)** | | | |
| 7.1 | Search Room Screen | UI chọn ngày/số khách để tìm phòng | High | 4 |
| 7.2 | Room Screen | UI danh sách phòng có sẵn | High | 4 |
| 7.3 | Room Gallery Screen | UI thư viện ảnh phòng | Medium | 3 |
| 7.4 | Xử lý chọn phòng | Triển khai chọn phòng và flow booking | High | 4 |
| **8** | **Quy trình Booking** | | | |
| 8.1 | Booking Screen | UI đặt phòng (thông tin lưu trú, khách, yêu cầu) | High | 4 |
| 8.2 | Payment Screen | UI thanh toán (phương thức thanh toán) | High | 4 |
| 8.3 | Confirmed Screen | UI xác nhận đặt phòng thành công | High | 3 |
| 8.4 | Xử lý logic Booking | Tạo booking và xử lý thanh toán (demo) | High | 4 |
| **9** | **Quản lý người dùng** | | | |
| 9.1 | Profile Screen | UI hồ sơ người dùng | Medium | 4 |
| 9.2 | Edit Profile Screen | UI chỉnh sửa hồ sơ | Medium | 4 |
| 9.3 | Change Password Screen | UI đổi mật khẩu | Medium | 3 |
| 9.4 | Xử lý Profile | Cập nhật hồ sơ và đổi mật khẩu | Medium | 4 |
| **10** | **Bookings & Trips** | | | |
| 10.1 | My Trip Screen | UI chuyến đi của tôi (các booking) | High | 4 |
| 10.2 | Recent Booked Screen | UI đặt gần đây | Medium | 3 |
| 10.3 | Xử lý Trip | Quản lý booking (huỷ, sửa) | High | 4 |
| 10.4 | Cập nhật trạng thái | Theo dõi/cập nhật trạng thái booking | Medium | 3 |
| **11** | **Bookmarks & Reviews** | | | |
| 11.1 | My Bookmark Screen | UI khách sạn đã lưu | Medium | 4 |
| 11.2 | My Review Screen | UI đánh giá của tôi | Medium | 3 |
| 11.3 | Review Screen | UI viết đánh giá | Medium | 4 |
| 11.4 | Xử lý Bookmark/Review | Triển khai tính năng bookmark và review | Medium | 4 |
| **12** | **Vouchers & Promotions** | | | |
| 12.1 | Voucher Screen | UI danh sách voucher | Medium | 4 |
| 12.2 | Voucher Detail Screen | UI chi tiết voucher | Medium | 3 |
| 12.3 | Xử lý Voucher | Áp dụng và validate voucher | Medium | 4 |
| **13** | **Settings & Preferences** | | | |
| 13.1 | Language Screen | UI lựa chọn ngôn ngữ | Low | 3 |
| 13.2 | Notification Screen | UI cài đặt thông báo | Medium | 3 |
| 13.3 | Help Screen | UI trợ giúp (FAQ/hỗ trợ) | Low | 4 |
| 13.4 | Payment Help Screen | UI trợ giúp thanh toán | Low | 3 |
| 13.5 | Xử lý Settings | Lưu và cập nhật cài đặt | Medium | 3 |
| **14** | **Tính năng nâng cao** | | | |
| 14.1 | Theme Toggle | Dark/Light theme toggle toàn app | Medium | 4 |
| 14.2 | Offline Support | Cache dữ liệu offline và đồng bộ | Medium | 4 |
| 14.3 | Push Notifications | Thông báo đẩy cho cập nhật booking | Medium | 4 |
| 14.4 | Location Services | Tìm kiếm khách sạn theo vị trí | Low | 4 |
| **15** | **Testing & Tối ưu** | | | |
| 15.1 | Unit Testing | Unit test cho use cases và ViewModels | High | 4 |
| 15.2 | UI Testing | UI test cho các flow quan trọng | Medium | 4 |
| 15.3 | Tối ưu hiệu năng | Tối ưu hiệu năng/bộ nhớ | High | 4 |
| 15.4 | Xử lý lỗi | Cơ chế xử lý lỗi toàn diện | High | 3 |
| 15.5 | Kiểm thử cuối | Test trên nhiều thiết bị, thu thập phản hồi | High | 4 |

---

## 🤖 **16 — AI & Personalization**

| # | Task | Description | Priority | Estimate Time (≤ 4 hours per task) |
|---|------|-------------|----------|-----------------------------------|
| **16.1** | AI Recommendations (Cold Start) | Gợi ý khách sạn theo phổ biến/khu vực khi chưa có lịch sử | High | 4 |
| **16.2** | Personalized Recommendations | Gợi ý theo lịch sử xem/đặt, bookmarks, hành vi | High | 4 |
| **16.3** | Natural Language Search (NLP) | Tìm kiếm theo câu tự nhiên: “khách sạn gần biển ở Đà Nẵng dưới 1tr” | High | 4 |
| **16.4** | Query Rewriting & Spelling | Chuẩn hoá truy vấn, sửa lỗi chính tả, thêm synonym | Medium | 3 |
| **16.5** | Chat Assistant (FAQ/Guide) | Trợ lý chat giúp tìm phòng, giải thích chính sách, hướng dẫn đặt | Medium | 4 |
| **16.6** | Sentiment on Reviews | Phân tích cảm xúc review để hiển thị điểm tích cực/tiêu cực | Medium | 3 |
| **16.7** | Dynamic Sorting | Sắp xếp thông minh theo relevance/CTR/quality score | Medium | 3 |
| **16.8** | Price Insights (Heuristic/AI) | Cảnh báo giá cao/thấp, xu hướng khu vực | Low | 3 |
| **16.9** | Push Notification Optimization | Thời điểm gửi thông báo tối ưu (heuristic) | Low | 3 |
| **16.10** | A/B Metrics & Telemetry | Thu thập ẩn danh: CTR, conversion, dwell time để tinh chỉnh | Medium | 3 |

Notes:
- Giai đoạn đầu có thể dùng heuristic + rule-based; sau đó thay thế từng phần bằng model đơn giản (on-device) hoặc service API.
- NLP/Chat có thể tích hợp provider API trước (không lưu PII), sau đó tối ưu prompt/guardrail.
- Tất cả tracking phải ẩn danh, tuân thủ privacy.

## 📊 **Thống kê dự án**

### **Tổng số nhiệm vụ**: 75
### **Tổng thời gian ước lượng**: 280 giờ (35 tuần/1 người, ~17.5 tuần/2 người)
### **Phân bổ ưu tiên**:
- **High**: 32 (43%)
- **Medium**: 35 (47%)
- **Low**: 8 (10%)

### **Phân bổ màn hình**:
- **Authentication**: 7
- **Hotel Management**: 8
- **Booking Flow**: 6
- **User Management**: 6
- **Settings & Help**: 8

---

## 🎯 **Giai đoạn phát triển**

### **Giai đoạn 1: Nền tảng (Tasks 1–3)**
- Thiết lập dự án và kiến trúc
- Welcome/Onboarding và Authentication
- **Thời lượng**: 6–8 tuần (2 người)

### **Giai đoạn 2: Tính năng lõi (Tasks 4–8)**
- Home, Search, Hotel Details, Booking flow
- **Thời lượng**: 8–10 tuần (2 người)

### **Giai đoạn 3: Tính năng người dùng (Tasks 9–12)**
- Profile, Trips, Bookmarks, Vouchers
- **Thời lượng**: 6–8 tuần (2 người)

### **Giai đoạn 4: Hoàn thiện & Kiểm thử (Tasks 13–15)**
- Settings, tính năng nâng cao, kiểm thử toàn diện
- **Thời lượng**: 4–6 tuần (2 người)

---

## 🔧 **Lưu ý kỹ thuật**

### **Architecture**:
- Clean Architecture với các layer Domain, Data, Presentation
- MVVM với ViewModel
- Repository pattern cho data access
- Use Cases cho business logic

### **Technologies**:
- **UI**: Jetpack Compose
- **Navigation**: Navigation Compose
- **DI**: Koin
- **Database**: Room Database
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **State Management**: StateFlow/Flow

### **Tính năng chính**:
- Tìm kiếm và lọc khách sạn
- Quản lý booking
- Xác thực người dùng
- Tích hợp thanh toán (demo)
- Push notifications
- Hỗ trợ offline
- Hỗ trợ đa ngôn ngữ

---

## 📝 **Ghi chú**

1. **Ước lượng thời gian**: Mỗi nhiệm vụ ≤ 4 giờ
2. **Phối hợp team**: Chia việc theo sở trường từng thành viên
3. **Ưu tiên**: Làm trước nhiệm vụ High để bảo đảm core flow
4. **Testing**: Kiểm thử gia tăng theo từng sprint
5. **Linh hoạt**: Điều chỉnh tuỳ tiến độ/thay đổi yêu cầu

---

## 🚀 **Chỉ số thành công**

- **Tính năng**: 25+ màn hình hoạt động đầy đủ
- **Hiệu năng**: Khởi động <3s, animation mượt 60fps
- **Chất lượng**: ≥80% test coverage, không bug nghiêm trọng
- **Trải nghiệm**: Điều hướng trực quan, UI phản hồi tốt
- **Kiến trúc**: Sạch, dễ bảo trì, dễ mở rộng

---

*This schedule provides a comprehensive roadmap for developing the ChillStay app to match the quality and functionality of the BookingApp project.*

