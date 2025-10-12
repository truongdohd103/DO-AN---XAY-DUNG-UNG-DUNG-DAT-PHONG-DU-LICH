# Kế hoạch 3 tháng hoàn thiện ChillStay (20/09 → 20/12)

## Giả định
- Kiến trúc: Clean Architecture + MVVM + Compose + Koin/Hilt (đã sẵn)
- Dữ liệu: giai đoạn đầu dùng Fake/In-memory, dần chuyển sang Firestore/Room
- Ưu tiên: Hoàn thiện flow chính E2E: Onboarding → Auth → Home/Search → Details → Room → Booking → Trip → Profile

---

## Tháng 1 — Yêu cầu, Thiết kế, Kiến trúc, Nền tảng dữ liệu

### Yêu cầu & thiết kế
- One-pager: mục tiêu, phạm vi V1, KPIs, ràng buộc kỹ thuật
- User flows: Onboarding → Auth → Home/Search → Details → Room → Booking → Trip → Profile
- Sitemap: nhóm màn theo feature; dự kiến deep-link cho Details/Booking
- Wireframe (low-fi): Welcome, Carousel, Authentication (Sign in/Sign up), Home (search/filter), Details, Room, Booking, Profile, Bookmark, Trip

### Kiến trúc & khởi tạo
- Cấu trúc module/layer: core, domain, data, ui; chuẩn hoá package
- Navigation graph, `Routes`, `AppNavHost`, `MainActivity`
- DI: modules ViewModel/UseCase/Repository; base `Result`, error mapper, DispatcherProvider
- Coding conventions: naming, theming, spacing, typography

### Firebase & mô hình dữ liệu
- Tích hợp Firebase: Auth (Email/Password), Firestore, Storage
- Thiết kế schema Firestore: users, homestays (hotels), rooms, reviews, bookmarks, trips (bookings), vouchers
- Security Rules v1:
  - users: mỗi user chỉ xem/sửa profile của mình
  - bookmarks: chỉ CRUD của user
  - trips: user chỉ xem/huỷ booking của mình
  - reviews: sửa/xoá review của chính user
  - homestays/rooms: read public, write admin
- Seed data dev: JSON + ảnh mẫu (Storage), import 20–50 bản ghi

### DevOps & nền tảng
- Flavors: dev/release; app icon; versioning
- CI: build + lint; secrets Firebase an toàn
- Tài liệu: README (setup), PROJECT_ANALYSIS (kiến trúc), USE_CASES (liệt kê)

#### Deliverables
- Wireframe + user flows + sitemap
- App chạy được: DI + Navigation
- Kết nối Firebase + Security Rules v1 + seed data

#### Acceptance
- App → Welcome/Carousel → Main (bottom nav)
- Đăng ký/đăng nhập với Firebase Auth
- Đọc dữ liệu homestays từ Firestore

---

## Tháng 2 — UI lõi, Chủ đề, Profile/Security

### Onboarding & Authentication
- Welcome + Carousel (auto next/skip) với SharedPreferences first-run
- Authentication: Sign in/Sign up (password rules realtime, success message)
- Fill Profile: cập nhật tên, avatar (Storage), validate các trường bắt buộc

### Home/Search/Filter
- Home UI theo Figma: header, search bar, categories/tabs, hotel cards, promotions, recently booked
- Search + filter: theo tên/khu vực/giá/sao/amenities (query Firestore + indexes)
- Recently Booked: từ trips của user (fallback trending nếu rỗng)

### My Bookmark
- List/Grid switcher; thêm/xoá bookmark; đồng bộ trạng thái tim
- Pagination/lazy + image caching (Coil) + placeholders

### Profile & Security
- Profile, Edit Profile, Change Password
- Dark mode + theme tokens (màu/chữ/spacing), component base (Button, Chip, Card, Tag, Badge, Rating)
- Privacy: ẩn PII, validate inputs; thông báo lỗi thân thiện

### Trải nghiệm & hiệu năng
- Scaffold/Insets nhất quán; không trùng bottom bar/header
- State hoá, loading/empty/error; debounce search; snapshotFlow theo scroll

#### Deliverables
- UI đầy đủ cho Welcome, Carousel, Authentication, Fill Profile, Home, Search/Filter, Recently Booked, My Bookmark
- Theme hệ thống + component base
- Profile/Change Password tích hợp Firebase

#### Acceptance
- Search + filter chạy trên Firestore (đủ composite indexes)
- Bookmark list/grid mượt; thêm/xoá realtime (snapshot listener)
- Dark mode hoạt động toàn bộ màn đã có

---

## Tháng 3 — Details/Booking/Trip, Đồng bộ, Tối ưu, Release

### Homestay/Hotel Details
- Slider/Gallery (pager), thumbnails; facilities căn đúng layout; location, languages
- Reviews: list + filter theo sao; điểm trung bình; phân trang/lazy
- Policies: children/extra beds, check-in/out; contact property

### Bookmark & Booking Flow
- Luồng bookmark nhất quán giữa Home/Details/Bookmark
- Book Panel/Booking Date: date range picker, số khách/phòng, pricing preview
- Room screen: hiển thị đầy đủ (amenities, breakfast, payment, discount, sold-out overlay)
- Booking screen: stay details, special requests, payment method (mock), price breakdown

### Đồng bộ Firestore
- Đồng bộ: homestay, review, bookmark, profile, trips
- Use Cases: CreateBooking, CancelBooking, GetUserBookings, ApplyVoucherToBooking (nếu dùng voucher)
- Real-time cập nhật trips/bookmarks → UI

### Kiểm thử tích hợp & tối ưu
- E2E: Onboarding → Auth → Home/Search → Details → Room → Booking → Trip
- Tối ưu: recomposition, keys/remember, lazy lists, prefetch ảnh, tránh overdraw
- Security Rules: test bằng emulator suite (allow/deny đúng)
- Crash/Logs: bật crashlytics (tuỳ chọn), logging có kiểm soát

### Release & Tài liệu
- Build bundle release, ký, shrinker/proguard
- Báo cáo: kiến trúc, schema, flows, indexes, bảo mật, kiểm thử
- Demo script + video

#### Deliverables
- Details/Room/Booking/Trip hoàn chỉnh + real-time
- E2E pass, mượt, không crash
- AAB release + tài liệu + demo

#### Acceptance
- Đặt phòng end-to-end (tạo/huỷ), cập nhật Trip
- Review filter sao hoạt động; bookmark đồng bộ
- Security Rules pass các case chính

---

## Gợi ý AI & Personalization (tuỳ thời gian/Phase sau)
- NLP Search cơ bản (query rewrite, spelling) + Recommendations heuristic/cá nhân hoá
- Sentiment analysis review (tag tích cực/tiêu cực)
- Chat assistant (FAQ/guide) qua API provider (không lưu PII)

---

## Rủi ro & Giảm thiểu
- Firestore query hạn chế: thiết kế index, precomputed fields, normalized schema
- Ảnh nặng: thumbnails + caching; giới hạn kích thước upload
- Timezones & date range: chuẩn hoá UTC, validate đầu vào
- Quota/chi phí: cache offline, hạn chế listener không cần thiết

## Phân công (2 người)
- Dev A: Presentation/UI + Navigation + State
- Dev B: Domain/Data + Use Cases + Repositories + Persistence
- Pair cho Booking & Voucher




