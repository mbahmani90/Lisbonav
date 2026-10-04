# Lisbonav

A Kotlin Multiplatform app for getting around Lisbon, for **Android and iOS** from one shared codebase.

- **Live bus map:** real-time positions of Carris Metropolitana buses on a map.
- **Navegante card reader** *(planned)*: read Lisbon's contactless transit card over NFC
  (Calypso) and show its passes and recent trips.

> Work in progress. See the [roadmap](#roadmap) for what's done.

## Tech stack

| Concern | Library |
|---|---|
| Shared code | Kotlin Multiplatform (Android + iOS) |
| UI | Compose Multiplatform |
| Networking | Ktor (OkHttp engine on Android, Darwin on iOS) |
| JSON | kotlinx.serialization |
| Dependency injection | Koin |
| Tests | kotlin.test, kotlinx-coroutines-test, Ktor `MockEngine` |

## Data source

[Carris Metropolitana open data](https://github.com/carrismetropolitana): `GET https://api.carrismetropolitana.pt/v2/vehicles`

- Public, **no API key** needed.
- Returns all vehicles in service in one JSON array (no pagination); responses are cached for 5 s.

## Architecture

Clean Architecture with dependencies pointing inward:

```
presentation ──► domain ◄── data
                   ▲
                   di (Koin wires everything together)
```

```
shared/src/
├── commonMain/…/lisbonav/
│   ├── data/remote/          # Ktor API client, DTOs, HttpClient factory
│   └── di/                   # Koin modules, initKoin()
├── androidMain/…/di/         # OkHttp engine
└── iosMain/…/di/             # Darwin engine, initKoinIos() for Swift
androidApp/                   # Android entry point (LisbonavApp starts Koin)
iosApp/                       # iOS entry point (iOSApp.swift starts Koin)
```

- The **engine** is the only platform-specific networking piece. JSON, base URL and error
  handling are Ktor plugins in common code, so both platforms behave the same.
- Every API sits behind an **interface**, so the repository and ViewModels can be tested with fakes.

## Build & run

Requirements: Android Studio (with the Kotlin Multiplatform plugin), JDK 21 (requested by
`gradle/gradle-daemon-jvm.properties`), and Xcode for iOS.

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS: open iosApp/iosApp.xcodeproj in Xcode and run, or build from the command line
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

## Tests

Shared tests run on both platforms, without network access:

```bash
./gradlew :shared:testAndroidHostTest      # Android (JVM)
./gradlew :shared:iosSimulatorArm64Test    # iOS simulator
```

## Roadmap

- [x] Vehicles API client and DTO (Ktor + kotlinx.serialization)
- [x] Dependency injection (Koin, platform HTTP engines)
- [ ] Domain model, mapper and repository
- [ ] ViewModel and map screen with live bus positions
- [ ] Request logging (debug only) and timeouts
- [ ] CI (GitHub Actions)
- [ ] Navegante card reader (NFC, Android first)
