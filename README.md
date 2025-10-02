# ChillStay

Initial MVVM skeleton commit.

## Architecture
- MVVM with simple Clean-ish layers
- No DI yet (manual wiring in `MainActivity`)

## Modules
- `core/base`: `BaseViewModel`, `UiState`, `UiEvent`, `UiEffect`
- `core/common`: `Result`, `DispatcherProvider`
- `domain`: `model`, `repository` (interfaces), `usecase`
- `data`: `repository` implementations (in-memory sample)
- `ui/navigation`: `Routes`, `AppNavHost`
- `ui/home`: `HomeViewModel`, `HomeScreen`
- `presentation`: `MainActivity` hosting `AppNavHost`

## Build
- Android Gradle Plugin 8.1.0
- compileSdk 36
- Compose BOM + Material3, Navigation Compose

## Next Steps
- Add DI (Koin/Hilt)
- Expand features (auth, details, etc.)
- Write UI/Unit tests
