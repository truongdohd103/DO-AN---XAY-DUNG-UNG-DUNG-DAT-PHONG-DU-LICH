package com.example.chillstay.di

// Updated to use firestore package structure
import com.example.chillstay.data.api.ChillStayApi
import com.example.chillstay.data.api.FirebaseChillStayApi
import com.example.chillstay.data.repository.firestore.FirebaseAuthRepository
import com.example.chillstay.data.repository.firestore.FirestoreBillRepository
import com.example.chillstay.data.repository.firestore.FirestoreBookingRepository
import com.example.chillstay.data.repository.firestore.FirestoreBookmarkRepository
import com.example.chillstay.data.repository.firestore.FirestoreHotelRepository
import com.example.chillstay.data.repository.firestore.FirestoreReviewRepository
import com.example.chillstay.data.repository.firestore.FirestoreUserRepository
import com.example.chillstay.data.repository.firestore.FirestoreVipStatusRepository
import com.example.chillstay.data.repository.firestore.FirestoreVoucherRepository
import com.example.chillstay.data.repository.image.ImageUploadRepositoryImpl
import com.example.chillstay.domain.repository.AuthRepository
import com.example.chillstay.domain.repository.BillRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.ImageUploadRepository
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.domain.repository.VoucherRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    // Firebase Firestore instance
    single { FirebaseFirestore.getInstance() }
    // Firebase Auth instance
    single { FirebaseAuth.getInstance() }

    // Auth repository
    single<AuthRepository> { FirebaseAuthRepository(get()) }

    // Firestore repositories
    single<HotelRepository> { FirestoreHotelRepository(get()) }
    single<UserRepository> { FirestoreUserRepository(get()) }
    single<BookingRepository> { FirestoreBookingRepository(get()) }
    single<BookmarkRepository> { FirestoreBookmarkRepository(get()) }
    single<ReviewRepository> { FirestoreReviewRepository(get()) }
    single<VoucherRepository> { FirestoreVoucherRepository(get()) }
    single<BillRepository> { FirestoreBillRepository(get()) }
    single<VipStatusRepository> { FirestoreVipStatusRepository(get()) }
    single<ChillStayApi> { FirebaseChillStayApi(get()) }

    // Shared Ktor HttpClient (dùng cho image-service và các repository khác nếu cần)
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }

            install(Logging) {
                level = LogLevel.INFO // Log network requests/responses
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        android.util.Log.d("ChillStayImageUpload", message)
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 30_000L
            }

            // Engine-level timeouts (nếu cần)
            engine {
                connectTimeout = 10_000 // 10 seconds
                socketTimeout = 30_000 // 30 seconds
            }
        }
    }

    // ImageUploadRepository binding — sử dụng HttpClient và Context từ Koin
    single<ImageUploadRepository> {
        ImageUploadRepositoryImpl(
            httpClient = get(),          // lấy HttpClient đã đăng ký ở trên
            context = androidContext()   // nếu impl cần Context (ví dụ để lấy cache dir, resources...)
            // nếu impl cần thêm param (baseUrl, apiKey) -> truyền thêm ở đây
        )
    }
}
