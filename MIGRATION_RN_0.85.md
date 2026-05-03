# Migration Guide: React Native 0.80.1 → 0.85.2

Tài liệu này ghi lại toàn bộ quá trình nâng cấp `@vietmap/vietmap-react-native-navigation` từ React Native 0.80.1 lên 0.85.2, bao gồm từng vấn đề gặp phải, nguyên nhân, cách sửa, và hướng dẫn rollback.

---

## Mục lục

1. [Tổng quan kiến trúc SDK](#1-tổng-quan-kiến-trúc-sdk)
2. [Breaking Change 1 – iOS: keyWindow crash](#2-breaking-change-1--ios-keywindow-crash)
3. [Breaking Change 2 – Android: RCTEventEmitter deprecated](#3-breaking-change-2--android-rcteventemitter-deprecated)
4. [Breaking Change 3 – Android: Event property name conflict](#4-breaking-change-3--android-event-property-name-conflict)
5. [Breaking Change 4 – Android: Java version mismatch](#5-breaking-change-4--android-java-version-mismatch)
6. [Breaking Change 5 – Android: lifecycle-extensions deprecated](#6-breaking-change-5--android-lifecycle-extensions-deprecated)
7. [Breaking Change 6 – iOS Podspec: minimum iOS version conflict](#7-breaking-change-6--ios-podspec-minimum-ios-version-conflict)
8. [Breaking Change 7 – NDK 26 không hỗ trợ std::format](#8-breaking-change-7--ndk-26-không-hỗ-trợ-stdformat)
9. [Breaking Change 8 – gesture-handler: shadowNodeFromValue removed](#9-breaking-change-8--gesture-handler-shadownodeFromvalue-removed)
10. [Breaking Change 9 – React 19.1.0 không tương thích với RN 0.85.2](#10-breaking-change-9--react-1910-không-tương-thích-với-rn-0852)
11. [Package version bumps](#11-package-version-bumps)
12. [Tóm tắt tất cả file đã thay đổi](#12-tóm-tắt-tất-cả-file-đã-thay-đổi)
13. [Rollback hướng dẫn](#13-rollback-hướng-dẫn)

---

## 1. Tổng quan kiến trúc SDK

SDK này dùng **old/bridge architecture** hoàn toàn — không có TurboModule hay Fabric:

| Layer | Pattern |
|-------|---------|
| Android | `ReactContextBaseJavaModule` + `SimpleViewManager` + `@ReactMethod`/`@ReactProp` |
| iOS | `RCT_EXTERN_MODULE` + `RCTViewManager` + `RCT_EXTERN_METHOD` |
| JavaScript | `NativeModules` + `requireNativeComponent` |

Từ RN 0.76+, **New Architecture được bật mặc định**. SDK vẫn hoạt động thông qua **Interop Layer** (tầng tương thích ngược), nhưng một số API cũ đã bị xóa hoặc thay đổi hành vi. RN 0.85.2 tiếp tục siết thêm những API này.

---

## 2. Breaking Change 1 – iOS: keyWindow crash

### Vấn đề

**File:** `ios/VietMapNavigationView.swift`, dòng 24

```swift
// CODE CŨ — GÂY CRASH
func addToWindow() {
    let window = UIApplication.shared.keyWindow!  // ← crash!
    self.frame = window.bounds
    window.addSubview(self)
}
```

**Nguyên nhân:**
- `UIApplication.shared.keyWindow` bị **deprecated từ iOS 13** và **không còn hoạt động từ iOS 15+** khi app dùng UIWindowScene.
- Force-unwrap (`!`) khiến app crash ngay lập tức thay vì trả về `nil`.
- Example app đặt minimum iOS là 15.1, nên tất cả thiết bị đích đều bị ảnh hưởng.
- RN 0.85.2 nâng minimum iOS lên 13.4, mọi app mới đều sẽ chạy iOS 15+ và gặp lỗi này.

**Bị xóa/deprecated ở phiên bản:** iOS 13 (deprecated), iOS 15 (không hoạt động trong multi-scene apps)

### Fix

```swift
// CODE MỚI — TƯƠNG THÍCH iOS 13+
func addToWindow() {
    guard let window = UIApplication.shared.connectedScenes
        .compactMap({ $0 as? UIWindowScene })
        .flatMap({ $0.windows })
        .first(where: { $0.isKeyWindow }) else { return }
    self.frame = window.bounds
    window.addSubview(self)
}
```

**Giải thích:** Duyệt qua `connectedScenes` để tìm `UIWindowScene` đang active, sau đó lấy window đang là key window. `guard let` thay cho force-unwrap — nếu không tìm thấy thì thoát an toàn.

### Rollback

Khôi phục lại dòng cũ, nhưng lưu ý app sẽ crash trên iOS 15+ trong multi-scene context.

---

## 3. Breaking Change 2 – Android: RCTEventEmitter deprecated

### Vấn đề

**Files:**
- `android/src/main/java/vn/vietmap/vietmapnavigation/VietMapNavigationView.kt` (3 chỗ: dòng 467, 1436, 1492)
- `android/src/main/java/vn/vietmap/utilities/PluginUtilities.kt` (dòng 36)

```kotlin
// CODE CŨ — DEPRECATED
context
    .getJSModule(RCTEventEmitter::class.java)
    .receiveEvent(id, eventName, writableMap)
```

**Nguyên nhân:**
- `RCTEventEmitter` là API thuộc bridge cũ, bị **deprecated từ RN 0.73**.
- Từ RN 0.76+, New Architecture được bật mặc định. Khi app dùng New Arch, `RCTEventEmitter.receiveEvent()` không hoạt động — events không được gửi đến JavaScript, không có lỗi nhưng silent failure.
- RN 0.85.2 tiếp tục enforcement này, interop layer không còn forward calls từ API cũ này.

**Bị deprecated ở phiên bản:** RN 0.73 (deprecated), behavior thay đổi từ RN 0.76+

### Fix

**Bước 1:** Tạo file mới `android/src/main/java/vn/vietmap/utilities/VietMapEvent.kt`:

```kotlin
package vn.vietmap.utilities

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class VietMapEvent(
    surfaceId: Int,
    viewId: Int,
    private val eventNameStr: String,    // Không dùng "eventName" — xem Issue 3
    private val eventDataMap: WritableMap?
) : Event<VietMapEvent>(surfaceId, viewId) {
    override fun getEventName(): String = eventNameStr
    override fun getEventData(): WritableMap? = eventDataMap
}
```

**Bước 2:** Thay thế tất cả `RCTEventEmitter` calls:

```kotlin
// CODE MỚI — TƯƠNG THÍCH OLD ARCH VÀ NEW ARCH
val surfaceId = UIManagerHelper.getSurfaceId(this)
UIManagerHelper.getEventDispatcherForReactTag(context, id)
    ?.dispatchEvent(VietMapEvent(surfaceId, id, eventName, writableMap))
```

**Import cần thêm:**
```kotlin
import com.facebook.react.uimanager.UIManagerHelper
import vn.vietmap.utilities.VietMapEvent
```

**Import cần xóa:**
```kotlin
import com.facebook.react.uimanager.events.RCTEventEmitter  // XÓA DÒNG NÀY
```

**Với `PluginUtilities.kt`** (không có `View` reference, dùng surfaceId = -1):
```kotlin
UIManagerHelper.getEventDispatcher(context, id)
    ?.dispatchEvent(VietMapEvent(-1, id, "sendRouteProgressEvent", writableMap))
```

**Tại sao `-1`?** `getSurfaceId()` cần một `View` object. `PluginUtilities` là static utility không giữ View. Trong old arch, surfaceId `-1` được interop layer chấp nhận và bỏ qua. Nếu cần full new arch support, caller phải truyền View reference.

### Rollback

Khôi phục import và 4 call sites về `RCTEventEmitter`. Events sẽ không hoạt động khi app dùng New Architecture.

---

## 4. Breaking Change 3 – Android: Event property name conflict

### Vấn đề

Đây là lỗi phát sinh **sau khi fix Issue 2** — xảy ra khi compile lần đầu sau khi tạo `VietMapEvent.kt`.

```
e: VietMapEvent.kt:9:17 'eventName' hides member of supertype 'Event' and needs an 'override' modifier.
```

**Nguyên nhân:**
- Trong phiên bản `react-android` đi kèm RN 0.85.2, class `Event<T>` đã được refactor sang Kotlin và có thêm property `eventName` ở superclass.
- File ban đầu khai báo `private val eventName: String` — tên này **conflict** với property trong superclass.
- Kotlin bắt buộc phải thêm `override` hoặc đổi tên để tránh ẩn (hide) member của superclass.
- Không thể dùng `override` vì superclass property có thể có visibility khác.

**Xảy ra ở phiên bản:** RN 0.85.2 (thay đổi trong `react-android` Kotlin refactor)

### Fix

Đổi tên private fields để tránh conflict:

```kotlin
// TRƯỚC (gây compile error)
class VietMapEvent(
    surfaceId: Int,
    viewId: Int,
    private val eventName: String,    // ← conflict với superclass
    private val eventData: WritableMap?
) : Event<VietMapEvent>(surfaceId, viewId) {
    override fun getEventName(): String = eventName
    override fun getEventData(): WritableMap? = eventData
}

// SAU (compile thành công)
class VietMapEvent(
    surfaceId: Int,
    viewId: Int,
    private val eventNameStr: String,    // ← đổi tên
    private val eventDataMap: WritableMap?
) : Event<VietMapEvent>(surfaceId, viewId) {
    override fun getEventName(): String = eventNameStr
    override fun getEventData(): WritableMap? = eventDataMap
}
```

### Rollback

Xóa `VietMapEvent.kt` và khôi phục `RCTEventEmitter` (xem Issue 2 rollback).

---

## 5. Breaking Change 4 – Android: Java version mismatch

### Vấn đề

**File:** `android/build.gradle`

```groovy
// CONFIG CŨ — MÂU THUẪN
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8   // Java 8
    targetCompatibility JavaVersion.VERSION_1_8   // Java 8
}
kotlinOptions {
    jvmTarget = '17'                               // Java 17
}
```

**Nguyên nhân:**
- `compileOptions` (Java) và `kotlinOptions.jvmTarget` (Kotlin) phải trỏ cùng một phiên bản JVM.
- Từ **RN 0.83+**, React Native yêu cầu **Java 17** ở cả source và target. Nếu Java source là 1.8 nhưng jvmTarget là 17, một số library dependencies có thể không resolve đúng.
- Với RN 0.85, AGP (Android Gradle Plugin) 8.x yêu cầu JVM target thống nhất.

**Xảy ra từ phiên bản:** RN 0.73 (Kotlin/JVM 17 adopted), RN 0.83 (Java 17 requirement tightened)

### Fix

```groovy
// CONFIG MỚI — NHẤT QUÁN
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
kotlinOptions {
    jvmTarget = '17'
}
```

### Rollback

Đổi lại `VERSION_17` → `VERSION_1_8` trong `compileOptions`. Kotlin `jvmTarget = '17'` vẫn giữ nguyên để không gây lỗi Kotlin compilation.

---

## 6. Breaking Change 5 – Android: lifecycle-extensions deprecated

### Vấn đề

**File:** `android/build.gradle`

```groovy
// DEPENDENCY CŨ — ĐÃ BỊ ARCHIVE
implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
```

**Nguyên nhân:**
- `lifecycle-extensions` bị **Google archive (ngừng phát triển) từ năm 2021**, không còn nhận update bảo mật hay tương thích.
- Thư viện này bị tách ra thành nhiều artifact riêng: `lifecycle-runtime-ktx`, `lifecycle-viewmodel-ktx`, v.v.
- Với AGP 8.7.3 và target SDK 35, một số dependencies bên trong `lifecycle-extensions` conflict với AndroidX versions mới.

**Bị deprecated ở phiên bản:** AndroidX Lifecycle 2.2.0 (artifact cuối cùng, không có 2.3+)

### Fix

```groovy
// THAY THẾ BẰNG CÁC ARTIFACT CỤ THỂ
implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
```

**Giải thích:** `lifecycle-runtime-ktx` cung cấp `LifecycleOwner` và coroutine extensions. `lifecycle-viewmodel-ktx` cung cấp `ViewModelProvider` helpers. Đây là hai tính năng chính mà `lifecycle-extensions` cung cấp.

### Rollback

Đổi lại thành `implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"`.

---

## 7. Breaking Change 6 – iOS Podspec: minimum iOS version conflict

### Vấn đề

**File:** `vietmap-react-native-navigation.podspec`

```ruby
# HAI DÒNG MÂU THUẪN NHAU
s.platforms = { :ios => "12.4" }   # ← dòng này
# ...
s.platform = :ios, '12.0'          # ← và dòng này (format deprecated)
```

**Nguyên nhân:**
- Có hai directive khai báo minimum iOS version, giá trị khác nhau (`12.0` vs `12.4`).
- `s.platform` (singular) là format **deprecated** trong CocoaPods 1.x, bị ghi đè bởi `s.platforms`.
- **RN 0.85.2 yêu cầu minimum iOS 13.4**. Nếu podspec khai báo thấp hơn, consuming app có thể gặp lỗi linker khi iOS 13 APIs được dùng.
- Example app đặt iOS 15.1 nhưng podspec nói 12.x — gây ra cảnh báo CocoaPods và có thể lỗi trong một số tool chains.

**Thay đổi minimum iOS ở phiên bản:** RN 0.64 (iOS 11), RN 0.74 (iOS 13.4)

### Fix

```ruby
# XÓA dòng s.platform = :ios, '12.0'
# CHỈ GIỮ LẠI s.platforms VÀ CẬP NHẬT GIÁ TRỊ
s.platforms = { :ios => "13.4" }
```

### Rollback

Khôi phục cả hai dòng với giá trị cũ. Lưu ý: app sẽ không build được nếu consuming app dùng RN 0.74+ với iOS minimum 13.4.

---

## 8. Breaking Change 7 – NDK 26 không hỗ trợ std::format

### Vấn đề

**File:** `example/android/build.gradle`

```
C/C++: error: no member named 'format' in namespace 'std'; did you mean 'folly::format'?
    return std::format("{}%", dimension.value);
```

**Root cause:** Lỗi nằm trong file header của chính React Native 0.85.2:
`react-android-0.85.2-debug/prefab/modules/reactnative/include/react/renderer/core/graphicsConversions.h:71`

```cpp
// TRONG RN 0.85.2's OWN HEADER
return std::format("{}%", dimension.value);  // dùng C++20 std::format
```

**Nguyên nhân:**
- React Native 0.85.2 sử dụng `std::format` từ **C++20 standard library** trong internal headers.
- `std::format` chỉ được hỗ trợ đầy đủ trong **Android NDK r27+** (cụ thể là `libc++` trong NDK 27).
- NDK 26 (`26.1.10909125`) có C++20 support không đầy đủ — `std::format` chưa được implement.
- Example app đang dùng `ndkVersion = "26.1.10909125"`.

**Yêu cầu NDK thay đổi ở phiên bản:** RN 0.84 → NDK 26; RN 0.85 → NDK 27

### Fix

**File:** `example/android/build.gradle`

```groovy
// TRƯỚC
ndkVersion = "26.1.10909125"

// SAU
ndkVersion = "27.0.12077973"
```

**Điều kiện tiên quyết:** NDK 27 phải được cài sẵn. Kiểm tra:
```bash
ls ~/Library/Android/sdk/ndk/
```

Nếu chưa có, cài qua Android Studio → SDK Manager → SDK Tools → NDK (Side by side) → chọn version 27.x.

### Rollback

Đổi lại `ndkVersion = "26.1.10909125"`. Build sẽ fail với `std::format` error khi dùng RN 0.85.2.

---

## 9. Breaking Change 8 – gesture-handler: shadowNodeFromValue removed

### Vấn đề

**Xuất hiện sau khi fix NDK issue (Breaking Change 7)**

```
error: use of undeclared identifier 'shadowNodeFromValue'; did you mean 'shadowNodeListFromValue'?
    auto shadowNode = shadowNodeFromValue(runtime, arguments[0]);
```

**File lỗi:** `react-native-gesture-handler/android/src/main/jni/cpp-adapter.cpp:22`

**Package bị ảnh hưởng:** `react-native-gesture-handler@2.21.2`

**Nguyên nhân:**
- Trong React Native's C++ JSI API, function `shadowNodeFromValue()` đã bị **rename/remove**.
- Phiên bản mới là `shadowNodeListFromValue()` trả về một vector thay vì single node.
- `react-native-gesture-handler@2.21.2` vẫn dùng API cũ `shadowNodeFromValue`, không tương thích với RN 0.85.2's ReactCommon headers.
- Đây là **breaking change ở C++ level** trong React Native's internal API, ảnh hưởng tất cả native modules có C++ code.

**API bị xóa ở phiên bản:** RN 0.82+ (shadowNodeFromValue → shadowNodeListFromValue)

### Fix

**File:** `example/package.json`

```json
// TRƯỚC
"react-native-gesture-handler": "^2.21.2"

// SAU
"react-native-gesture-handler": "^2.31.1"
```

Sau đó chạy:
```bash
npm install
```

**Lưu ý:** `react-native-gesture-handler@2.31.1` là phiên bản đầu tiên trong 2.x series hỗ trợ RN 0.85.2. Nếu bạn muốn dùng v3.x, cần kiểm tra breaking changes của gesture-handler riêng.

### Rollback

Đổi lại `"^2.21.2"` trong `example/package.json` và chạy `npm install`. Build sẽ fail với C++ error ở trên.

---

## 10. Breaking Change 9 – React 19.1.0 không tương thích với RN 0.85.2

### Vấn đề

```
npm error ERESOLVE unable to resolve dependency tree
npm error peer react@"^19.2.3" from react-native@0.85.2
npm error Found: react@19.1.0
```

**Nguyên nhân:**
- `react-native@0.85.2` khai báo peer dependency `react@"^19.2.3"`.
- SDK và example đang dùng `react@19.1.0`.
- `19.1.0` không thỏa `^19.2.3` (yêu cầu tối thiểu 19.2.3).

**Thay đổi ở phiên bản:** RN 0.85 (nâng React peer dep từ 19.1.x lên 19.2.x)

### Fix

**File:** `package.json` và `example/package.json`

```json
// TRƯỚC
"react": "19.1.0"
"react-test-renderer": "19.1.0"  // (chỉ trong example)

// SAU
"react": "19.2.5"
"react-test-renderer": "19.2.5"  // (chỉ trong example, phải khớp với react)
```

**Lưu ý:** `react-test-renderer` luôn phải có cùng version với `react`. Nếu đổi react mà quên đổi react-test-renderer sẽ gặp runtime error khi chạy tests.

Cũng cập nhật `peerDependencies` trong root `package.json`:
```json
// TRƯỚC
"react": ">=18.0.0"

// SAU — tránh conflict nếu consuming app dùng React 20 tương lai
"react": ">=18.0.0 <20"
```

### Rollback

Đổi lại `react@19.1.0` và `react-test-renderer@19.1.0`. Không thể dùng `react-native@0.85.2` với React 19.1.0.

---

## 11. Package version bumps

Bảng tổng hợp tất cả package versions đã thay đổi:

### Root `package.json`

| Package | Trước | Sau | Lý do |
|---------|-------|-----|-------|
| `react` | `19.1.0` | `19.2.5` | RN 0.85.2 peer dep yêu cầu `^19.2.3` |
| `react-native` | `0.80.1` | `0.85.2` | Mục tiêu nâng cấp |
| `@react-native-community/cli` | `15.1.3` | `16.0.3` | CLI 16 required cho RN 0.85 |
| `@react-native-community/cli-platform-android` | `15.1.3` | `16.0.3` | Đồng bộ với CLI |
| `@react-native-community/cli-platform-ios` | `15.1.3` | `16.0.3` | Đồng bộ với CLI |
| `react-native` peerDep | `>=0.70.0` | `>=0.72.0` | Phản ánh minimum thực tế |

### `example/package.json`

| Package | Trước | Sau | Lý do |
|---------|-------|-----|-------|
| `react` | `19.1.0` | `19.2.5` | RN 0.85.2 peer dep |
| `react-native` | `0.80.1` | `0.85.2` | Mục tiêu nâng cấp |
| `react-test-renderer` | `19.1.0` | `19.2.5` | Phải khớp với react version |
| `react-native-gesture-handler` | `^2.21.2` | `^2.31.1` | Breaking C++ API change (Issue 8) |
| `@react-native-community/cli` | `15.0.1` | `16.0.3` | Required cho RN 0.85 |
| `@react-native-community/cli-platform-android` | `15.0.1` | `16.0.3` | Đồng bộ |
| `@react-native-community/cli-platform-ios` | `15.0.1` | `16.0.3` | Đồng bộ |
| `@react-native/babel-preset` | `0.80.1` | `0.85.2` | Phải khớp RN version |
| `@react-native/eslint-config` | `0.80.1` | `0.85.2` | Phải khớp RN version |
| `@react-native/metro-config` | `0.80.1` | `0.85.2` | Phải khớp RN version |
| `@react-native/typescript-config` | `0.80.1` | `0.85.2` | Phải khớp RN version |
| `metro` | `^0.81.0` | `^0.82.0` | RN 0.85 dùng Metro 0.82 |
| `metro-config` | `^0.81.0` | `^0.82.0` | Đồng bộ với metro |
| `metro-resolver` | `^0.81.0` | `^0.82.0` | Đồng bộ với metro |
| `metro-runtime` | `^0.81.0` | `^0.82.0` | Đồng bộ với metro |

### `example/android/build.gradle`

| Config | Trước | Sau | Lý do |
|--------|-------|-----|-------|
| `ndkVersion` | `26.1.10909125` | `27.0.12077973` | NDK 27 cần cho `std::format` C++20 |

---

## 12. Tóm tắt tất cả file đã thay đổi

| File | Loại thay đổi | Mô tả |
|------|--------------|-------|
| `ios/VietMapNavigationView.swift` | Sửa code | `keyWindow` → `connectedScenes` API |
| `android/src/main/java/vn/vietmap/utilities/VietMapEvent.kt` | **Tạo mới** | Event subclass thay thế RCTEventEmitter |
| `android/src/main/java/vn/vietmap/vietmapnavigation/VietMapNavigationView.kt` | Sửa code | 3 call sites RCTEventEmitter → UIManagerHelper; xóa import |
| `android/src/main/java/vn/vietmap/utilities/PluginUtilities.kt` | Sửa code | 1 call site RCTEventEmitter → UIManagerHelper; xóa import |
| `android/build.gradle` | Config | Java 17; lifecycle-extensions → runtime-ktx + viewmodel-ktx |
| `vietmap-react-native-navigation.podspec` | Config | iOS minimum 13.4; xóa dòng `s.platform` cũ |
| `package.json` | Dependencies | React 19.2.5; RN 0.85.2; CLI 16; peerDeps |
| `example/package.json` | Dependencies | Tất cả RN-related packages; gesture-handler 2.31.1 |
| `example/android/build.gradle` | Config | NDK 27.0.12077973 |

---

## 13. Rollback hướng dẫn

Để quay lại RN 0.80.1, thực hiện theo thứ tự ngược:

### Bước 1: Rollback package.json files

**`package.json`:**
```json
{
  "peerDependencies": {
    "react": ">=18.0.0",
    "react-native": ">=0.70.0"
  },
  "devDependencies": {
    "react": "19.1.0",
    "react-native": "0.80.1",
    "@react-native-community/cli": "15.1.3",
    "@react-native-community/cli-platform-android": "15.1.3",
    "@react-native-community/cli-platform-ios": "15.1.3"
  }
}
```

**`example/package.json`** — rollback các version về giá trị cũ:
```json
{
  "dependencies": {
    "react": "19.1.0",
    "react-native": "0.80.1",
    "react-native-gesture-handler": "^2.21.2"
  },
  "devDependencies": {
    "@react-native-community/cli": "15.0.1",
    "@react-native/babel-preset": "0.80.1",
    "metro": "^0.81.0",
    "react-test-renderer": "19.1.0"
    // ... (các package khác tương tự)
  }
}
```

### Bước 2: Rollback Android native code

**`android/build.gradle`:**
```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
// Đổi lại lifecycle
implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
```

**`example/android/build.gradle`:**
```groovy
ndkVersion = "26.1.10909125"
```

**Xóa file:** `android/src/main/java/vn/vietmap/utilities/VietMapEvent.kt`

**`android/.../VietMapNavigationView.kt`** — khôi phục import và 3 call sites:
```kotlin
import com.facebook.react.uimanager.events.RCTEventEmitter
// ...
context.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, eventName, writableMap)
```

**`android/.../PluginUtilities.kt`** — tương tự.

### Bước 3: Rollback iOS

**`ios/VietMapNavigationView.swift`:**
```swift
func addToWindow() {
    let window = UIApplication.shared.keyWindow!
    self.frame = window.bounds
    window.addSubview(self)
}
```

**`vietmap-react-native-navigation.podspec`:**
```ruby
s.platforms = { :ios => "12.4" }
# ...
s.platform = :ios, '12.0'
```

### Bước 4: Reinstall

```bash
# Root
npm install

# Example
cd example
npm install
cd ios && bundle exec pod install && cd ..
```

---

## Lưu ý khi upgrade các dự án tiêu thụ SDK này

Nếu bạn là developer đang dùng `@vietmap/vietmap-react-native-navigation` trong dự án của mình và muốn nâng RN lên 0.85.x, cần đảm bảo:

1. **NDK 27** đã được cài trong Android Studio SDK Manager.
2. **`ndkVersion = "27.0.12077973"`** trong `android/build.gradle` của app.
3. **`react-native-gesture-handler >= 2.31.1`** nếu dùng trong app.
4. **`react >= 19.2.3`** trong `package.json`.
5. **iOS deployment target >= 13.4** trong Podfile: `platform :ios, '13.4'`.
6. Chạy `pod install` sau khi update RN.
7. Nếu app dùng New Architecture (`newArchEnabled=true`), SDK hoạt động qua Interop Layer — tất cả events đi qua `UIManagerHelper.getEventDispatcherForReactTag()`.
