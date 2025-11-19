# Prompt để request lại sau khi merge code từ giang/task2 vào main

## Context
Sau khi merge code từ nhánh `giang/task2` vào `main`, và pull về nhánh `truong/task1`, cần áp dụng lại các thay đổi về kiến trúc MVVM cho module đăng ký, đăng nhập, và quản lý hồ sơ người dùng.

## Yêu cầu

Từ code hiện tại của dự án và scope trong `System_Description_Business_Model.md`, hãy áp dụng lại kiến trúc MVVM đầy đủ (3 tầng: Presentation, Domain, Data) cho module:
- **Đăng ký (Sign Up)**
- **Đăng nhập (Sign In)**  
- **Quản lý hồ sơ người dùng (Profile Management)**

## Checklist kiến trúc cần đảm bảo:

1. ✅ **Layer phân tách rõ ràng**: 3 layer (Presentation, Domain, Data) + DI
2. ✅ **Domain là pure Kotlin**: UseCase, Entity, Repository interface không có Android SDK
3. ✅ **UseCase trả về Flow<Result<T>>**: Không dùng blocking `suspend fun` trả về `Result<T>` trực tiếp
4. ✅ **Repository interface nằm ở Domain**: Đúng vị trí
5. ✅ **Data layer implement Repository**: Đúng
6. ✅ **ViewModel không biết gì về AndroidX/Firestore**: Phải loại bỏ hoàn toàn FirebaseAuth/Firestore khỏi ViewModel, chỉ inject UseCase
7. ✅ **UseCase xử lý cả auth + lưu profile**: UseCase phải orchestrate FirebaseAuth + Firestore operations
8. ✅ **Xử lý side-effect đúng cách**: UiEffect một lần, không lặp lại
9. ✅ **State là immutable, event là one-time**: Đảm bảo data class + sealed event
10. ✅ **Navigation không nằm trong ViewModel**: Navigation qua callback lambda trong Composable

## Các file cần kiểm tra/sửa:

### Domain Layer:
- `domain/repository/AuthRepository.kt` - Interface cho authentication
- `domain/repository/UserRepository.kt` - Interface cho user management
- `domain/usecase/SignInUseCase.kt` - Trả về `Flow<Result<User>>`
- `domain/usecase/SignUpUseCase.kt` - Trả về `Flow<Result<User>>`, orchestrate auth + create profile
- `domain/usecase/SignOutUseCase.kt` - Sign out use case
- `domain/usecase/GetCurrentUserIdUseCase.kt` - Get current user ID
- `domain/usecase/user/GetUserProfileUseCase.kt` - Trả về `Flow<Result<User>>`
- `domain/usecase/user/UpdateUserProfileUseCase.kt` - Trả về `Flow<Result<User>>`

### Data Layer:
- `data/repository/firestore/FirebaseAuthRepository.kt` - Implement AuthRepository với FirebaseAuth
- `data/repository/firestore/FirestoreUserRepository.kt` - Implement UserRepository với Firestore

### Presentation Layer:
- `ui/auth/AuthViewModel.kt` - Không có Firebase imports, chỉ inject UseCase
- `ui/auth/AuthUiState.kt` - Immutable state
- `ui/auth/AuthUiEvent.kt` - Sealed class events
- `ui/auth/AuthUiEffect.kt` - Sealed class effects (one-time)
- `ui/auth/SignInScreen.kt` - Sử dụng AuthViewModel, không gọi Firebase trực tiếp
- `ui/auth/SignUpScreen.kt` - Sử dụng AuthViewModel, không gọi Firebase trực tiếp
- `ui/profile/ProfileScreen.kt` - Sử dụng AuthViewModel cho profile management
- `ui/navigation/AppNavHost.kt` - Inject AuthViewModel, handle navigation qua effects
- `ui/main/MainScreen.kt` - Sử dụng authState từ AuthViewModel

### DI:
- `di/RepositoryModule.kt` - Bind AuthRepository và UserRepository
- `di/UseCaseModule.kt` - Bind tất cả auth/user use cases
- `di/ViewModelModule.kt` - Bind AuthViewModel

## Lưu ý đặc biệt:

1. **SignInUseCase**: Phải xử lý trường hợp user chưa có profile trong Firestore:
   - Tìm user theo FirebaseAuth UID
   - Nếu không tìm thấy, tìm theo email
   - Nếu tìm thấy theo email nhưng ID khác, xóa document cũ và tạo mới với ID đúng
   - Nếu vẫn không tìm thấy, tự động tạo user profile mới

2. **Clear message khi navigate**: Khi chuyển từ Authentication screen sang SignIn/SignUp, phải clear message cũ bằng `LaunchedEffect(Unit) { authViewModel.onEvent(AuthUiEvent.ClearMessage) }`

3. **Không có Firebase imports trong ViewModel**: ViewModel chỉ inject UseCase, không có `FirebaseAuth` hay `FirebaseFirestore`

4. **UseCase trả về Flow**: Tất cả UseCase phải trả về `Flow<Result<T>>` để ViewModel có thể collect và handle loading/error states

## Kiểm tra sau khi áp dụng:

- [ ] Build thành công không có lỗi
- [ ] Đăng ký tài khoản mới hoạt động
- [ ] Đăng nhập với tài khoản đã có hoạt động
- [ ] Đăng nhập với tài khoản chưa có profile tự động tạo profile
- [ ] Xem và chỉnh sửa profile hoạt động
- [ ] Không có message cũ hiển thị khi navigate giữa các màn hình auth
- [ ] Navigation hoạt động đúng qua UiEffect
- [ ] Không có Firebase imports trong ViewModel

