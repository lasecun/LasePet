# AGENTS.md

## Project snapshot
- Kotlin Multiplatform + Compose app with one Gradle module (`composeApp`) and one Xcode wrapper app (`iosApp`).
- Root project includes only `:composeApp` (`settings.gradle.kts`); iOS app is built from Xcode and embeds the Kotlin framework.
- Existing repository guidance source discovered: `README.md` (no `.github/copilot-instructions.md`, `CLAUDE.md`, or other agent rule files present).

## Architecture (what talks to what)
- Shared UI entrypoint is `App()` in `composeApp/src/commonMain/kotlin/es/itram/App.kt`.
- Android entrypoint: `MainActivity` calls `setContent { App() }` in `composeApp/src/androidMain/kotlin/es/itram/MainActivity.kt`.
- iOS entrypoint: `MainViewController()` in `composeApp/src/iosMain/kotlin/es/itram/MainViewController.kt`.
- Swift bridge: `iosApp/iosApp/ContentView.swift` uses `MainViewControllerKt.MainViewController()` from `import ComposeApp`.
- Platform-specific behavior uses `expect/actual`: `getPlatform()` declared in `commonMain` and implemented in `androidMain` + `iosMain` (`Platform.*.kt`).

## Source set boundaries and conventions
- Default to `commonMain` for shared logic/UI; use `androidMain` or `iosMain` only for true platform APIs.
- Keep package namespace consistent (`es.itram`) across source sets.
- When adding platform features, add/update all three files together: `Platform.kt`, `Platform.android.kt`, `Platform.ios.kt` (or equivalent expect/actual pair).
- Do not edit generated files under `composeApp/build/`.
- Shared assets live in `composeApp/src/commonMain/composeResources/` and are consumed via generated `Res` accessors (example in `App.kt`).

## Build, test, and verification workflows
- Android debug APK: `./gradlew :composeApp:assembleDebug`.
- Install on connected Android device/emulator: `./gradlew :composeApp:installDebug`.
- Cross-target tests report: `./gradlew :composeApp:allTests`.
- Android unit tests: `./gradlew :composeApp:testDebugUnitTest`.
- iOS Kotlin tests (simulator target): `./gradlew :composeApp:iosSimulatorArm64Test`.
- Consistency checks (including Xcode/KMP config): `./gradlew :composeApp:check`.

## Dependency and tooling patterns
- Versions/plugins are centralized in `gradle/libs.versions.toml`; prefer `libs.*` aliases in Gradle files.
- `composeApp/build.gradle.kts` defines KMP targets (`androidTarget`, `iosArm64`, `iosSimulatorArm64`) and creates static iOS framework `ComposeApp`.
- Android config is intentionally modern/high-min-sdk (`minSdk = 33`, `compileSdk/targetSdk = 36`); keep new APIs compatible with this baseline.

## Agent change checklist
- If you rename shared Kotlin APIs used by Swift, update both Kotlin (`iosMain`) and Swift call sites (`iosApp/iosApp/ContentView.swift`).
- If you add dependencies, update `gradle/libs.versions.toml` first, then consume via alias.
- If you touch UI/resources, verify both Android (`assembleDebug`) and iOS bridge build path (`check` or Xcode run).

