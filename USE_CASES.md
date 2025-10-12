# ChillStay - Use Cases Documentation

## 📋 Overview
This document provides detailed information about all use cases implemented in the ChillStay application. Use cases encapsulate business logic and provide a clean interface between the presentation layer and data layer.

## 🏗️ Architecture
- **Clean Architecture**: Use cases are part of the Domain layer
- **Dependency Injection**: Using Koin for DI
- **Error Handling**: Using Result wrapper for success/failure states
- **Validation**: Business rules and input validation included

---

## 🏨 Hotel Use Cases

### 1. GetHotelsUseCase
**Purpose**: Retrieve all available hotels

**Location**: `domain/usecase/hotel/GetHotelsUseCase.kt`

**Parameters**: None

**Returns**: `Result<List<Hotel>>`

**Business Logic**:
- Fetches all hotels from repository
- Returns success with hotel list or failure with exception

**Usage Example**:
```kotlin
val result = getHotelsUseCase()
when (result) {
    is Result.Success -> // Handle hotel list
    is Result.Error -> // Handle error
}
```

---

### 2. SearchHotelsUseCase
**Purpose**: Search hotels with filters

**Location**: `domain/usecase/hotel/SearchHotelsUseCase.kt`

**Parameters**:
- `query: String` - Search query
- `country: String?` - Filter by country (optional)
- `city: String?` - Filter by city (optional)
- `minRating: Double?` - Minimum rating filter (optional)
- `maxPrice: Double?` - Maximum price filter (optional)

**Returns**: `Result<List<Hotel>>`

**Business Logic**:
- Applies multiple filters to hotel search
- Returns filtered hotel list based on criteria

**Usage Example**:
```kotlin
val result = searchHotelsUseCase(
    query = "luxury",
    country = "Vietnam",
    minRating = 4.0,
    maxPrice = 500.0
)
```

---

### 3. GetHotelByIdUseCase
**Purpose**: Get detailed information about a specific hotel

**Location**: `domain/usecase/hotel/GetHotelByIdUseCase.kt`

**Parameters**:
- `hotelId: String` - Unique hotel identifier

**Returns**: `Result<Hotel>`

**Business Logic**:
- Validates hotel ID
- Returns hotel details or "Hotel not found" error

**Usage Example**:
```kotlin
val result = getHotelByIdUseCase("hotel_123")
```

---

### 4. GetHotelRoomsUseCase
**Purpose**: Get available rooms for a hotel with optional filters

**Location**: `domain/usecase/hotel/GetHotelRoomsUseCase.kt`

**Parameters**:
- `hotelId: String` - Hotel identifier
- `checkIn: String?` - Check-in date (optional)
- `checkOut: String?` - Check-out date (optional)
- `guests: Int?` - Number of guests (optional)

**Returns**: `Result<List<Room>>`

**Business Logic**:
- Filters rooms based on availability and guest requirements
- Returns available rooms for specified criteria

**Usage Example**:
```kotlin
val result = getHotelRoomsUseCase(
    hotelId = "hotel_123",
    checkIn = "2024-01-15",
    checkOut = "2024-01-18",
    guests = 2
)
```

---

## 📅 Booking Use Cases

### 5. CreateBookingUseCase
**Purpose**: Create a new hotel booking

**Location**: `domain/usecase/booking/CreateBookingUseCase.kt`

**Parameters**:
- `userId: String` - User making the booking
- `roomId: String` - Room to book
- `dateFrom: LocalDate` - Check-in date
- `dateTo: LocalDate` - Check-out date
- `guests: Int` - Number of guests
- `price: Double` - Total booking price
- `appliedVouchers: List<String>` - Applied voucher codes (optional)

**Returns**: `Result<Booking>`

**Business Logic**:
- Validates booking dates (check-in cannot be after check-out)
- Ensures check-in date is not in the past
- Validates guest count and price are positive
- Creates booking with "PENDING" status

**Validation Rules**:
- Check-in date must be before check-out date
- Check-in date cannot be in the past
- Guest count must be greater than 0
- Price must be greater than 0

**Usage Example**:
```kotlin
val result = createBookingUseCase(
    userId = "user_123",
    roomId = "room_456",
    dateFrom = LocalDate.of(2024, 1, 15),
    dateTo = LocalDate.of(2024, 1, 18),
    guests = 2,
    price = 300.0,
    appliedVouchers = listOf("SAVE10")
)
```

---

### 6. GetUserBookingsUseCase
**Purpose**: Retrieve all bookings for a specific user

**Location**: `domain/usecase/booking/GetUserBookingsUseCase.kt`

**Parameters**:
- `userId: String` - User identifier
- `status: String?` - Filter by booking status (optional)

**Returns**: `Result<List<Booking>>`

**Business Logic**:
- Fetches user's booking history
- Optionally filters by booking status

**Usage Example**:
```kotlin
// Get all bookings
val allBookings = getUserBookingsUseCase("user_123")

// Get only pending bookings
val pendingBookings = getUserBookingsUseCase("user_123", "PENDING")
```

---

### 7. CancelBookingUseCase
**Purpose**: Cancel an existing booking

**Location**: `domain/usecase/booking/CancelBookingUseCase.kt`

**Parameters**:
- `bookingId: String` - Booking to cancel

**Returns**: `Result<Boolean>`

**Business Logic**:
- Validates booking exists
- Checks if booking can be cancelled
- Prevents cancellation of already cancelled or completed bookings

**Validation Rules**:
- Booking must exist
- Booking cannot be already cancelled
- Booking cannot be completed

**Usage Example**:
```kotlin
val result = cancelBookingUseCase("booking_789")
```

---

## 👤 User Use Cases

### 8. GetUserProfileUseCase
**Purpose**: Retrieve user profile information

**Location**: `domain/usecase/user/GetUserProfileUseCase.kt`

**Parameters**:
- `userId: String` - User identifier

**Returns**: `Result<User>`

**Business Logic**:
- Fetches user profile from repository
- Returns user data or "User not found" error

**Usage Example**:
```kotlin
val result = getUserProfileUseCase("user_123")
```

---

### 9. UpdateUserProfileUseCase
**Purpose**: Update user profile information

**Location**: `domain/usecase/user/UpdateUserProfileUseCase.kt`

**Parameters**:
- `userId: String` - User identifier
- `fullName: String?` - New full name (optional)
- `gender: String?` - New gender (optional)
- `photoUrl: String?` - New photo URL (optional)
- `dateOfBirth: LocalDate?` - New date of birth (optional)

**Returns**: `Result<User>`

**Business Logic**:
- Validates user exists
- Validates input parameters
- Updates only provided fields
- Returns updated user profile

**Validation Rules**:
- User must exist
- Full name cannot be empty if provided
- Gender must be "Male", "Female", or "Other"
- Date of birth cannot be in the future

**Usage Example**:
```kotlin
val result = updateUserProfileUseCase(
    userId = "user_123",
    fullName = "John Doe",
    gender = "Male",
    dateOfBirth = LocalDate.of(1990, 5, 15)
)
```

---

## 🔖 Bookmark Use Cases

### 10. AddBookmarkUseCase
**Purpose**: Add a hotel to user's bookmarks

**Location**: `domain/usecase/bookmark/AddBookmarkUseCase.kt`

**Parameters**:
- `userId: String` - User identifier
- `hotelId: String` - Hotel to bookmark

**Returns**: `Result<Bookmark>`

**Business Logic**:
- Checks if hotel is already bookmarked
- Creates new bookmark with current timestamp
- Prevents duplicate bookmarks

**Validation Rules**:
- Hotel must not be already bookmarked by user

**Usage Example**:
```kotlin
val result = addBookmarkUseCase("user_123", "hotel_456")
```

---

### 11. RemoveBookmarkUseCase
**Purpose**: Remove a hotel from user's bookmarks

**Location**: `domain/usecase/bookmark/RemoveBookmarkUseCase.kt`

**Parameters**:
- `userId: String` - User identifier
- `hotelId: String` - Hotel to remove from bookmarks

**Returns**: `Result<Boolean>`

**Business Logic**:
- Removes bookmark from user's collection
- Returns success/failure status

**Usage Example**:
```kotlin
val result = removeBookmarkUseCase("user_123", "hotel_456")
```

---

### 12. GetUserBookmarksUseCase
**Purpose**: Retrieve all bookmarked hotels for a user

**Location**: `domain/usecase/bookmark/GetUserBookmarksUseCase.kt`

**Parameters**:
- `userId: String` - User identifier

**Returns**: `Result<List<Bookmark>>`

**Business Logic**:
- Fetches all bookmarks for specified user
- Returns bookmark list with hotel references

**Usage Example**:
```kotlin
val result = getUserBookmarksUseCase("user_123")
```

---

## ⭐ Review Use Cases

### 13. CreateReviewUseCase
**Purpose**: Create a new hotel review

**Location**: `domain/usecase/review/CreateReviewUseCase.kt`

**Parameters**:
- `userId: String` - User creating the review
- `hotelId: String` - Hotel being reviewed
- `text: String` - Review text content
- `rating: Int` - Rating (1-5 stars)

**Returns**: `Result<Review>`

**Business Logic**:
- Validates review content and rating
- Prevents duplicate reviews from same user
- Creates review with current date

**Validation Rules**:
- Review text cannot be empty
- Rating must be between 1 and 5
- User cannot review the same hotel twice

**Usage Example**:
```kotlin
val result = createReviewUseCase(
    userId = "user_123",
    hotelId = "hotel_456",
    text = "Great hotel with excellent service!",
    rating = 5
)
```

---

### 14. GetHotelReviewsUseCase
**Purpose**: Retrieve reviews for a specific hotel

**Location**: `domain/usecase/review/GetHotelReviewsUseCase.kt`

**Parameters**:
- `hotelId: String` - Hotel identifier
- `limit: Int?` - Maximum number of reviews (optional)
- `offset: Int` - Number of reviews to skip (default: 0)

**Returns**: `Result<List<Review>>`

**Business Logic**:
- Fetches reviews for specified hotel
- Supports pagination with limit and offset
- Returns reviews in chronological order

**Usage Example**:
```kotlin
// Get first 10 reviews
val result = getHotelReviewsUseCase("hotel_456", limit = 10)

// Get next 10 reviews (pagination)
val nextPage = getHotelReviewsUseCase("hotel_456", limit = 10, offset = 10)
```

---

## 🎫 Voucher Use Cases

### 15. GetAvailableVouchersUseCase
**Purpose**: Get available vouchers for user or hotel

**Location**: `domain/usecase/voucher/GetAvailableVouchersUseCase.kt`

**Parameters**:
- `userId: String?` - User identifier (optional)
- `hotelId: String?` - Hotel identifier (optional)

**Returns**: `Result<List<Voucher>>`

**Business Logic**:
- Filters vouchers by status (ACTIVE only)
- Validates voucher validity period
- Filters by hotel applicability if specified

**Filtering Criteria**:
- Voucher status must be "ACTIVE"
- Current time must be within validity period
- Hotel-specific vouchers must match hotel ID

**Usage Example**:
```kotlin
// Get all available vouchers
val allVouchers = getAvailableVouchersUseCase()

// Get vouchers for specific hotel
val hotelVouchers = getAvailableVouchersUseCase(hotelId = "hotel_456")
```

---

### 16. ApplyVoucherToBookingUseCase
**Purpose**: Apply a voucher to an existing booking

**Location**: `domain/usecase/voucher/ApplyVoucherToBookingUseCase.kt`

**Parameters**:
- `bookingId: String` - Booking identifier
- `voucherCode: String` - Voucher code to apply

**Returns**: `Result<Double>` - Discount amount

**Business Logic**:
- Validates booking exists and is pending
- Validates voucher exists and is active
- Checks voucher validity period
- Verifies voucher applies to booking's hotel
- Calculates discount amount based on voucher type

**Validation Rules**:
- Booking must exist and be in "PENDING" status
- Voucher must exist and be "ACTIVE"
- Voucher must be valid at current time
- Voucher must apply to booking's hotel (if hotel-specific)
- Discount cannot exceed booking price

**Discount Calculation**:
- **PERCENTAGE**: `booking.price * (voucher.value / 100.0)`
- **FIXED**: `voucher.value`
- Final discount is capped at booking price

**Usage Example**:
```kotlin
val result = applyVoucherToBookingUseCase(
    bookingId = "booking_789",
    voucherCode = "SAVE20"
)
// Returns discount amount (e.g., 60.0 for 20% off $300 booking)
```

---

## 🔔 Notification Use Cases

### 17. GetUserNotificationsUseCase
**Purpose**: Retrieve notifications for a user

**Location**: `domain/usecase/notification/GetUserNotificationsUseCase.kt`

**Parameters**:
- `userId: String` - User identifier
- `isRead: Boolean?` - Filter by read status (optional)
- `limit: Int?` - Maximum number of notifications (optional)

**Returns**: `Result<List<Notification>>`

**Business Logic**:
- Fetches user's notifications
- Optionally filters by read status
- Supports pagination with limit

**Usage Example**:
```kotlin
// Get all notifications
val allNotifications = getUserNotificationsUseCase("user_123")

// Get only unread notifications
val unreadNotifications = getUserNotificationsUseCase("user_123", isRead = false)

// Get latest 5 notifications
val recentNotifications = getUserNotificationsUseCase("user_123", limit = 5)
```

---

### 18. MarkNotificationAsReadUseCase
**Purpose**: Mark a specific notification as read

**Location**: `domain/usecase/notification/MarkNotificationAsReadUseCase.kt`

**Parameters**:
- `notificationId: String` - Notification identifier

**Returns**: `Result<Boolean>`

**Business Logic**:
- Updates notification's read status
- Returns success/failure status

**Usage Example**:
```kotlin
val result = markNotificationAsReadUseCase("notification_123")
```

---

### 19. MarkAllNotificationsAsReadUseCase
**Purpose**: Mark all notifications as read for a user

**Location**: `domain/usecase/notification/MarkAllNotificationsAsReadUseCase.kt`

**Parameters**:
- `userId: String` - User identifier

**Returns**: `Result<Boolean>`

**Business Logic**:
- Updates all user's notifications to read status
- Returns success/failure status

**Usage Example**:
```kotlin
val result = markAllNotificationsAsReadUseCase("user_123")
```

---

## 🔐 Authentication Use Cases

### 20. SignUpUseCase
**Purpose**: Register a new user account

**Location**: `domain/usecase/SignUpUseCase.kt`

**Parameters**:
- `email: String` - User email
- `password: String` - User password

**Returns**: `Result<User>`

**Business Logic**:
- Creates new user with provided credentials
- Generates default profile information
- Returns created user data

**Usage Example**:
```kotlin
val result = signUpUseCase("user@example.com", "password123")
```

---

### 21. SignInUseCase
**Purpose**: Authenticate user login

**Location**: `domain/usecase/SignInUseCase.kt`

**Parameters**:
- `email: String` - User email
- `password: String` - User password

**Returns**: `Result<User>`

**Business Logic**:
- Validates user credentials
- Returns user data on successful authentication
- Returns error for invalid credentials

**Usage Example**:
```kotlin
val result = signInUseCase("user@example.com", "password123")
```

---

## 📊 Error Handling

All use cases return a `Result<T>` wrapper that can be either:
- `Result.Success<T>` - Contains the successful result data
- `Result.Error` - Contains the exception that occurred

**Example Error Handling**:
```kotlin
val result = getHotelsUseCase()
when (result) {
    is Result.Success -> {
        val hotels = result.data
        // Handle successful result
    }
    is Result.Error -> {
        val exception = result.throwable
        // Handle error (show message, log, etc.)
    }
}
```

---

## 🔧 Dependency Injection

All use cases are registered in the Koin DI container in `UseCaseModule.kt`:

```kotlin
val useCaseModule = module {
    // Hotel use cases
    factory { GetHotelsUseCase(get()) }
    factory { SearchHotelsUseCase(get()) }
    factory { GetHotelByIdUseCase(get()) }
    factory { GetHotelRoomsUseCase(get()) }
    
    // Booking use cases
    factory { CreateBookingUseCase(get()) }
    factory { GetUserBookingsUseCase(get()) }
    factory { CancelBookingUseCase(get()) }
    
    // ... other use cases
}
```

---

## 📝 Notes

1. **Thread Safety**: All use cases are designed to be called from coroutines
2. **Validation**: Business rules and input validation are enforced in use cases
3. **Error Handling**: Consistent error handling using Result wrapper
4. **Testing**: Use cases can be easily unit tested with mock repositories
5. **Extensibility**: New use cases can be added following the same pattern

---

## 🚀 Future Enhancements

- Add caching mechanisms for frequently accessed data
- Implement use case composition for complex operations
- Add audit logging for critical operations
- Implement use case versioning for API compatibility


