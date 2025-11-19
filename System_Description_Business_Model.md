# System Description in the Natural Language: Business Model

## Objective and Scope

ChillStay là một ứng dụng di động (mobile application) được phát triển cho nền tảng Android, hỗ trợ người dùng đặt phòng khách sạn, homestay và căn hộ du lịch trực tuyến.

Ứng dụng được thiết kế để:
- Cho phép người dùng tìm kiếm và khám phá các cơ sở lưu trú (hotel, resort, homestay) theo địa điểm, giá cả, và tiêu chí cá nhân
- Hỗ trợ quy trình đặt phòng trực tuyến với thanh toán an toàn
- Quản lý lịch sử đặt phòng và các chuyến đi của người dùng
- Cho phép người dùng đánh giá và chia sẻ trải nghiệm lưu trú
- Cung cấp hệ thống voucher và khuyến mãi để tăng trải nghiệm người dùng
- Hỗ trợ chủ khách sạn quản lý khách sạn và xem báo cáo thống kê
- Hỗ trợ quản trị viên quản lý toàn bộ hệ thống

Ứng dụng sử dụng Firebase làm backend service, bao gồm Firebase Authentication cho xác thực người dùng và Firestore Database cho lưu trữ dữ liệu.

## User and Functions that Each User Could Use

### Guest User (Người dùng chưa đăng nhập)

Guest user có thể sử dụng các chức năng sau:
- Xem danh sách khách sạn trên trang chủ
- Tìm kiếm khách sạn theo tên hoặc địa điểm
- Xem chi tiết khách sạn (thông tin, hình ảnh, đánh giá, phòng)
- Xem danh sách phòng của khách sạn
- Xem thông tin voucher/ưu đãi (chưa thể claim hoặc áp dụng)

### Authenticated User (Người dùng đã đăng nhập - Role: USER)

Người dùng đã đăng nhập có thể sử dụng tất cả chức năng của Guest user, cộng thêm:

**Quản lý tài khoản:**
- Đăng ký tài khoản mới (email/password, Google, Facebook)
- Đăng nhập vào hệ thống
- Xem và chỉnh sửa thông tin cá nhân (fullName, gender, photoUrl, dateOfBirth)
- Đăng xuất khỏi tài khoản

**Tìm kiếm và khám phá:**
- Tìm kiếm khách sạn với bộ lọc nâng cao
- Xem danh sách khách sạn theo danh mục (phổ biến, đề xuất, xu hướng)
- Xem chi tiết khách sạn đầy đủ
- Lưu khách sạn vào danh sách yêu thích (bookmark)
- Xem danh sách khách sạn đã lưu

**Đặt phòng:**
- Chọn phòng và đặt phòng trực tuyến
- Nhập thông tin đặt phòng (ngày check-in, check-out, số khách, số phòng)
- Chọn tùy chọn đặc biệt (high floor, quiet room, extra pillows, airport shuttle, early check-in, late check-out)
- Áp dụng voucher/ưu đãi vào đơn đặt phòng
- Chọn phương thức thanh toán
- Xem chi tiết giá (base price, discount, service fee, taxes, total price)
- Xác nhận và thanh toán đặt phòng

**Quản lý đặt phòng:**
- Xem danh sách tất cả đặt phòng của mình (My Trips)
- Lọc đặt phòng theo trạng thái (pending, confirmed, checked-in, checked-out, completed, cancelled, refunded)
- Xem chi tiết đặt phòng
- Hủy đặt phòng (nếu được phép theo chính sách)
- Xem hóa đơn (Bill) của đặt phòng

**Đánh giá:**
- Viết đánh giá cho khách sạn sau khi hoàn thành chuyến đi
- Xem đánh giá của người dùng khác về khách sạn

**Voucher và ưu đãi:**
- Xem danh sách voucher có sẵi
- Xem chi tiết voucher (điều kiện, thời hạn, giá trị)
- Claim voucher (nhận voucher vào tài khoản)
- Kiểm tra điều kiện đủ điều kiện sử dụng voucher
- Áp dụng voucher vào đơn đặt phòng

**Thông báo:**
- Xem danh sách thông báo
- Đánh dấu thông báo đã đọc
- Nhận thông báo về: xác nhận đặt phòng, hủy đặt phòng, thanh toán thành công/thất bại, nhắc nhở đánh giá, khuyến mãi, thông báo hệ thống

**Hỗ trợ:**
- Gửi yêu cầu hỗ trợ (hủy đặt phòng, hoàn tiền, khiếu nại, câu hỏi)
- Xem lịch sử yêu cầu hỗ trợ
- Xem phản hồi từ bộ phận hỗ trợ

### Hotel Owner (Chủ khách sạn/Quản lý khách sạn - Role: HOTEL_OWNER)

Hotel Owner có quyền truy cập vào các chức năng của Authenticated user (với vai trò khách hàng), cộng thêm các chức năng quản lý khách sạn:

**Quản lý khách sạn:**
- Xem danh sách khách sạn được phân quyền quản lý
- Xem chi tiết khách sạn (thông tin đầy đủ)
- Chỉnh sửa thông tin khách sạn (tên, mô tả, địa chỉ, thành phố, quốc gia, loại cơ sở, hạng sao)
- Upload và quản lý hình ảnh khách sạn
- Quản lý tiện ích (features) và ngôn ngữ hỗ trợ (languages)
- Quản lý chính sách (policies): thêm, sửa, xóa chính sách
- Cập nhật tọa độ địa lý (coordinate)

**Quản lý phòng:**
- Xem danh sách phòng của khách sạn
- Thêm phòng mới (nhập thông tin: loại phòng, giá, hình ảnh, sức chứa, chi tiết phòng)
- Chỉnh sửa thông tin phòng (giá, sức chứa, trạng thái available, chi tiết)
- Xóa phòng (nếu không có đặt phòng đang hoạt động)
- Cập nhật trạng thái phòng (available/unavailable)

**Quản lý đặt phòng:**
- Xem danh sách tất cả đặt phòng của khách sạn
- Lọc đặt phòng theo trạng thái (pending, confirmed, checked-in, checked-out, completed, cancelled)
- Xem chi tiết đặt phòng (thông tin khách hàng, phòng, ngày, giá)
- Xác nhận đặt phòng (chuyển từ PENDING sang CONFIRMED)
- Hủy đặt phòng (nếu cần thiết)
- Cập nhật trạng thái check-in (CONFIRMED → CHECKED_IN)
- Cập nhật trạng thái check-out (CHECKED_IN → CHECKED_OUT → COMPLETED)
- Xem hóa đơn của đặt phòng

**Xem báo cáo và thống kê:**
- Báo cáo doanh thu (theo ngày, tuần, tháng, năm)
- Báo cáo đặt phòng (số lượng, tỷ lệ hủy, tỷ lệ hoàn thành)
- Báo cáo tỷ lệ lấp đầy phòng (occupancy rate)
- Báo cáo đánh giá (điểm trung bình, số lượt đánh giá, phân tích đánh giá)
- Báo cáo phòng (phòng được đặt nhiều nhất, doanh thu theo phòng)
- So sánh với các kỳ trước
- Xuất báo cáo ra file (PDF, Excel)

**Quản lý đánh giá:**
- Xem tất cả đánh giá của khách hàng về khách sạn
- Phản hồi đánh giá (trả lời comment của khách hàng)
- Lọc đánh giá theo điểm số, thời gian
- Xem thống kê đánh giá (phân bố điểm số, xu hướng)

### Admin User (Người dùng quản trị - Role: ADMIN)

Admin user có quyền truy cập vào tất cả chức năng của Authenticated user, cộng thêm các chức năng quản trị sau:

**Quản lý người dùng:**
- Xem danh sách tất cả người dùng trong hệ thống
- Tìm kiếm người dùng theo email, tên, hoặc ID
- Xem chi tiết thông tin người dùng
- Thêm người dùng mới (tạo tài khoản thủ công)
- Chỉnh sửa thông tin người dùng
- Xóa người dùng (vô hiệu hóa tài khoản)
- Khóa/mở khóa tài khoản người dùng
- Phân quyền Hotel Owner cho khách sạn (tạo HotelOwnership record)

**Quản lý khách sạn:**
- Xem danh sách tất cả khách sạn trong hệ thống
- Tìm kiếm khách sạn theo tên, địa điểm, hoặc ID
- Xem chi tiết khách sạn
- Thêm khách sạn mới (tạo hotel record với đầy đủ thông tin)
- Chỉnh sửa thông tin khách sạn (tên, mô tả, địa chỉ, tiện ích, chính sách, v.v.)
- Xóa khách sạn
- Quản lý phòng của khách sạn (thêm/sửa/xóa phòng)
- Phân quyền quản lý khách sạn cho Hotel Owner

**Quản lý voucher:**
- Xem danh sách tất cả voucher trong hệ thống
- Tìm kiếm voucher theo mã, tiêu đề, hoặc ID
- Xem chi tiết voucher
- Tạo voucher mới (thiết lập mã, giá trị, điều kiện, thời hạn)
- Chỉnh sửa voucher (cập nhật thông tin, điều kiện, trạng thái)
- Xóa voucher hoặc vô hiệu hóa voucher
- Xem thống kê sử dụng voucher (số lượt claim, số lượt áp dụng)

**Hỗ trợ khách hàng (Support):**
- Xem danh sách yêu cầu hỗ trợ từ người dùng
- Xem chi tiết yêu cầu hỗ trợ
- Phản hồi yêu cầu hỗ trợ (gửi tin nhắn, email, hoặc thông báo)
- Xem lịch sử hỗ trợ của người dùng
- Quản lý các vấn đề liên quan đến đặt phòng (hỗ trợ hủy, hoàn tiền, v.v.)
- Cập nhật trạng thái yêu cầu hỗ trợ (pending, in-progress, resolved, closed)

**Xem báo cáo và thống kê:**
- Báo cáo doanh thu (theo ngày, tuần, tháng, năm) - toàn hệ thống
- Báo cáo đặt phòng (số lượng đặt phòng, tỷ lệ hủy, tỷ lệ hoàn thành) - toàn hệ thống
- Báo cáo người dùng (số người dùng mới, người dùng hoạt động)
- Báo cáo khách sạn (khách sạn phổ biến, doanh thu theo khách sạn)
- Báo cáo voucher (hiệu quả voucher, số lượt sử dụng)
- Báo cáo đánh giá (điểm đánh giá trung bình, số lượt đánh giá)
- Xuất báo cáo ra file (PDF, Excel)

**Gửi thông báo:**
- Tạo thông báo mới (chọn loại thông báo, nội dung)
- Gửi thông báo cho một người dùng cụ thể
- Gửi thông báo cho nhiều người dùng (broadcast)
- Gửi thông báo cho tất cả người dùng (system-wide notification)
- Xem lịch sử thông báo đã gửi
- Lên lịch gửi thông báo (scheduled notification)

## Detailed Business Process of Functions

### 1. Đăng ký tài khoản mới

**Quy trình:**
1. Người dùng mở ứng dụng và chọn "Đăng ký"
2. Người dùng chọn phương thức đăng ký:
   - Đăng ký bằng Email và mật khẩu
   - Đăng ký bằng Google (Google Sign-In)
   - Đăng ký bằng Facebook (Facebook Sign-In)
3. Nếu chọn Email/Password:
   - Người dùng nhập email, mật khẩu, và xác nhận mật khẩu
   - Hệ thống kiểm tra định dạng email hợp lệ
   - Hệ thống kiểm tra mật khẩu đủ mạnh (tối thiểu 8 ký tự)
   - Hệ thống kiểm tra email chưa được sử dụng
   - Hệ thống gửi mã OTP đến email người dùng
   - Người dùng nhập mã OTP để xác thực
4. Nếu chọn Google/Facebook:
   - Người dùng chọn tài khoản Google/Facebook
   - Hệ thống xác thực với Google/Facebook
   - Hệ thống lấy thông tin cơ bản (email, tên, ảnh đại diện)
5. Hệ thống tạo tài khoản User mới trong Firebase Authentication và Firestore với role = USER (mặc định), status = ACTIVE
6. Hệ thống tự động đăng nhập người dùng
7. Hệ thống chuyển người dùng đến trang chủ

**Ngoại lệ:**
- Email không đúng định dạng → Hiển thị thông báo lỗi
- Email đã được sử dụng → Hiển thị thông báo "Email đã tồn tại"
- Mật khẩu không đủ mạnh → Hiển thị yêu cầu mật khẩu
- Mật khẩu và xác nhận không khớp → Hiển thị thông báo lỗi
- Mã OTP không đúng hoặc hết hạn → Yêu cầu gửi lại mã
- Xác thực Google/Facebook thất bại → Hiển thị thông báo lỗi

### 2. Tìm kiếm và xem danh sách khách sạn

**Quy trình:**
1. Người dùng mở trang chủ (Home screen)
2. Hệ thống tự động tải danh sách khách sạn phổ biến và hiển thị
3. Người dùng có thể:
   - Xem danh sách khách sạn theo danh mục (phổ biến, đề xuất, xu hướng)
   - Chọn tab để chuyển đổi giữa các danh mục
   - Kéo xuống để làm mới danh sách
4. Người dùng chọn biểu tượng tìm kiếm để tìm kiếm nâng cao
5. Người dùng nhập từ khóa (tên khách sạn, địa điểm) vào ô tìm kiếm
6. Hệ thống tìm kiếm khách sạn theo từ khóa và hiển thị kết quả
7. Người dùng chọn một khách sạn từ danh sách để xem chi tiết

**Ngoại lệ:**
- Không có kết nối mạng → Hiển thị thông báo lỗi và nút "Thử lại"
- Không tìm thấy kết quả → Hiển thị "Không có kết quả phù hợp"
- Lỗi tải dữ liệu → Hiển thị thông báo lỗi và nút "Thử lại"

### 3. Xem chi tiết khách sạn

**Quy trình:**
1. Người dùng chọn một khách sạn từ danh sách
2. Hệ thống chuyển đến màn hình chi tiết khách sạn (Hotel Detail)
3. Hệ thống tải thông tin chi tiết khách sạn:
   - Tên, mô tả, địa chỉ, thành phố, quốc gia
   - Loại cơ sở (hotel, resort)
   - Hạng sao, điểm đánh giá, số lượt đánh giá
   - Hình ảnh (danh sách)
   - Tiện ích (features)
   - Chính sách (policies)
   - Tọa độ (coordinate) để hiển thị bản đồ
   - Danh sách đánh giá từ người dùng khác
4. Người dùng có thể:
   - Vuốt để xem nhiều hình ảnh
   - Xem bản đồ vị trí khách sạn
   - Xem danh sách đánh giá
   - Chọn nút "Yêu thích" để lưu khách sạn (nếu đã đăng nhập)
   - Chọn "Chọn phòng" để xem danh sách phòng

**Ngoại lệ:**
- Khách sạn không tồn tại → Hiển thị thông báo lỗi và quay lại
- Lỗi tải dữ liệu → Hiển thị thông báo lỗi và nút "Thử lại"

### 4. Đặt phòng

**Quy trình:**
1. Người dùng xem chi tiết khách sạn và chọn "Chọn phòng"
2. Hệ thống chuyển đến màn hình danh sách phòng (Room screen)
3. Hệ thống tải danh sách phòng của khách sạn và hiển thị:
   - Loại phòng (type)
   - Giá mỗi đêm (price)
   - Hình ảnh phòng
   - Sức chứa (capacity)
   - Trạng thái còn phòng (available)
   - Chi tiết phòng (RoomDetail: name, size, view)
4. Người dùng chọn một phòng và nhấn "Đặt phòng"
5. Hệ thống kiểm tra người dùng đã đăng nhập:
   - Nếu chưa đăng nhập → Chuyển đến màn hình đăng nhập
   - Nếu đã đăng nhập → Chuyển đến màn hình đặt phòng (Booking screen)
6. Tại màn hình đặt phòng, hệ thống hiển thị:
   - Thông tin khách sạn và phòng đã chọn
   - Ngày check-in và check-out (mặc định: hôm nay và ngày mai)
   - Số người lớn (mặc định: 2)
   - Số trẻ em (mặc định: 0)
   - Số phòng (mặc định: 1)
7. Người dùng có thể điều chỉnh:
   - Chọn ngày check-in và check-out
   - Thay đổi số người lớn và trẻ em
   - Thay đổi số phòng
   - Chọn tùy chọn đặc biệt (BookingPreferences):
     * Tầng cao (highFloor)
     * Phòng yên tĩnh (quietRoom)
     * Thêm gối (extraPillows)
     * Đưa đón sân bay (airportShuttle)
     * Check-in sớm (earlyCheckIn)
     * Check-out muộn (lateCheckOut)
   - Nhập ghi chú đặc biệt (specialRequests)
8. Hệ thống tự động tính toán giá:
   - Giá gốc (basePrice) = giá phòng × số đêm × số phòng
   - Phí dịch vụ (serviceFee)
   - Thuế (taxes)
   - Tổng tạm tính (subtotal)
9. Người dùng có thể áp dụng voucher:
   - Chọn "Áp dụng voucher"
   - Hệ thống hiển thị danh sách voucher có sẵi và đủ điều kiện
   - Người dùng chọn voucher
   - Hệ thống kiểm tra điều kiện voucher (minBookingAmount, validFrom, validTo, applicableHotelIds)
   - Hệ thống tính giảm giá từ voucher
   - Tổng tiền được cập nhật
10. Người dùng chọn phương thức thanh toán (paymentMethod):
    - Thẻ tín dụng/ghi nợ (CREDIT_CARD, DEBIT_CARD)
    - Ví điện tử (DIGITAL_WALLET)
    - Chuyển khoản ngân hàng (BANK_TRANSFER)
    - Tiền mặt (CASH)
11. Hệ thống hiển thị tổng chi phí cuối cùng (totalPrice)
12. Người dùng xem lại thông tin và nhấn "Xác nhận đặt phòng"
13. Hệ thống kiểm tra:
    - Phòng còn trống trong khoảng thời gian đã chọn
    - Thông tin đặt phòng hợp lệ
    - Phương thức thanh toán đã chọn
14. Hệ thống tạo đối tượng Booking mới với trạng thái PENDING
15. Hệ thống tạo đối tượng Bill tương ứng
16. Hệ thống xử lý thanh toán (tùy phương thức)
17. Nếu thanh toán thành công:
    - Cập nhật trạng thái Booking thành CONFIRMED
    - Cập nhật trạng thái Bill thành "paid"
    - Tạo Payment record
    - Gửi thông báo xác nhận đặt phòng cho người dùng
    - Gửi thông báo cho Hotel Owner (nếu có)
    - Hiển thị thông báo thành công và mã đặt phòng
    - Chuyển người dùng đến màn hình chi tiết đặt phòng
18. Nếu thanh toán thất bại:
    - Giữ trạng thái Booking là PENDING
    - Hiển thị thông báo lỗi thanh toán
    - Cho phép người dùng thử lại hoặc chọn phương thức thanh toán khác

**Ngoại lệ:**
- Phòng không còn trống trong khoảng thời gian đã chọn → Hiển thị thông báo "Phòng đã được đặt" và đề xuất phòng/ngày khác
- Ngày check-out phải sau ngày check-in → Hiển thị thông báo lỗi
- Số khách vượt quá sức chứa phòng → Hiển thị thông báo lỗi
- Voucher không đủ điều kiện → Hiển thị thông báo lý do và không áp dụng voucher
- Thanh toán thất bại → Hiển thị thông báo lỗi và cho phép thử lại
- Lỗi kết nối mạng → Hiển thị thông báo lỗi và nút "Thử lại"

### 5. Xem danh sách đặt phòng (My Trips)

**Quy trình:**
1. Người dùng đã đăng nhập chọn tab "Chuyến đi" (My Trips) trên thanh điều hướng
2. Hệ thống tải danh sách đặt phòng của người dùng từ Firestore
3. Hệ thống hiển thị danh sách đặt phòng với các tab:
   - Tất cả
   - Đang xử lý (PENDING, CONFIRMED)
   - Đã hoàn thành (COMPLETED)
   - Đã hủy (CANCELLED)
4. Mỗi đặt phòng hiển thị:
   - Tên khách sạn
   - Hình ảnh khách sạn
   - Ngày check-in và check-out
   - Trạng thái đặt phòng
   - Tổng tiền
5. Người dùng có thể:
   - Chọn tab để lọc đặt phòng theo trạng thái
   - Kéo xuống để làm mới danh sách
   - Chọn một đặt phòng để xem chi tiết
   - Chọn "Hủy đặt phòng" (nếu trạng thái cho phép)
6. Người dùng chọn một đặt phòng để xem chi tiết:
   - Hệ thống chuyển đến màn hình chi tiết đặt phòng
   - Hiển thị đầy đủ thông tin: khách sạn, phòng, ngày, khách, giá, trạng thái
   - Cho phép xem hóa đơn (Bill)
   - Cho phép viết đánh giá (nếu đã hoàn thành chuyến đi)

**Ngoại lệ:**
- Người dùng chưa đăng nhập → Chuyển đến màn hình đăng nhập
- Không có đặt phòng nào → Hiển thị "Bạn chưa có đặt phòng nào"
- Lỗi tải dữ liệu → Hiển thị thông báo lỗi và nút "Thử lại"

### 6. Hủy đặt phòng

**Quy trình:**
1. Người dùng xem danh sách đặt phòng hoặc chi tiết đặt phòng
2. Người dùng chọn "Hủy đặt phòng" (chỉ hiển thị nếu trạng thái cho phép)
3. Hệ thống hiển thị hộp thoại xác nhận hủy đặt phòng
4. Hệ thống hiển thị thông tin:
   - Chính sách hủy phòng của khách sạn
   - Phí hủy (nếu có)
   - Số tiền được hoàn lại (nếu có)
5. Người dùng xác nhận hủy đặt phòng
6. Hệ thống cập nhật trạng thái Booking thành CANCELLED
7. Hệ thống xử lý hoàn tiền (nếu áp dụng):
   - Tạo Payment record mới với trạng thái "refund"
   - Cập nhật trạng thái Bill
8. Hệ thống gửi thông báo hủy đặt phòng cho người dùng
9. Hệ thống gửi thông báo cho Hotel Owner (nếu có)
10. Hệ thống hiển thị thông báo thành công
11. Danh sách đặt phòng được cập nhật

**Ngoại lệ:**
- Đặt phòng không thể hủy (theo chính sách) → Hiển thị thông báo "Không thể hủy đặt phòng này"
- Đã quá thời hạn hủy → Hiển thị thông báo "Đã quá thời hạn hủy đặt phòng"
- Lỗi xử lý hủy → Hiển thị thông báo lỗi và yêu cầu liên hệ hỗ trợ

### 7. Claim và áp dụng Voucher

**Quy trình Claim Voucher:**
1. Người dùng đã đăng nhập chọn tab "Ưu đãi" (Voucher) trên thanh điều hướng
2. Hệ thống tải danh sách voucher có sẵi (status = ACTIVE) từ Firestore
3. Hệ thống hiển thị danh sách voucher:
   - Mã voucher (code)
   - Tiêu đề (title)
   - Mô tả (description)
   - Loại giảm giá (type: PERCENTAGE hoặc FIXED_AMOUNT)
   - Giá trị giảm giá (value)
   - Thời hạn hiệu lực (validFrom, validTo)
4. Người dùng chọn một voucher để xem chi tiết
5. Hệ thống hiển thị chi tiết voucher:
   - Điều kiện sử dụng (VoucherConditions):
     * Số tiền đặt phòng tối thiểu (minBookingAmount)
     * Giảm giá tối đa (maxDiscountAmount)
     * Chỉ áp dụng cho người dùng mới (applicableForNewUsers)
     * Số lần sử dụng tối đa mỗi người (maxUsagePerUser)
     * Số lần sử dụng tối đa tổng (maxTotalUsage)
     * Cấp độ người dùng yêu cầu (requiredUserLevel)
     * Ngày áp dụng (validDays)
     * Khung giờ áp dụng (validTimeSlots)
   - Danh sách khách sạn áp dụng (applicableHotelIds, null = áp dụng cho tất cả)
6. Người dùng chọn "Claim Voucher"
7. Hệ thống kiểm tra điều kiện đủ điều kiện (checkVoucherEligibility):
   - Voucher còn hiệu lực (validFrom ≤ now ≤ validTo)
   - Voucher chưa bị claim bởi người dùng này (kiểm tra VoucherClaim)
   - Chưa đạt giới hạn số lần sử dụng tổng (nếu có)
   - Chưa đạt giới hạn số lần sử dụng mỗi người (nếu có)
8. Nếu đủ điều kiện:
   - Hệ thống tạo VoucherClaim record mới (voucherId, userId, claimedAt)
   - Hệ thống cập nhật số lần sử dụng hiện tại (currentUsage) trong VoucherConditions
   - Hiển thị thông báo "Đã nhận voucher thành công"
9. Nếu không đủ điều kiện:
   - Hiển thị thông báo lý do không đủ điều kiện

**Quy trình Áp dụng Voucher vào Đặt phòng:**
1. Trong quy trình đặt phòng (bước 9 của quy trình đặt phòng), người dùng chọn "Áp dụng voucher"
2. Hệ thống tải danh sách voucher đã claim bởi người dùng
3. Hệ thống lọc voucher đủ điều kiện:
   - Voucher còn hiệu lực
   - Đơn đặt phòng đạt minBookingAmount
   - Khách sạn được chọn nằm trong applicableHotelIds (hoặc null)
   - Chưa vượt quá maxUsagePerUser
4. Hệ thống hiển thị danh sách voucher đủ điều kiện
5. Người dùng chọn voucher
6. Hệ thống tính toán giảm giá:
   - Nếu type = PERCENTAGE: discount = totalPrice × value / 100 (tối đa maxDiscountAmount)
   - Nếu type = FIXED_AMOUNT: discount = value (tối đa maxDiscountAmount)
7. Hệ thống cập nhật tổng tiền: totalPrice = subtotal - discount
8. Khi người dùng xác nhận đặt phòng:
   - Hệ thống tạo BookingVoucher record (bookingId, voucherId, discountAmount)
   - Hệ thống cập nhật appliedVouchers trong Booking

**Ngoại lệ:**
- Voucher đã hết hạn → Hiển thị thông báo "Voucher đã hết hạn"
- Voucher đã được claim → Hiển thị thông báo "Bạn đã nhận voucher này"
- Đã đạt giới hạn số lần sử dụng → Hiển thị thông báo "Voucher đã hết lượt sử dụng"
- Đơn đặt phòng chưa đạt minBookingAmount → Hiển thị thông báo "Đơn hàng chưa đủ điều kiện"
- Khách sạn không nằm trong danh sách áp dụng → Hiển thị thông báo "Voucher không áp dụng cho khách sạn này"

### 8. Viết đánh giá

**Quy trình:**
1. Sau khi hoàn thành chuyến đi (trạng thái Booking = COMPLETED), hệ thống gửi thông báo nhắc nhở viết đánh giá
2. Người dùng chọn thông báo hoặc vào "Chuyến đi" → Chọn đặt phòng đã hoàn thành → "Viết đánh giá"
3. Hệ thống chuyển đến màn hình viết đánh giá (Review screen)
4. Hệ thống hiển thị thông tin đặt phòng:
   - Tên khách sạn
   - Phòng đã ở
   - Ngày check-in và check-out
5. Người dùng nhập đánh giá:
   - Chọn số sao (rating): từ 1 đến 5
   - Nhập nội dung đánh giá (comment)
6. Người dùng nhấn "Gửi đánh giá"
7. Hệ thống kiểm tra:
   - Đã chọn số sao
   - Nội dung đánh giá không rỗng
   - Người dùng chưa đánh giá cho đặt phòng này
8. Hệ thống tạo Review record mới (userId, hotelId, comment, rating)
9. Hệ thống cập nhật thông tin khách sạn:
   - Tính lại điểm đánh giá trung bình (rating)
   - Tăng số lượt đánh giá (reviewCount)
10. Hệ thống gửi thông báo cho Hotel Owner (nếu có)
11. Hệ thống hiển thị thông báo "Đánh giá đã được gửi"
12. Hệ thống chuyển người dùng quay lại màn hình trước

**Ngoại lệ:**
- Chưa chọn số sao → Hiển thị thông báo "Vui lòng chọn số sao"
- Nội dung đánh giá rỗng → Hiển thị thông báo "Vui lòng nhập nội dung đánh giá"
- Đã đánh giá cho đặt phòng này → Hiển thị thông báo "Bạn đã đánh giá cho đặt phòng này"
- Lỗi gửi đánh giá → Hiển thị thông báo lỗi và nút "Thử lại"

### 9. Quản lý khách sạn (Hotel Owner)

**Quy trình Chỉnh sửa thông tin khách sạn:**
1. Hotel Owner đăng nhập với tài khoản có role = HOTEL_OWNER
2. Hotel Owner chọn menu "Quản lý khách sạn" → "Danh sách khách sạn"
3. Hệ thống hiển thị danh sách khách sạn được phân quyền quản lý (từ HotelOwnership với isActive = true)
4. Hotel Owner chọn một khách sạn
5. Hệ thống hiển thị màn hình quản lý khách sạn với các tab:
   - Thông tin chung
   - Phòng
   - Đặt phòng
   - Đánh giá
   - Báo cáo
6. Hotel Owner chọn tab "Thông tin chung" → "Chỉnh sửa"
7. Hệ thống hiển thị form chỉnh sửa với thông tin hiện tại
8. Hotel Owner chỉnh sửa các trường:
   - Tên, mô tả, địa chỉ, thành phố, quốc gia
   - Loại cơ sở, hạng sao
   - Tọa độ (latitude, longitude)
   - Upload/cập nhật hình ảnh
   - Thêm/sửa/xóa tiện ích
   - Thêm/sửa/xóa ngôn ngữ hỗ trợ
   - Thêm/sửa/xóa chính sách
9. Hotel Owner nhấn "Lưu"
10. Hệ thống kiểm tra quyền: Hotel Owner có quyền quản lý khách sạn này (kiểm tra HotelOwnership với permissions chứa MANAGE_INFO)
11. Hệ thống cập nhật Hotel record trong Firestore
12. Hệ thống upload hình ảnh mới lên Firebase Storage (nếu có)
13. Hệ thống hiển thị thông báo "Cập nhật thành công"

**Ngoại lệ:**
- Không có quyền quản lý khách sạn này → Hiển thị thông báo "Bạn không có quyền quản lý khách sạn này"
- Lỗi upload hình ảnh → Hiển thị thông báo lỗi và yêu cầu thử lại

### 10. Quản lý phòng (Hotel Owner)

**Quy trình Thêm phòng mới:**
1. Hotel Owner chọn khách sạn → Tab "Phòng" → "Thêm phòng"
2. Hệ thống hiển thị form nhập thông tin phòng:
   - Loại phòng (type) - bắt buộc
   - Giá mỗi đêm (price) - bắt buộc
   - Hình ảnh phòng (upload)
   - Sức chứa (capacity) - bắt buộc
   - Trạng thái (available) - mặc định true
   - Chi tiết phòng (RoomDetail):
     * Tên phòng
     * Diện tích (size)
     * Hướng nhìn (view)
3. Hotel Owner nhập thông tin và upload hình ảnh
4. Hotel Owner nhấn "Lưu"
5. Hệ thống kiểm tra quyền: Hotel Owner có quyền MANAGE_ROOMS
6. Hệ thống kiểm tra thông tin hợp lệ
7. Hệ thống tạo Room record mới với hotelId = khách sạn được chọn
8. Hệ thống upload hình ảnh lên Firebase Storage
9. Hệ thống hiển thị thông báo "Thêm phòng thành công"

**Quy trình Cập nhật giá phòng:**
1. Hotel Owner chọn một phòng từ danh sách
2. Hotel Owner chọn "Chỉnh sửa"
3. Hotel Owner cập nhật giá phòng
4. Hotel Owner nhấn "Lưu"
5. Hệ thống cập nhật Room record
6. Hệ thống kiểm tra: Nếu có đặt phòng đang hoạt động với giá cũ, có thể cảnh báo
7. Hệ thống hiển thị thông báo "Cập nhật thành công"

**Ngoại lệ:**
- Phòng có đặt phòng đang hoạt động → Có thể cảnh báo nhưng vẫn cho phép cập nhật
- Giá phòng không hợp lệ (≤ 0) → Hiển thị thông báo lỗi
- Không có quyền MANAGE_ROOMS → Hiển thị thông báo "Bạn không có quyền quản lý phòng"

### 11. Quản lý đặt phòng (Hotel Owner)

**Quy trình Xác nhận đặt phòng:**
1. Hotel Owner chọn khách sạn → Tab "Đặt phòng"
2. Hệ thống hiển thị danh sách đặt phòng với trạng thái PENDING
3. Hotel Owner chọn một đặt phòng
4. Hệ thống hiển thị chi tiết:
   - Thông tin khách hàng
   - Phòng đã đặt
   - Ngày check-in/check-out
   - Số khách, số phòng
   - Tổng tiền
   - Yêu cầu đặc biệt
5. Hotel Owner xem xét và chọn "Xác nhận đặt phòng"
6. Hệ thống kiểm tra quyền: Hotel Owner có quyền MANAGE_BOOKINGS
7. Hệ thống cập nhật trạng thái Booking từ PENDING → CONFIRMED
8. Hệ thống gửi thông báo xác nhận đặt phòng cho khách hàng
9. Hệ thống hiển thị thông báo "Đã xác nhận đặt phòng"

**Quy trình Check-in:**
1. Hotel Owner chọn đặt phòng có trạng thái CONFIRMED
2. Vào ngày check-in, Hotel Owner chọn "Check-in"
3. Hệ thống cập nhật trạng thái Booking từ CONFIRMED → CHECKED_IN
4. Hệ thống gửi thông báo cho khách hàng
5. Hệ thống hiển thị thông báo "Check-in thành công"

**Quy trình Check-out:**
1. Hotel Owner chọn đặt phòng có trạng thái CHECKED_IN
2. Vào ngày check-out, Hotel Owner chọn "Check-out"
3. Hệ thống cập nhật trạng thái Booking từ CHECKED_IN → CHECKED_OUT → COMPLETED
4. Hệ thống gửi thông báo hoàn thành chuyến đi cho khách hàng
5. Hệ thống gửi thông báo nhắc nhở viết đánh giá
6. Hệ thống hiển thị thông báo "Check-out thành công"

**Ngoại lệ:**
- Không có quyền MANAGE_BOOKINGS → Hiển thị thông báo "Bạn không có quyền quản lý đặt phòng"

### 12. Xem báo cáo khách sạn (Hotel Owner)

**Quy trình Xem báo cáo doanh thu:**
1. Hotel Owner chọn khách sạn → Tab "Báo cáo" → "Doanh thu"
2. Hệ thống kiểm tra quyền: Hotel Owner có quyền VIEW_REPORTS
3. Hệ thống hiển thị form chọn thời gian:
   - Chọn khoảng thời gian (ngày, tuần, tháng, năm)
   - Chọn ngày bắt đầu và kết thúc
4. Hotel Owner chọn thời gian và nhấn "Xem báo cáo"
5. Hệ thống tính toán và hiển thị:
   - Tổng doanh thu của khách sạn trong khoảng thời gian
   - Doanh thu theo ngày/tuần/tháng
   - Biểu đồ doanh thu
   - So sánh với kỳ trước
   - Doanh thu theo phòng
   - Top phòng có doanh thu cao nhất
6. Hotel Owner có thể xuất báo cáo ra file

**Quy trình Xem báo cáo tỷ lệ lấp đầy phòng:**
1. Hotel Owner chọn khách sạn → Tab "Báo cáo" → "Tỷ lệ lấp đầy"
2. Hệ thống hiển thị:
   - Tỷ lệ lấp đầy phòng theo ngày/tuần/tháng
   - Biểu đồ tỷ lệ lấp đầy
   - So sánh với kỳ trước
   - Phòng có tỷ lệ lấp đầy cao nhất/thấp nhất
3. Hotel Owner có thể lọc theo khoảng thời gian

**Ngoại lệ:**
- Không có quyền VIEW_REPORTS → Hiển thị thông báo "Bạn không có quyền xem báo cáo"

### 13. Quản lý người dùng (Admin)

**Quy trình Xem danh sách người dùng:**
1. Admin đăng nhập vào hệ thống với tài khoản có role = ADMIN
2. Admin chọn menu "Quản lý" → "Người dùng"
3. Hệ thống tải danh sách tất cả người dùng từ Firestore
4. Hệ thống hiển thị danh sách người dùng với thông tin:
   - Email
   - Họ tên
   - Role (USER, HOTEL_OWNER, ADMIN)
   - Ngày đăng ký
   - Trạng thái tài khoản (ACTIVE, LOCKED)
   - Số đặt phòng
5. Admin có thể:
   - Tìm kiếm người dùng theo email hoặc tên
   - Lọc người dùng theo role, trạng thái
   - Sắp xếp theo ngày đăng ký, số đặt phòng
   - Chọn một người dùng để xem chi tiết

**Quy trình Thêm người dùng mới:**
1. Admin chọn "Thêm người dùng mới"
2. Hệ thống hiển thị form nhập thông tin:
   - Email (bắt buộc)
   - Mật khẩu (bắt buộc)
   - Họ tên (bắt buộc)
   - Giới tính
   - Ngày sinh
   - Role (USER, HOTEL_OWNER, ADMIN)
3. Admin nhập thông tin và nhấn "Tạo tài khoản"
4. Hệ thống kiểm tra:
   - Email chưa được sử dụng
   - Mật khẩu đủ mạnh
   - Thông tin hợp lệ
5. Hệ thống tạo tài khoản User mới trong Firebase Authentication và Firestore
6. Hệ thống hiển thị thông báo "Tạo tài khoản thành công"
7. Danh sách người dùng được cập nhật

**Quy trình Xóa người dùng:**
1. Admin chọn một người dùng từ danh sách
2. Admin chọn "Xóa người dùng"
3. Hệ thống kiểm tra: Admin không thể xóa chính mình
4. Hệ thống hiển thị hộp thoại xác nhận:
   - Cảnh báo về việc xóa người dùng
   - Thông tin về số đặt phòng đang có
   - Lựa chọn: Xóa hoàn toàn hoặc Vô hiệu hóa
5. Admin xác nhận xóa
6. Nếu chọn "Vô hiệu hóa":
   - Hệ thống cập nhật trạng thái User thành "LOCKED"
   - Người dùng không thể đăng nhập
   - Dữ liệu vẫn được giữ lại
7. Nếu chọn "Xóa hoàn toàn":
   - Hệ thống kiểm tra người dùng không có đặt phòng đang hoạt động
   - Hệ thống xóa User record khỏi Firestore
   - Hệ thống xóa tài khoản khỏi Firebase Authentication
8. Hệ thống hiển thị thông báo thành công
9. Danh sách người dùng được cập nhật

**Ngoại lệ:**
- Email đã được sử dụng → Hiển thị thông báo "Email đã tồn tại"
- Người dùng có đặt phòng đang hoạt động → Không cho phép xóa hoàn toàn, chỉ cho phép vô hiệu hóa
- Admin không thể xóa chính mình → Hiển thị thông báo "Không thể xóa tài khoản của chính bạn"
- Lỗi xóa tài khoản → Hiển thị thông báo lỗi và yêu cầu thử lại

### 14. Quản lý khách sạn (Admin)

**Quy trình Thêm khách sạn mới:**
1. Admin chọn menu "Quản lý" → "Khách sạn" → "Thêm khách sạn"
2. Hệ thống hiển thị form nhập thông tin khách sạn:
   - Tên khách sạn (bắt buộc)
   - Mô tả
   - Địa chỉ (bắt buộc)
   - Thành phố (bắt buộc)
   - Quốc gia (bắt buộc)
   - Loại cơ sở (HOTEL, RESORT)
   - Hạng sao (0-5)
   - Tọa độ (latitude, longitude)
   - Danh sách hình ảnh (upload nhiều ảnh)
   - Danh sách tiện ích (features)
   - Danh sách ngôn ngữ hỗ trợ
   - Danh sách chính sách (policies)
3. Admin nhập thông tin và upload hình ảnh
4. Admin nhấn "Lưu"
5. Hệ thống kiểm tra thông tin hợp lệ
6. Hệ thống tạo Hotel record mới trong Firestore
7. Hệ thống upload hình ảnh lên Firebase Storage
8. Hệ thống hiển thị thông báo "Thêm khách sạn thành công"
9. Admin có thể tiếp tục thêm phòng cho khách sạn hoặc phân quyền cho Hotel Owner

**Quy trình Phân quyền Hotel Owner cho khách sạn:**
1. Admin chọn một khách sạn từ danh sách
2. Admin chọn "Phân quyền quản lý"
3. Hệ thống hiển thị form:
   - Tìm kiếm người dùng (theo email hoặc tên)
   - Chọn người dùng có role = HOTEL_OWNER
   - Chọn vai trò (OWNER, MANAGER)
   - Chọn quyền (MANAGE_INFO, MANAGE_ROOMS, VIEW_REPORTS, MANAGE_BOOKINGS)
4. Admin chọn người dùng và quyền, nhấn "Phân quyền"
5. Hệ thống kiểm tra: Người dùng có role = HOTEL_OWNER
6. Hệ thống tạo HotelOwnership record mới (userId, hotelId, role, permissions, isActive = true)
7. Hệ thống hiển thị thông báo "Phân quyền thành công"

**Quy trình Xóa khách sạn:**
1. Admin chọn một khách sạn từ danh sách
2. Admin chọn "Xóa"
3. Hệ thống kiểm tra:
   - Khách sạn không có đặt phòng đang hoạt động
   - Khách sạn không có đánh giá (hoặc có thể xóa cùng đánh giá)
4. Hệ thống hiển thị hộp thoại xác nhận
5. Admin xác nhận xóa
6. Hệ thống xóa Hotel record và các Room liên quan
7. Hệ thống xóa HotelOwnership records liên quan
8. Hệ thống xóa hình ảnh khỏi Firebase Storage
9. Hệ thống hiển thị thông báo "Xóa khách sạn thành công"

**Ngoại lệ:**
- Khách sạn có đặt phòng đang hoạt động → Hiển thị thông báo "Không thể xóa khách sạn có đặt phòng đang hoạt động"
- Lỗi upload hình ảnh → Hiển thị thông báo lỗi và yêu cầu thử lại

### 15. Quản lý voucher (Admin)

**Quy trình Tạo voucher mới:**
1. Admin chọn menu "Quản lý" → "Voucher" → "Tạo voucher"
2. Hệ thống hiển thị form nhập thông tin voucher:
   - Mã voucher (code) - bắt buộc, duy nhất
   - Tiêu đề (title) - bắt buộc
   - Mô tả (description)
   - Loại giảm giá (PERCENTAGE, FIXED_AMOUNT)
   - Giá trị giảm giá (value) - bắt buộc
   - Thời hạn hiệu lực (validFrom, validTo) - bắt buộc
   - Danh sách khách sạn áp dụng (null = tất cả)
   - Điều kiện sử dụng (VoucherConditions):
     * Số tiền đặt phòng tối thiểu
     * Giảm giá tối đa
     * Chỉ áp dụng cho người dùng mới
     * Số lần sử dụng tối đa mỗi người
     * Số lần sử dụng tối đa tổng
     * Cấp độ người dùng yêu cầu
     * Ngày áp dụng
     * Khung giờ áp dụng
3. Admin nhập thông tin và nhấn "Tạo voucher"
4. Hệ thống kiểm tra:
   - Mã voucher chưa tồn tại
   - Thời hạn hiệu lực hợp lệ (validTo > validFrom)
   - Giá trị giảm giá hợp lệ
5. Hệ thống tạo Voucher record mới với status = ACTIVE
6. Hệ thống hiển thị thông báo "Tạo voucher thành công"

**Quy trình Xóa/Vô hiệu hóa voucher:**
1. Admin chọn một voucher từ danh sách
2. Admin chọn "Xóa" hoặc "Vô hiệu hóa"
3. Nếu chọn "Vô hiệu hóa":
   - Hệ thống cập nhật status = INACTIVE
   - Voucher không còn hiển thị cho người dùng
4. Nếu chọn "Xóa":
   - Hệ thống kiểm tra voucher chưa được sử dụng
   - Hệ thống xóa Voucher record và VoucherConditions
5. Hệ thống hiển thị thông báo thành công

**Ngoại lệ:**
- Mã voucher đã tồn tại → Hiển thị thông báo "Mã voucher đã tồn tại"
- Voucher đã được sử dụng → Không cho phép xóa, chỉ cho phép vô hiệu hóa
- Thời hạn hiệu lực không hợp lệ → Hiển thị thông báo lỗi

### 16. Hỗ trợ khách hàng (Admin Support)

**Quy trình Xem yêu cầu hỗ trợ:**
1. Admin chọn menu "Hỗ trợ" → "Yêu cầu hỗ trợ"
2. Hệ thống tải danh sách yêu cầu hỗ trợ từ Firestore (SupportRequest collection)
3. Hệ thống hiển thị danh sách với thông tin:
   - Người dùng gửi yêu cầu
   - Loại yêu cầu (CANCELLATION, REFUND, COMPLAINT, QUESTION, OTHER)
   - Trạng thái (PENDING, IN_PROGRESS, RESOLVED, CLOSED)
   - Thời gian tạo
4. Admin có thể:
   - Lọc theo trạng thái
   - Tìm kiếm theo email người dùng
   - Sắp xếp theo thời gian
   - Chọn một yêu cầu để xem chi tiết

**Quy trình Phản hồi yêu cầu hỗ trợ:**
1. Admin chọn một yêu cầu hỗ trợ
2. Hệ thống hiển thị chi tiết:
   - Thông tin người dùng
   - Nội dung yêu cầu
   - Lịch sử trao đổi (SupportMessage)
   - Thông tin liên quan (đặt phòng, hóa đơn, v.v.)
3. Admin có thể:
   - Cập nhật trạng thái (IN_PROGRESS, RESOLVED)
   - Gửi phản hồi (tạo Notification cho người dùng)
   - Thực hiện hành động (hủy đặt phòng, hoàn tiền, v.v.)
4. Admin nhập phản hồi và nhấn "Gửi"
5. Hệ thống tạo SupportMessage record mới (supportRequestId, senderId = adminId, senderType = ADMIN, message, createdAt)
6. Hệ thống tạo Notification cho người dùng
7. Hệ thống cập nhật trạng thái SupportRequest
8. Hệ thống lưu lịch sử trao đổi

### 17. Xem báo cáo và thống kê (Admin)

**Quy trình Xem báo cáo doanh thu:**
1. Admin chọn menu "Báo cáo" → "Doanh thu"
2. Hệ thống hiển thị form chọn thời gian:
   - Chọn khoảng thời gian (ngày, tuần, tháng, năm)
   - Chọn ngày bắt đầu và kết thúc
3. Admin chọn thời gian và nhấn "Xem báo cáo"
4. Hệ thống tính toán và hiển thị:
   - Tổng doanh thu trong khoảng thời gian (toàn hệ thống)
   - Doanh thu theo ngày/tuần/tháng
   - Biểu đồ doanh thu
   - So sánh với kỳ trước
   - Top khách sạn có doanh thu cao nhất
   - Doanh thu theo khách sạn
5. Admin có thể xuất báo cáo ra file (PDF, Excel)

**Quy trình Xem báo cáo đặt phòng:**
1. Admin chọn menu "Báo cáo" → "Đặt phòng"
2. Hệ thống hiển thị thống kê:
   - Tổng số đặt phòng
   - Số đặt phòng theo trạng thái
   - Tỷ lệ hủy đặt phòng
   - Tỷ lệ hoàn thành
   - Số đặt phòng theo khách sạn
   - Biểu đồ xu hướng đặt phòng
3. Admin có thể lọc theo khoảng thời gian

**Quy trình Xem báo cáo người dùng:**
1. Admin chọn menu "Báo cáo" → "Người dùng"
2. Hệ thống hiển thị thống kê:
   - Tổng số người dùng
   - Số người dùng mới (theo thời gian)
   - Số người dùng hoạt động (có đặt phòng trong tháng)
   - Tỷ lệ người dùng sử dụng voucher
   - Top người dùng có nhiều đặt phòng nhất
   - Phân bố người dùng theo role

### 18. Gửi thông báo (Admin)

**Quy trình Gửi thông báo cho một người dùng:**
1. Admin chọn menu "Thông báo" → "Gửi thông báo"
2. Hệ thống hiển thị form:
   - Chọn người nhận: Một người dùng cụ thể hoặc Nhiều người dùng
   - Loại thông báo (GENERAL, PROMOTION, SYSTEM, v.v.)
   - Tiêu đề (bắt buộc)
   - Nội dung (bắt buộc)
   - Dữ liệu bổ sung (bookingId, voucherId, v.v.)
   - Lên lịch gửi (tùy chọn)
3. Nếu chọn "Một người dùng":
   - Admin tìm kiếm và chọn người dùng
4. Nếu chọn "Nhiều người dùng":
   - Admin có thể chọn theo: Tất cả, Theo điều kiện (người dùng mới, người dùng có đặt phòng, v.v.)
5. Admin nhập nội dung và nhấn "Gửi"
6. Hệ thống tạo Notification record(s) cho người dùng được chọn
7. Hệ thống gửi push notification (nếu người dùng bật)
8. Hệ thống hiển thị thông báo "Gửi thông báo thành công"
9. Nếu có lên lịch, hệ thống sẽ gửi vào thời điểm đã chọn

**Ngoại lệ:**
- Không chọn người nhận → Hiển thị thông báo "Vui lòng chọn người nhận"
- Nội dung rỗng → Hiển thị thông báo "Vui lòng nhập nội dung"

## Information about Related Objects

### User
- **id**: String - Mã định danh duy nhất của người dùng (từ Firebase Authentication)
- **email**: String - Địa chỉ email (dùng để đăng nhập)
- **password**: String - Mật khẩu (được mã hóa, lưu trong Firebase Authentication)
- **fullName**: String - Họ và tên đầy đủ
- **gender**: String - Giới tính
- **photoUrl**: String - URL ảnh đại diện
- **dateOfBirth**: Date - Ngày sinh
- **role**: UserRole - Vai trò người dùng (USER, HOTEL_OWNER, ADMIN) - mặc định là USER
- **status**: UserStatus - Trạng thái tài khoản (ACTIVE, LOCKED) - mặc định là ACTIVE
- **createdAt**: Timestamp - Thời điểm tạo tài khoản
- **updatedAt**: Timestamp - Thời điểm cập nhật cuối

### Hotel
- **id**: String - Mã định danh duy nhất của khách sạn
- **name**: String - Tên khách sạn
- **description**: String - Mô tả chi tiết về khách sạn
- **formattedAddress**: String - Địa chỉ đầy đủ (đã định dạng)
- **city**: String - Thành phố
- **country**: String - Quốc gia
- **propertyType**: PropertyType - Loại cơ sở (HOTEL, RESORT)
- **starRating**: Integer - Hạng sao (0-5)
- **reviewCount**: Integer - Số lượt đánh giá
- **rating**: Double - Điểm đánh giá trung bình (0.0-5.0)
- **images**: List<String> - Danh sách URL hình ảnh
- **languages**: List<String> - Danh sách ngôn ngữ hỗ trợ
- **features**: List<String> - Danh sách tiện ích (wifi, pool, parking, etc.)
- **minPrice**: Double - Giá phòng thấp nhất
- **coordinate**: Coordinate - Tọa độ địa lý (để hiển thị bản đồ)
- **policies**: List<Policy> - Danh sách chính sách
- **rooms**: List<Room> - Danh sách phòng (có thể load riêng)

### Coordinate
- **latitude**: Double - Vĩ độ
- **longitude**: Double - Kinh độ

### Policy
- **title**: String - Tiêu đề chính sách (ví dụ: "Chính sách hủy phòng")
- **content**: String - Nội dung chi tiết chính sách

### Room
- **id**: String - Mã định danh duy nhất của phòng
- **hotelId**: String - Mã khách sạn chứa phòng này
- **type**: String - Loại phòng (single, double, twin, suite, etc.)
- **price**: Double - Giá mỗi đêm
- **imageUrl**: String - URL hình ảnh phòng
- **capacity**: Integer - Sức chứa (số người tối đa)
- **available**: Boolean - Trạng thái còn phòng
- **detail**: RoomDetail - Chi tiết phòng

### RoomDetail
- **name**: String - Tên phòng
- **size**: Double - Diện tích (m²)
- **view**: String - Hướng nhìn (sea view, garden view, city view, etc.)

### Booking
- **id**: String - Mã định danh duy nhất của đặt phòng
- **userId**: String - Mã người dùng đặt phòng
- **hotelId**: String - Mã khách sạn
- **roomId**: String - Mã phòng
- **dateFrom**: String - Ngày check-in (format: yyyy-MM-dd)
- **dateTo**: String - Ngày check-out (format: yyyy-MM-dd)
- **guests**: Integer - Tổng số khách
- **adults**: Integer - Số người lớn
- **children**: Integer - Số trẻ em
- **rooms**: Integer - Số phòng
- **price**: Double - Giá phòng (base price)
- **originalPrice**: Double - Giá gốc (trước giảm giá)
- **discount**: Double - Tổng giảm giá
- **serviceFee**: Double - Phí dịch vụ
- **taxes**: Double - Thuế
- **totalPrice**: Double - Tổng tiền phải thanh toán
- **status**: BookingStatus - Trạng thái đặt phòng (PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, COMPLETED, CANCELLED, REFUNDED)
- **paymentMethod**: PaymentMethod - Phương thức thanh toán (CREDIT_CARD, DEBIT_CARD, DIGITAL_WALLET, BANK_TRANSFER, CASH)
- **specialRequests**: String - Ghi chú đặc biệt
- **preferences**: BookingPreferences - Tùy chọn đặc biệt
- **appliedVouchers**: List<String> - Danh sách mã voucher đã áp dụng
- **createdAt**: Timestamp - Thời điểm tạo đặt phòng
- **updatedAt**: Timestamp - Thời điểm cập nhật cuối
- **hotel**: Hotel - Thông tin khách sạn (có thể load riêng)
- **room**: Room - Thông tin phòng (có thể load riêng)

### BookingPreferences
- **highFloor**: Boolean - Yêu cầu tầng cao
- **quietRoom**: Boolean - Yêu cầu phòng yên tĩnh
- **extraPillows**: Boolean - Yêu cầu thêm gối
- **airportShuttle**: Boolean - Yêu cầu đưa đón sân bay
- **earlyCheckIn**: Boolean - Yêu cầu check-in sớm
- **lateCheckOut**: Boolean - Yêu cầu check-out muộn

### Voucher
- **id**: String - Mã định danh duy nhất của voucher
- **code**: String - Mã voucher (để người dùng nhập)
- **title**: String - Tiêu đề voucher
- **description**: String - Mô tả voucher
- **type**: VoucherType - Loại giảm giá (PERCENTAGE, FIXED_AMOUNT)
- **value**: Double - Giá trị giảm giá (% hoặc số tiền)
- **status**: VoucherStatus - Trạng thái (ACTIVE, INACTIVE, EXPIRED)
- **applicableHotelIds**: List<String> - Danh sách mã khách sạn áp dụng (null = áp dụng cho tất cả)
- **validFrom**: Timestamp - Thời điểm bắt đầu hiệu lực
- **validTo**: Timestamp - Thời điểm kết thúc hiệu lực
- **conditions**: VoucherConditions - Điều kiện sử dụng
- **createdAt**: Timestamp - Thời điểm tạo
- **updatedAt**: Timestamp - Thời điểm cập nhật cuối

### VoucherConditions
- **minBookingAmount**: Double - Số tiền đặt phòng tối thiểu
- **maxDiscountAmount**: Double - Giảm giá tối đa
- **applicableForNewUsers**: Boolean - Chỉ áp dụng cho người dùng mới
- **applicableForExistingUsers**: Boolean - Áp dụng cho người dùng cũ
- **maxUsagePerUser**: Integer - Số lần sử dụng tối đa mỗi người
- **maxTotalUsage**: Integer - Số lần sử dụng tối đa tổng (0 = không giới hạn)
- **currentUsage**: Integer - Số lần đã sử dụng
- **requiredUserLevel**: String - Cấp độ người dùng yêu cầu (VIP, GOLD, SILVER, etc.)
- **validDays**: List<String> - Ngày áp dụng (MONDAY, TUESDAY, etc.)
- **validTimeSlots**: List<String> - Khung giờ áp dụng (MORNING, AFTERNOON, EVENING)

### VoucherClaim (Association Class)
- **voucherId**: String - Mã voucher
- **userId**: String - Mã người dùng
- **claimedAt**: Timestamp - Thời điểm claim voucher

### BookingVoucher (Association Class)
- **bookingId**: String - Mã đặt phòng
- **voucherId**: String - Mã voucher
- **discountAmount**: Double - Số tiền giảm giá đã áp dụng

### Review (Association Class)
- **id**: String - Mã định danh duy nhất của đánh giá
- **userId**: String - Mã người dùng viết đánh giá
- **hotelId**: String - Mã khách sạn được đánh giá
- **comment**: String - Nội dung đánh giá
- **rating**: Integer - Số sao (1-5)
- **createdAt**: Timestamp - Thời điểm tạo đánh giá

### Bookmark (Association Class)
- **id**: String - Mã định danh duy nhất
- **userId**: String - Mã người dùng
- **hotelId**: String - Mã khách sạn
- **createdAt**: Timestamp - Thời điểm lưu vào yêu thích

### Notification
- **id**: String - Mã định danh duy nhất
- **userId**: String - Mã người dùng nhận thông báo
- **title**: String - Tiêu đề thông báo
- **message**: String - Nội dung thông báo
- **type**: NotificationType - Loại thông báo (GENERAL, BOOKING_CONFIRMATION, BOOKING_CANCELLED, PAYMENT_SUCCESS, PAYMENT_FAILED, REVIEW_REMINDER, PROMOTION, SYSTEM)
- **read**: Boolean - Đã đọc chưa
- **createdAt**: Timestamp - Thời điểm tạo
- **updatedAt**: Timestamp - Thời điểm cập nhật
- **data**: Map<String, String> - Dữ liệu bổ sung (ví dụ: bookingId, voucherId)

### Bill
- **id**: String - Mã định danh duy nhất
- **bookingId**: String - Mã đặt phòng (1-1 với Booking)
- **amount**: Double - Tổng số tiền
- **paymentMethod**: String - Phương thức thanh toán
- **status**: String - Trạng thái (pending, paid, refunded)
- **createdAt**: Timestamp - Thời điểm tạo
- **paidAt**: Timestamp - Thời điểm thanh toán (null nếu chưa thanh toán)
- **payments**: List<Payment> - Danh sách giao dịch thanh toán

### Payment
- **id**: String - Mã định danh duy nhất
- **billId**: String - Mã hóa đơn
- **amount**: Double - Số tiền
- **paymentMethod**: String - Phương thức thanh toán
- **status**: String - Trạng thái (success, failed, pending, refund)
- **paymentDate**: Timestamp - Thời điểm thanh toán
- **gateway**: String - Cổng thanh toán (stripe, paypal, etc.)
- **transactionCode**: String - Mã giao dịch từ cổng thanh toán

### HotelOwnership (Association Class)
- **id**: String - Mã định danh duy nhất
- **userId**: String - Mã người dùng (HOTEL_OWNER)
- **hotelId**: String - Mã khách sạn
- **role**: String - Vai trò trong khách sạn (OWNER, MANAGER)
- **permissions**: List<String> - Danh sách quyền (MANAGE_INFO, MANAGE_ROOMS, VIEW_REPORTS, MANAGE_BOOKINGS)
- **createdAt**: Timestamp - Thời điểm phân quyền
- **isActive**: Boolean - Trạng thái hoạt động

### SupportRequest
- **id**: String - Mã định danh duy nhất
- **userId**: String - Mã người dùng gửi yêu cầu
- **type**: SupportRequestType - Loại yêu cầu (CANCELLATION, REFUND, COMPLAINT, QUESTION, OTHER)
- **subject**: String - Tiêu đề yêu cầu
- **description**: String - Mô tả chi tiết
- **status**: SupportRequestStatus - Trạng thái (PENDING, IN_PROGRESS, RESOLVED, CLOSED)
- **relatedBookingId**: String - Mã đặt phòng liên quan (nếu có)
- **createdAt**: Timestamp - Thời điểm tạo
- **updatedAt**: Timestamp - Thời điểm cập nhật cuối
- **resolvedAt**: Timestamp - Thời điểm giải quyết
- **resolvedBy**: String - Mã admin giải quyết

### SupportMessage
- **id**: String - Mã định danh duy nhất
- **supportRequestId**: String - Mã yêu cầu hỗ trợ
- **senderId**: String - Mã người gửi (userId hoặc adminId)
- **senderType**: String - Loại người gửi (USER, ADMIN)
- **message**: String - Nội dung tin nhắn
- **createdAt**: Timestamp - Thời điểm gửi

### Report
- **id**: String - Mã định danh duy nhất
- **type**: ReportType - Loại báo cáo (REVENUE, BOOKING, USER, HOTEL, VOUCHER, REVIEW)
- **period**: String - Khoảng thời gian (DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM)
- **startDate**: Date - Ngày bắt đầu
- **endDate**: Date - Ngày kết thúc
- **data**: Map<String, Object> - Dữ liệu báo cáo (tổng hợp)
- **createdAt**: Timestamp - Thời điểm tạo
- **createdBy**: String - Mã admin hoặc hotel owner tạo báo cáo

## Relation among Objects

### Quan hệ 1-n (One-to-Many)

1. **User → Booking**: Một người dùng có thể có nhiều đặt phòng. Mỗi đặt phòng thuộc về một người dùng.

2. **Hotel → Room**: Một khách sạn có nhiều phòng. Mỗi phòng thuộc về một khách sạn.

3. **Hotel → Policy**: Một khách sạn có nhiều chính sách (chính sách hủy phòng, chính sách check-in/out, v.v.). Mỗi chính sách thuộc về một khách sạn.

4. **Hotel → Coordinate**: Một khách sạn có một tọa độ địa lý (composition). Mỗi tọa độ thuộc về một khách sạn.

5. **Room → RoomDetail**: Một phòng có một chi tiết phòng (composition). Mỗi chi tiết phòng thuộc về một phòng.

6. **Booking → BookingPreferences**: Một đặt phòng có một bộ tùy chọn đặc biệt (composition). Mỗi bộ tùy chọn thuộc về một đặt phòng.

7. **Booking → Bill**: Một đặt phòng có một hóa đơn (1-1, composition). Mỗi hóa đơn thuộc về một đặt phòng.

8. **Bill → Payment**: Một hóa đơn có thể có nhiều giao dịch thanh toán (nếu thanh toán nhiều lần hoặc hoàn tiền). Mỗi giao dịch thanh toán thuộc về một hóa đơn.

9. **User → Notification**: Một người dùng có thể có nhiều thông báo. Mỗi thông báo thuộc về một người dùng.

10. **Voucher → VoucherConditions**: Một voucher có một bộ điều kiện sử dụng (composition). Mỗi bộ điều kiện thuộc về một voucher.

11. **User → SupportRequest**: Một người dùng có thể có nhiều yêu cầu hỗ trợ. Mỗi yêu cầu hỗ trợ thuộc về một người dùng.

12. **SupportRequest → SupportMessage**: Một yêu cầu hỗ trợ có nhiều tin nhắn trao đổi. Mỗi tin nhắn thuộc về một yêu cầu hỗ trợ.

13. **Admin/Hotel Owner → Report**: Một admin hoặc hotel owner có thể tạo nhiều báo cáo. Mỗi báo cáo được tạo bởi một người dùng.

### Quan hệ n-n (Many-to-Many) - Sử dụng Association Class

1. **User ↔ Hotel (qua Review)**: Một người dùng có thể đánh giá nhiều khách sạn. Một khách sạn có thể được đánh giá bởi nhiều người dùng. Quan hệ này được thể hiện qua lớp **Review** (association class) chứa thông tin: userId, hotelId, comment, rating.

2. **User ↔ Hotel (qua Bookmark)**: Một người dùng có thể lưu nhiều khách sạn vào yêu thích. Một khách sạn có thể được lưu bởi nhiều người dùng. Quan hệ này được thể hiện qua lớp **Bookmark** (association class) chứa thông tin: userId, hotelId, createdAt.

3. **User ↔ Voucher (qua VoucherClaim)**: Một người dùng có thể claim nhiều voucher. Một voucher có thể được claim bởi nhiều người dùng. Quan hệ này được thể hiện qua lớp **VoucherClaim** (association class) chứa thông tin: userId, voucherId, claimedAt.

4. **Booking ↔ Voucher (qua BookingVoucher)**: Một đặt phòng có thể áp dụng nhiều voucher (theo thiết kế hiện tại, có thể áp dụng nhiều voucher). Một voucher có thể được áp dụng cho nhiều đặt phòng. Quan hệ này được thể hiện qua lớp **BookingVoucher** (association class) chứa thông tin: bookingId, voucherId, discountAmount.

5. **User ↔ Hotel (qua HotelOwnership)**: Một người dùng (HOTEL_OWNER) có thể quản lý nhiều khách sạn. Một khách sạn có thể được quản lý bởi nhiều người dùng (nhiều manager). Quan hệ này được thể hiện qua lớp **HotelOwnership** (association class) chứa thông tin: userId, hotelId, role, permissions, isActive.

### Quan hệ tham chiếu (Reference)

1. **Booking → Hotel**: Mỗi đặt phòng tham chiếu đến một khách sạn (foreign key: hotelId). Đây là quan hệ n-1.

2. **Booking → Room**: Mỗi đặt phòng tham chiếu đến một phòng (foreign key: roomId). Đây là quan hệ n-1.

3. **Review → User**: Mỗi đánh giá tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

4. **Review → Hotel**: Mỗi đánh giá tham chiếu đến một khách sạn (foreign key: hotelId). Đây là quan hệ n-1.

5. **Bookmark → User**: Mỗi bookmark tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

6. **Bookmark → Hotel**: Mỗi bookmark tham chiếu đến một khách sạn (foreign key: hotelId). Đây là quan hệ n-1.

7. **VoucherClaim → User**: Mỗi voucher claim tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

8. **VoucherClaim → Voucher**: Mỗi voucher claim tham chiếu đến một voucher (foreign key: voucherId). Đây là quan hệ n-1.

9. **BookingVoucher → Booking**: Mỗi booking voucher tham chiếu đến một đặt phòng (foreign key: bookingId). Đây là quan hệ n-1.

10. **BookingVoucher → Voucher**: Mỗi booking voucher tham chiếu đến một voucher (foreign key: voucherId). Đây là quan hệ n-1.

11. **Bill → Booking**: Mỗi hóa đơn tham chiếu đến một đặt phòng (foreign key: bookingId). Đây là quan hệ 1-1.

12. **Payment → Bill**: Mỗi giao dịch thanh toán tham chiếu đến một hóa đơn (foreign key: billId). Đây là quan hệ n-1.

13. **Notification → User**: Mỗi thông báo tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

14. **HotelOwnership → User**: Mỗi hotel ownership tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

15. **HotelOwnership → Hotel**: Mỗi hotel ownership tham chiếu đến một khách sạn (foreign key: hotelId). Đây là quan hệ n-1.

16. **SupportRequest → User**: Mỗi yêu cầu hỗ trợ tham chiếu đến một người dùng (foreign key: userId). Đây là quan hệ n-1.

17. **SupportRequest → Booking**: Mỗi yêu cầu hỗ trợ có thể tham chiếu đến một đặt phòng (foreign key: relatedBookingId). Đây là quan hệ n-1 (optional).

18. **SupportMessage → SupportRequest**: Mỗi tin nhắn hỗ trợ tham chiếu đến một yêu cầu hỗ trợ (foreign key: supportRequestId). Đây là quan hệ n-1.

19. **Report → User**: Mỗi báo cáo tham chiếu đến người tạo (foreign key: createdBy). Đây là quan hệ n-1.

### Ràng buộc nghiệp vụ (Business Constraints)

1. **Ràng buộc về đặt phòng:**
   - Một phòng chỉ có thể được đặt nếu còn trống trong khoảng thời gian check-in đến check-out.
   - Ngày check-out phải sau ngày check-in.
   - Số khách (adults + children) không được vượt quá sức chứa của phòng (capacity × rooms).

2. **Ràng buộc về voucher:**
   - Một voucher chỉ có thể được claim nếu còn hiệu lực (validFrom ≤ now ≤ validTo) và chưa hết lượt sử dụng.
   - Một voucher chỉ có thể được áp dụng vào đặt phòng nếu đơn hàng đạt minBookingAmount và khách sạn nằm trong danh sách áp dụng.
   - Một người dùng chỉ có thể claim một voucher một lần (trừ khi maxUsagePerUser > 1).

3. **Ràng buộc về đánh giá:**
   - Một người dùng chỉ có thể đánh giá một khách sạn một lần cho mỗi đặt phòng đã hoàn thành.
   - Chỉ có thể đánh giá sau khi đã hoàn thành chuyến đi (status = COMPLETED).

4. **Ràng buộc về thanh toán:**
   - Tổng số tiền thanh toán (từ các Payment) không được vượt quá số tiền trong Bill.
   - Một Bill chỉ có thể được thanh toán khi Booking có trạng thái CONFIRMED hoặc sau đó.

5. **Ràng buộc về trạng thái Booking:**
   - Trạng thái Booking phải tuân theo thứ tự: PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT → COMPLETED.
   - Có thể chuyển từ bất kỳ trạng thái nào (trừ COMPLETED) sang CANCELLED.
   - Có thể chuyển từ CANCELLED sang REFUNDED nếu đã hoàn tiền.

6. **Ràng buộc về Admin:**
   - Chỉ người dùng có role = ADMIN mới có thể truy cập các chức năng quản trị.
   - Admin không thể xóa chính tài khoản của mình.
   - Admin không thể thay đổi role của chính mình.

7. **Ràng buộc về Hotel Owner:**
   - Hotel Owner chỉ có thể quản lý khách sạn được phân quyền (có HotelOwnership record với isActive = true).
   - Hotel Owner không thể xóa khách sạn, chỉ có thể cập nhật thông tin.
   - Hotel Owner không thể xóa phòng có đặt phòng đang hoạt động.
   - Hotel Owner chỉ có thể xem báo cáo của khách sạn được phân quyền.
   - Admin có thể phân quyền Hotel Owner cho khách sạn (tạo HotelOwnership record).

8. **Ràng buộc về SupportRequest:**
   - Một người dùng chỉ có thể tạo yêu cầu hỗ trợ cho đặt phòng của chính mình.
   - Yêu cầu hỗ trợ phải được giải quyết bởi admin trước khi đóng.
   - Chỉ admin mới có thể cập nhật trạng thái SupportRequest thành RESOLVED hoặc CLOSED.

