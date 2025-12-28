package com.example.chillstay.di

// Updated to use firestore package structure
import com.example.chillstay.data.repository.firestore.FirestoreHotelRepository
import com.example.chillstay.data.repository.firestore.FirestoreUserRepository
import com.example.chillstay.data.repository.firestore.FirestoreBookingRepository
import com.example.chillstay.data.repository.firestore.FirestoreBookmarkRepository
import com.example.chillstay.data.repository.firestore.FirestoreReviewRepository
import com.example.chillstay.data.repository.firestore.FirestoreVoucherRepository
import com.example.chillstay.data.repository.firestore.FirestoreBillRepository
import com.example.chillstay.data.repository.firestore.FirestoreVipStatusRepository
import com.example.chillstay.data.repository.firestore.FirebaseAuthRepository
import com.example.chillstay.data.repository.storage.FirebaseStorageRepository
// removed SampleRepository binding
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.domain.repository.BillRepository
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.domain.repository.AuthRepository
import com.example.chillstay.domain.repository.StorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.dsl.module
import com.example.chillstay.data.api.ChillStayApi
import com.example.chillstay.data.api.FirebaseChillStayApi

val repositoryModule = module {
    // Firebase Firestore instance
    single { FirebaseFirestore.getInstance() }
    // Firebase Auth instance
    single { FirebaseAuth.getInstance() }
    // Firebase Storage instance
    single { FirebaseStorage.getInstance() }
    
    // Sample repository removed
    
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
    single<StorageRepository> { FirebaseStorageRepository(get()) }
    single<ChillStayApi> { FirebaseChillStayApi(get()) }
}