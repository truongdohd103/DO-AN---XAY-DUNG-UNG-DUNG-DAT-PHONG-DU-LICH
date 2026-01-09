# Chiến lược Offline/Cache cho ChillStay

## 📋 Tổng quan

Dự án ChillStay hiện tại chưa có cơ chế offline/cache. Tài liệu này mô tả chiến lược triển khai **"Cache First, Network Fallback"** phù hợp với Clean Architecture và Firebase.

## 🎯 Mục tiêu

1. **Offline-first**: App hoạt động tốt khi không có internet
2. **Performance**: Giảm số lần query Firestore, tăng tốc độ load
3. **Data freshness**: Cân bằng giữa cache và dữ liệu mới nhất
4. **User experience**: Hiển thị dữ liệu ngay lập tức từ cache, cập nhật nền

## 🏗️ Kiến trúc đề xuất

### 3-Layer Cache Strategy

```
┌─────────────────────────────────────────┐
│         UI Layer (ViewModel)             │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Domain Layer (Use Cases)           │
│  ┌────────────────────────────────────┐  │
│  │  Cache Strategy Coordinator      │  │
│  │  - Decide cache vs network       │  │
│  │  - Manage cache invalidation     │  │
│  └────────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Data Layer (Repositories)          │
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │  Memory  │→ │  Local   │→ │Network ││
│  │  Cache   │  │  DB      │  │Firestore│
│  └──────────┘  └──────────┘  └────────┘│
└─────────────────────────────────────────┘
```

### Cache Layers

1. **L1: In-Memory Cache** (Fastest, Volatile)
   - Lưu trong ViewModel/Repository
   - TTL ngắn (5-15 phút)
   - Dùng cho: Hotels list, User profile, Recent bookings

2. **L2: Local Database** (Room Database)
   - Persistent storage
   - TTL dài hơn (1-24 giờ tùy loại data)
   - Dùng cho: Hotels, Rooms, Reviews, Vouchers

3. **L3: Firebase Offline Persistence** (Built-in)
   - Firestore offline cache tự động
   - Fallback khi không có internet
   - Dùng cho: Real-time updates, Sync queue

## 📦 Implementation Plan

### Phase 1: Firebase Offline Persistence (Built-in)

**Ưu tiên: CAO** - Dễ implement, hiệu quả ngay

#### Setup
```kotlin
// ChillStayApplication.kt
FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
    .also { FirebaseFirestore.getInstance().firestoreSettings = it }
```

**Lợi ích:**
- ✅ Tự động cache queries
- ✅ Hoạt động offline ngay
- ✅ Sync tự động khi online lại
- ✅ Không cần code thêm nhiều

**Hạn chế:**
- ⚠️ Chỉ cache queries đã từng chạy
- ⚠️ Không control được TTL
- ⚠️ Cache size có giới hạn

### Phase 2: Room Database (Local Cache)

**Ưu tiên: TRUNG BÌNH** - Cần thời gian implement

#### Database Schema

```kotlin
// data/local/database/ChillStayDatabase.kt
@Database(
    entities = [
        CachedHotel::class,
        CachedRoom::class,
        CachedBooking::class,
        CachedReview::class,
        CachedVoucher::class,
        CachedUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChillStayDatabase : RoomDatabase() {
    abstract fun hotelDao(): HotelDao
    abstract fun roomDao(): RoomDao
    abstract fun bookingDao(): BookingDao
    abstract fun reviewDao(): ReviewDao
    abstract fun voucherDao(): VoucherDao
    abstract fun userDao(): UserDao
}
```

#### Entity với Timestamp

```kotlin
// data/local/entity/CachedHotel.kt
@Entity(tableName = "cached_hotels")
data class CachedHotel(
    @PrimaryKey val id: String,
    val name: String,
    val city: String,
    val country: String,
    val rating: Double,
    val imageUrl: String,
    // ... other fields
    val cachedAt: Long = System.currentTimeMillis(), // TTL tracking
    val expiresAt: Long = System.currentTimeMillis() + CACHE_TTL_HOTELS
) {
    companion object {
        const val CACHE_TTL_HOTELS = 1.hours.inWholeMilliseconds // 1 giờ
    }
    
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}
```

#### DAO với Cache Logic

```kotlin
// data/local/dao/HotelDao.kt
@Dao
interface HotelDao {
    @Query("SELECT * FROM cached_hotels WHERE expiresAt > :now ORDER BY rating DESC")
    suspend fun getAllHotels(now: Long = System.currentTimeMillis()): List<CachedHotel>
    
    @Query("SELECT * FROM cached_hotels WHERE id = :id AND expiresAt > :now")
    suspend fun getHotelById(id: String, now: Long = System.currentTimeMillis()): CachedHotel?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(hotels: List<CachedHotel>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotel(hotel: CachedHotel)
    
    @Query("DELETE FROM cached_hotels WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM cached_hotels")
    suspend fun clearAll()
}
```

### Phase 3: Repository Pattern với Cache Strategy

#### Cache-First Repository

```kotlin
// data/repository/cache/CachedHotelRepository.kt
class CachedHotelRepository @Inject constructor(
    private val firestoreRepo: FirestoreHotelRepository,
    private val localDao: HotelDao,
    private val memoryCache: HotelMemoryCache
) : HotelRepository {
    
    override suspend fun getHotels(): List<Hotel> {
        return try {
            // 1. Check memory cache first (L1)
            memoryCache.getHotels()?.let { return it }
            
            // 2. Check local database (L2)
            val cached = localDao.getAllHotels()
            if (cached.isNotEmpty() && !cached.any { it.isExpired() }) {
                val hotels = cached.map { it.toDomain() }
                memoryCache.putHotels(hotels) // Update memory cache
                return hotels
            }
            
            // 3. Fetch from network (L3)
            val hotels = firestoreRepo.getHotels()
            
            // 4. Update all cache layers
            localDao.insertHotels(hotels.map { it.toCached() })
            memoryCache.putHotels(hotels)
            
            hotels
        } catch (e: Exception) {
            // Network failed, try cache
            val cached = localDao.getAllHotels()
            if (cached.isNotEmpty()) {
                cached.map { it.toDomain() }
            } else {
                throw e
            }
        }
    }
    
    override suspend fun getHotelById(id: String): Hotel? {
        return try {
            // Memory cache
            memoryCache.getHotel(id)?.let { return it }
            
            // Local cache
            localDao.getHotelById(id)?.let {
                val hotel = it.toDomain()
                memoryCache.putHotel(hotel)
                return hotel
            }
            
            // Network
            val hotel = firestoreRepo.getHotelById(id)
            hotel?.let {
                localDao.insertHotel(it.toCached())
                memoryCache.putHotel(it)
            }
            hotel
        } catch (e: Exception) {
            localDao.getHotelById(id)?.toDomain()
        }
    }
}
```

#### Memory Cache Implementation

```kotlin
// data/repository/cache/HotelMemoryCache.kt
@Singleton
class HotelMemoryCache @Inject constructor() {
    private var hotelsCache: List<Hotel>? = null
    private var hotelCache = mutableMapOf<String, Hotel>()
    private var cacheTimestamp: Long = 0
    private val cacheTTL = 5.minutes.inWholeMilliseconds
    
    fun getHotels(): List<Hotel>? {
        return if (System.currentTimeMillis() - cacheTimestamp < cacheTTL) {
            hotelsCache
        } else {
            clear()
            null
        }
    }
    
    fun putHotels(hotels: List<Hotel>) {
        hotelsCache = hotels
        cacheTimestamp = System.currentTimeMillis()
        hotels.forEach { hotelCache[it.id] = it }
    }
    
    fun getHotel(id: String): Hotel? {
        return if (System.currentTimeMillis() - cacheTimestamp < cacheTTL) {
            hotelCache[id]
        } else {
            null
        }
    }
    
    fun putHotel(hotel: Hotel) {
        hotelCache[hotel.id] = hotel
        if (hotelsCache == null) {
            cacheTimestamp = System.currentTimeMillis()
        }
    }
    
    fun clear() {
        hotelsCache = null
        hotelCache.clear()
        cacheTimestamp = 0
    }
}
```

### Phase 4: Cache Invalidation Strategy

#### Time-based Invalidation

```kotlin
// domain/usecase/cache/CacheInvalidationUseCase.kt
class CacheInvalidationUseCase @Inject constructor(
    private val hotelDao: HotelDao,
    private val roomDao: RoomDao,
    // ... other DAOs
) {
    suspend fun invalidateExpiredCache() {
        val now = System.currentTimeMillis()
        hotelDao.deleteExpired(now)
        roomDao.deleteExpired(now)
        // ... other entities
    }
}
```

#### Event-based Invalidation

```kotlin
// Khi user tạo booking mới
class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val cacheInvalidator: CacheInvalidator
) {
    suspend fun invoke(booking: Booking): Result<Booking> {
        val result = bookingRepository.createBooking(booking)
        if (result.isSuccess) {
            // Invalidate room availability cache
            cacheInvalidator.invalidateRoomCache(booking.roomId)
            cacheInvalidator.invalidateHotelCache(booking.hotelId)
        }
        return result
    }
}
```

#### Manual Refresh

```kotlin
// UI có thể trigger refresh
fun HomeViewModel.refreshHotels() {
    viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        // Clear cache và fetch fresh data
        hotelMemoryCache.clear()
        localDao.clearAll()
        loadHotels()
    }
}
```

## 📊 Cache TTL Recommendations

| Data Type | Memory Cache | Local DB | Reason |
|-----------|-------------|----------|--------|
| Hotels List | 5 min | 1 hour | Changes infrequently |
| Hotel Detail | 15 min | 2 hours | User might revisit |
| Rooms | 5 min | 30 min | Availability changes often |
| Bookings | 10 min | 1 hour | User's own data |
| Reviews | 10 min | 2 hours | Changes infrequently |
| Vouchers | 15 min | 4 hours | Changes rarely |
| User Profile | 30 min | 24 hours | Changes rarely |

## 🔄 Data Flow Examples

### Scenario 1: Load Hotels (First Time)

```
1. User opens app
2. Check memory cache → Empty
3. Check local DB → Empty
4. Fetch from Firestore → Success
5. Save to local DB
6. Save to memory cache
7. Display to user
```

### Scenario 2: Load Hotels (Cached)

```
1. User opens app
2. Check memory cache → Found (fresh)
3. Display immediately
4. Background: Check if cache expired
5. If expired: Fetch from Firestore in background
6. Update cache silently
```

### Scenario 3: Offline Mode

```
1. User opens app (no internet)
2. Check memory cache → Empty/Expired
3. Check local DB → Found
4. Display cached data
5. Show "Offline" indicator
6. Queue sync when online
```

## 🛠️ Implementation Steps

### Step 1: Setup Room Database (Week 1)
- [ ] Add Room dependencies
- [ ] Create database schema
- [ ] Create entities và DAOs
- [ ] Setup database instance trong DI

### Step 2: Implement Memory Cache (Week 1-2)
- [ ] Create memory cache classes
- [ ] Add to DI module
- [ ] Test cache TTL logic

### Step 3: Refactor Repositories (Week 2-3)
- [ ] Create CachedHotelRepository
- [ ] Implement cache-first logic
- [ ] Add error handling
- [ ] Test offline scenarios

### Step 4: Cache Invalidation (Week 3)
- [ ] Implement time-based invalidation
- [ ] Add event-based invalidation
- [ ] Create background sync job

### Step 5: UI Updates (Week 4)
- [ ] Add loading states
- [ ] Show cache indicators
- [ ] Add pull-to-refresh
- [ ] Handle offline UI

## 📝 Code Structure

```
app/src/main/java/com/example/chillstay/
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   └── ChillStayDatabase.kt
│   │   ├── entity/
│   │   │   ├── CachedHotel.kt
│   │   │   ├── CachedRoom.kt
│   │   │   └── ...
│   │   └── dao/
│   │       ├── HotelDao.kt
│   │       └── ...
│   ├── repository/
│   │   ├── cache/
│   │   │   ├── HotelMemoryCache.kt
│   │   │   └── CacheInvalidator.kt
│   │   └── cached/
│   │       ├── CachedHotelRepository.kt
│   │       └── ...
│   └── mapper/
│       ├── HotelMapper.kt (toCached, toDomain)
│       └── ...
└── domain/
    └── usecase/
        └── cache/
            ├── InvalidateCacheUseCase.kt
            └── SyncCacheUseCase.kt
```

## ⚠️ Considerations

### 1. Storage Size
- Monitor local DB size
- Implement cleanup job for old data
- Consider pagination for large lists

### 2. Data Consistency
- Use transactions for critical updates
- Handle conflicts (local vs remote)
- Implement conflict resolution strategy

### 3. Performance
- Use background threads for DB operations
- Batch inserts/updates
- Index frequently queried fields

### 4. Testing
- Test offline scenarios
- Test cache expiration
- Test cache invalidation
- Test data sync after offline

## 🚀 Quick Start (Minimal Implementation)

Nếu muốn implement nhanh, bắt đầu với:

1. **Enable Firestore Offline Persistence** (5 phút)
   ```kotlin
   FirebaseFirestore.getInstance().firestoreSettings = 
       FirebaseFirestoreSettings.Builder()
           .setPersistenceEnabled(true)
           .build()
   ```

2. **Add Simple Memory Cache** (1 giờ)
   - Tạo memory cache cho hotels
   - Update repository để check cache trước

3. **Add Room Database** (1 ngày)
   - Setup Room
   - Cache hotels list
   - Implement cache-first logic

## 📚 References

- [Firebase Offline Persistence](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Cache Strategy Patterns](https://developer.android.com/topic/architecture/data-layer/offline-first)

