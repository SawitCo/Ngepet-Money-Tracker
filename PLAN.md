# PLAN.md — Ngepet: Ngedukasi Dompet

> Tri-course project: Software Engineering · Human-Computer Interaction · Framework & Layered Architecture  
> Platform: Kotlin — Android Native | Target: Mahasiswa & Pengguna Umum

---

## Overview

**Ngepet** *(Ngedukasi Dompet)* adalah aplikasi mobile pencatat keuangan harian yang membantu pengguna memahami pola pengeluaran, mengatur anggaran, dan meningkatkan literasi keuangan melalui tips harian yang kontekstual.

---

## Goals

| Course | Goal |
|--------|------|
| SE | Deliverable berupa working Android app dengan test coverage & dokumentasi |
| HCI | UI/UX yang accessible, onboarding yang intuitif, feedback loop yang jelas |
| FLA | Arsitektur berlapis yang bersih dengan penerapan minimal 3 design pattern |

---

## Tech Stack

| Layer | Teknologi |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| State Management | ViewModel + StateFlow (MVVM) |
| Local DB | Room (SQLite) |
| Voice Input | Android SpeechRecognizer API |
| Chart | Vico / MPAndroidChart |
| Widget (Home Screen) | Glance API (Jetpack) |
| Notifications | NotificationManager + WorkManager |
| DI | Hilt |

---

## Architecture — Layered

```
┌─────────────────────────────────┐
│         Presentation Layer       │  Composables, Screens, ViewModel, StateFlow
├─────────────────────────────────┤
│          Domain Layer            │  Use Cases, Entities, Repository Interfaces
├─────────────────────────────────┤
│           Data Layer             │  Repository Impl, Room DAO, Data Sources
└─────────────────────────────────┘
```

---

## Error Handling Strategy

### Prinsip Umum
Setiap layer hanya menangani error miliknya sendiri — Data layer wrap exception ke sealed class, Domain layer propagate via `Result`, Presentation layer render ke UI state.

```kotlin
// Domain layer — Result wrapper
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
}

// Typed exceptions
sealed class AppException(message: String) : Exception(message) {
    class DatabaseException(msg: String) : AppException(msg)
    class VoiceRecognitionException(msg: String) : AppException(msg)
    class PermissionDeniedException(msg: String) : AppException(msg)
    class ParseException(msg: String) : AppException(msg)
}
```

### Error per Layer

| Layer | Sumber Error | Penanganan |
|-------|-------------|------------|
| Data (Room) | Query gagal, DB corrupt | Wrap ke `DatabaseException`, return `Result.Error` |
| Data (Speech) | Timeout, tidak ada input, jaringan | Wrap ke `VoiceRecognitionException` |
| Domain (Use Case) | Validasi gagal (nominal < 0, kategori kosong) | Return `Result.Error` dengan pesan deskriptif |
| Presentation | Semua `Result.Error` dari use case | Map ke `UiState.Error(message)`, tampilkan Snackbar / inline error |

### Voice Input Error Handling
```kotlin
// SpeechRecognizer error codes → AppException
private fun mapSpeechError(errorCode: Int): VoiceRecognitionException = when (errorCode) {
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceRecognitionException("Tidak ada suara terdeteksi")
    SpeechRecognizer.ERROR_NO_MATCH -> VoiceRecognitionException("Ucapan tidak dikenali")
    SpeechRecognizer.ERROR_AUDIO -> VoiceRecognitionException("Masalah pada mikrofon")
    else -> VoiceRecognitionException("Terjadi kesalahan, coba lagi")
}
```

### UiState Pattern (di semua ViewModel)
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val retryAction: (() -> Unit)? = null) : UiState<Nothing>()
}
```

> **Aturan:** Tidak boleh ada `try-catch` di Presentation layer — semua exception sudah di-handle di Use Case / Repository sebelum sampai ke ViewModel.

---

## Design Patterns

### 1. Creational — Abstract Factory
**Konteks:** Setiap *input type* (Manual vs. Voice) menghasilkan family of objects yang berbeda — `Transaction` dengan metadata source-nya + `Category` default yang sesuai. Ini juga menjadi dasar filter di History screen (filter by `inputType`).

```kotlin
// Abstract Factory
interface TransactionComponentFactory {
    fun createTransaction(data: Map<String, Any>): Transaction
    fun createDefaultCategory(): Category
}

// Concrete Factories
class ManualInputFactory : TransactionComponentFactory {
    override fun createTransaction(data: Map<String, Any>) =
        Transaction(..., inputType = InputType.MANUAL)
    override fun createDefaultCategory() = Category(id = "general", ...)
}

class VoiceInputFactory : TransactionComponentFactory {
    override fun createTransaction(data: Map<String, Any>) =
        Transaction(..., inputType = InputType.VOICE)
    override fun createDefaultCategory() = Category(id = "uncategorized", ...)
}

// Usage di ViewModel
val factory: TransactionComponentFactory =
    if (isVoiceInput) VoiceInputFactory() else ManualInputFactory()
val tx = factory.createTransaction(data)
```

> Filter History: `transactions.filter { it.inputType == InputType.VOICE }` atau by `categoryId` — keduanya tersedia dari hasil factory.

### 2. Structural — Adapter
**Konteks:** Mengadaptasi hasil speech-to-text (raw string) menjadi `TransactionInputModel`.

```kotlin
class SpeechToTransactionAdapter {
    fun adapt(rawSpeech: String): TransactionInputModel { ... }
}
```

### 3. Behavioral — Strategy
**Konteks:** Kalkulasi laporan bisa di-swap antara strategi Daily / Weekly / Monthly / Category tanpa mengubah ViewModel. User memilih period di UI → strategy berganti → hasil laporan diperbarui.

```kotlin
interface ReportStrategy {
    fun calculate(transactions: List<Transaction>): ReportData
}

class DailyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData { ... }
}

class WeeklyReportStrategy : ReportStrategy { ... }
class MonthlyReportStrategy : ReportStrategy { ... }
class CategoryReportStrategy : ReportStrategy { ... }

// Context di ViewModel
class ReportViewModel : ViewModel() {
    private var strategy: ReportStrategy = MonthlyReportStrategy()

    fun setStrategy(s: ReportStrategy) { strategy = s }
    fun generateReport(data: List<Transaction>) = strategy.calculate(data)
}
```

---

## Features & Scope

### MVP (Must Have)
- [ ] Catat pemasukan & pengeluaran (manual input)
- [ ] Kategori transaksi (Makan, Transport, Gaji, dll.) — bisa custom
- [ ] Budget / limit per kategori per bulan
- [ ] Laporan & grafik (pie chart kategori, line chart tren harian)
- [ ] Input via suara — parse nominal + kategori dari kalimat natural

### V1.1 (Should Have)
- [ ] Home screen widget — ringkasan saldo & pengeluaran hari ini
- [ ] Daily tips literasi keuangan (rotasi konten, kontekstual berdasarkan pola)
- [ ] Notifikasi pengingat harian & peringatan jika mendekati budget limit

### Out of Scope
- Sinkronisasi cloud / multi-device
- Autentikasi / login
- Ekspor CSV/PDF *(pertimbangkan di V2)*

---

## Screens

```
Splash / Onboarding
│
├── Home Dashboard
│   ├── Saldo total
│   ├── Pengeluaran hari ini
│   ├── Shortcut: + Tambah Transaksi
│   └── Daily Tip card
│
├── Tambah Transaksi
│   ├── Input manual (nominal, kategori, catatan, tanggal)
│   └── Input suara (tekan & bicara)
│
├── Riwayat Transaksi
│   └── Filter by kategori / rentang tanggal / input type (manual | suara)
│
├── Laporan
│   ├── Pie chart (per kategori)
│   └── Line chart (tren harian/mingguan)
│
└── Budget
    ├── Set limit per kategori
    └── Progress bar penggunaan
```

---

## Data Models

```kotlin
enum class TransactionType { INCOME, EXPENSE }
enum class InputType { MANUAL, VOICE }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val inputType: InputType,   // untuk filter History by input source
    val amount: Double,
    val categoryId: String,
    val note: String?,
    val date: Long // epoch millis
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconRes: Int,
    val colorHex: String
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: String,
    val categoryId: String,
    val limit: Double,
    val month: Int,
    val year: Int
)

data class DailyTip(
    val id: String,
    val content: String,
    val triggerContext: String? // e.g., "high_food_spending"
)
```

> **Sumber konten DailyTip:** Konten tips di-bundle langsung di app sebagai data statis — disimpan di `assets/daily_tips.json` dan di-seed ke Room DB saat pertama kali app diinstall (via `RoomDatabase.Callback.onCreate`). Tidak ada dependency ke server/internet.
>
> **Strategi rotasi:** Tips ditampilkan berdasarkan index hari dalam tahun (`dayOfYear % totalTips`), sehingga berputar otomatis tanpa logika kompleks. Tips kontekstual (`triggerContext != null`) diprioritaskan jika ada pola spending yang cocok — misalnya, jika pengeluaran kategori Makan minggu ini > 40% total, tip dengan `triggerContext = "high_food_spending"` muncul duluan.
>
> **DailyTip sebagai Room Entity:**
```kotlin
@Entity(tableName = "daily_tips")
data class DailyTip(
    @PrimaryKey val id: String,
    val content: String,
    val triggerContext: String?, // nullable — null = tip umum, non-null = kontekstual
    val isActive: Boolean = true
)
```
```

---

## Permission Handling

### Permissions yang Dibutuhkan

| Permission | Kapan Diminta | Fallback jika Ditolak |
|-----------|--------------|----------------------|
| `RECORD_AUDIO` | Saat user pertama kali tap tombol Voice Input | Sembunyikan tombol Voice, tampilkan hanya manual input |
| `POST_NOTIFICATIONS` (Android 13+) | Saat user aktifkan reminder di Settings | Fitur notifikasi dinonaktifkan, UI badge "Aktifkan di Settings" |

### Permission Flow — RECORD_AUDIO
```
User tap mic button
    │
    ├─ [Belum pernah diminta] → Tampilkan rationale dialog
    │       "Ngepet butuh akses mikrofon untuk input suara"
    │       [Izinkan] → requestPermissions() → onResult
    │       [Nanti saja] → kembali ke form manual
    │
    ├─ [Granted] → Buka VoiceInputBottomSheet
    │
    └─ [Ditolak / Permanently denied]
            → Snackbar: "Izin mikrofon diperlukan"
            → Tombol "Buka Pengaturan" → Intent ke App Settings
            → Tombol mic di-disable sampai permission granted
```

### Implementasi (Compose)
```kotlin
val micPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) openVoiceInput()
    else showPermissionDeniedSnackbar()
}

fun onMicButtonClick(context: Context) {
    when {
        ContextCompat.checkSelfPermission(context, RECORD_AUDIO) == GRANTED -> openVoiceInput()
        shouldShowRationale -> showRationaleDialog()
        else -> micPermissionLauncher.launch(RECORD_AUDIO)
    }
}
```

### HCI Note
- Rationale dialog muncul **sebelum** system dialog, bukan sesudah — user mengerti *mengapa* izin dibutuhkan
- Jika permanently denied, **jangan** re-request permission — arahkan ke Settings
- Tombol mic tidak dihilangkan dari UI, tapi di-disable dengan tooltip "Izin mikrofon diperlukan"

---



```
User speaks → Android SpeechRecognizer API → raw string
    → SpeechToTransactionAdapter
        → NLP parsing (regex / simple rules):
            - Nominal: "lima puluh ribu" → 50000
            - Kategori: "makan siang" → kategori:Makan
            - Tipe: "beli" / "bayar" → EXPENSE | "terima" → INCOME
    → TransactionInputModel (pre-filled form)
    → User confirm → save via ViewModel → Room DB
```

---

## HCI Considerations

- **Onboarding minimal** — langsung ke home, no mandatory sign-up
- **One-thumb reachability** — FAB di bottom-right, navigasi bottom bar
- **Immediate feedback** — konfirmasi visual setelah transaksi tersimpan
- **Error tolerance** — voice input selalu buka form konfirmasi, tidak langsung save
- **Daily tip placement** — card di dashboard, bisa di-dismiss, tidak mengganggu flow utama
- **Budget warning** — color-coded progress bar (hijau → kuning → merah), notifikasi non-intrusive
- **Voice error tolerance** — hasil parsing selalu tampil sebagai preview dulu, user konfirmasi sebelum save; ada opsi "Edit manual" kalau parsing salah
### Empty States

Setiap screen dengan list/data wajib punya empty state yang informatif dan actionable — bukan cuma blank atau teks "Tidak ada data".

| Screen | Empty State Message | CTA |
|--------|-------------------|-----|
| Home Dashboard (belum ada transaksi) | Ilustrasi celengan + "Mulai catat pengeluaranmu hari ini!" | Tombol "+ Catat Sekarang" |
| Riwayat Transaksi (filter tidak ada hasil) | "Tidak ada transaksi yang cocok dengan filter ini" | Tombol "Reset Filter" |
| Riwayat Transaksi (belum ada sama sekali) | "Belum ada transaksi tercatat" | Tombol "+ Tambah Transaksi" |
| Laporan (belum ada data bulan ini) | "Belum cukup data untuk laporan bulan ini" | Tombol "Lihat Bulan Lalu" |
| Budget (belum ada budget diset) | Ilustrasi target + "Set budget untuk mulai tracking pengeluaran" | Tombol "+ Set Budget" |

**Aturan desain empty state:**
- Gunakan ilustrasi ringan (SVG icon, bukan foto) dengan warna Primary light (`#EAF3DE`)
- Pesan harus friendly dan tidak menyalahkan user
- Selalu ada minimal 1 action yang jelas — jangan biarkan user buntu
- Jangan tampilkan empty state saat data masih loading (tampilkan skeleton/shimmer dulu)



---

## Design System

### Brand
| Token | Value |
|-------|-------|
| App name | Ngepet |
| Tagline | ngedukasi dompet |
| App icon | `pig-money` (Tabler Icons) |

### Color Palette
| Role | Color | Hex |
|------|-------|-----|
| Primary | Green 600 | `#3B6D11` |
| Primary light | Green 50 | `#EAF3DE` |
| Primary dark | Green 800 | `#27500A` |
| Secondary | Pink 400 | `#D4537E` |
| Secondary light | Pink 50 | `#FBEAF0` |
| Secondary dark | Pink 800 | `#72243E` |
| Danger | Red 600 | `#A32D2D` |
| Warning | Amber 600 | `#BA7517` |
| Surface | Gray 50 | `#F1EFE8` |

### Color Usage
- **Primary (hijau)** — FAB, balance card, active nav, tip card, progress bar safe, income amount
- **Secondary (pink)** — label input "Suara", waveform visualizer, kategori Belanja, accent CTA sekunder
- **Danger (merah)** — expense amount, progress bar over-budget, badge "Melebihi limit"
- **Warning (amber)** — progress bar near-limit, badge "Hampir habis"

### Typography
| Style | Size | Weight |
|-------|------|--------|
| App name / screen title | 15–18px | 500 |
| Body | 13px | 400 |
| Label / caption | 10–11px | 400 |
| Amount large | 24–34px | 500 |

### Key Components
- **Balance card** — full primary green, rounded 18px, mini sub-cards 15% white overlay
- **FAB** — primary green, radius 15px, center bottom nav
- **Bottom nav** — 5 item: Home · Riwayat · FAB · Laporan · Budget
- **Transaction item** — icon 32px radius 10px, badge input type (hijau=manual, pink=suara)
- **Budget progress bar** — height 5px, 3 state: green < 70% / amber 70–99% / red ≥ 100%
- **Tip card** — green 50 bg, green 800 text, dismissible
- **Voice screen** — bottom sheet, mic ring (green 50 border + green 600 fill), pink waveform bars

---

## Folder Structure

```
app/src/main/java/com/ngepet/
├── core/
│   ├── constants/
│   └── utils/
├── data/
│   ├── local/
│   │   ├── dao/               # Room DAOs
│   │   ├── entity/            # Room Entities
│   │   └── AppDatabase.kt
│   ├── repository/            # implementasi Repository
│   └── source/                # SpeechRecognizer wrapper, dll
├── domain/
│   ├── model/                 # domain entities (bukan Room entity)
│   ├── repository/            # interface Repository
│   └── usecase/
├── presentation/
│   ├── ui/
│   │   ├── home/
│   │   ├── transaction/
│   │   ├── history/
│   │   ├── report/
│   │   └── budget/
│   ├── viewmodel/
│   └── theme/                 # Color.kt, Typography.kt, Theme.kt
├── widget/                    # Glance AppWidget
└── di/                        # Hilt modules
```

---

## Git & Branching Strategy

### Branch Model
```
main          ← production-ready, hanya merge dari develop via PR
develop       ← integration branch, semua feature merge ke sini
│
├── feature/home-dashboard
├── feature/add-transaction-manual
├── feature/voice-input
├── feature/report-chart
├── feature/budget-module
├── fix/voice-parsing-nominal
└── chore/setup-hilt-di
```

### Naming Convention
| Prefix | Kapan |
|--------|-------|
| `feature/` | Fitur baru dari milestone |
| `fix/` | Bug fix |
| `chore/` | Setup, dependency update, refactor non-fungsional |
| `test/` | Menambah test coverage tanpa mengubah logic |

### Commit Convention (Conventional Commits)
```
feat: tambah voice input bottom sheet
fix: parsing nominal "ribu" tidak terdeteksi
chore: setup Hilt DI modules
test: unit test MonthlyReportStrategy
refactor: pisahkan TransactionRepository interface ke domain layer
```

### Pull Request Rules
- Setiap PR minimal di-review 1 orang sebelum merge ke `develop`
- PR ke `main` hanya dari `develop`, wajib passing semua test
- Deskripsi PR wajib mention milestone terkait (e.g., `Closes #M3 - Budget Module`)

---

## Milestones

| # | Milestone | Deliverable |
|---|-----------|-------------|
| 1 | Project Setup | Android project (Kotlin + Compose), Room setup, Hilt DI |
| 2 | Core Feature | CRUD transaksi + kategori (manual input) |
| 3 | Budget Module | Set budget, progress tracking, alert |
| 4 | Voice Input | SpeechRecognizer integration + adapter |
| 5 | Laporan | Grafik pie + line (Vico / MPAndroidChart) |
| 6 | Widget & Tips | Glance widget + daily tips rotation |
| 7 | HCI Polish | Onboarding, animasi, accessibility review |
| 8 | Testing & Docs | Unit test use cases, integration test, README |

---

## Testing Strategy

| Layer | Approach |
|-------|----------|
| Domain (Use Cases) | Unit test — pure Kotlin, JUnit5 + MockK |
| Repository | Mock test dengan MockK |
| ViewModel | Unit test dengan Turbine (StateFlow testing) |
| UI | Compose UI test (composeTestRule) |
| Voice Adapter | Unit test parsing logic |

---

## Dependencies (build.gradle.kts — key libraries)

```kotlin
// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.05.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.9.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// ViewModel + StateFlow
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Hilt (DI)
implementation("com.google.dagger:hilt-android:2.51")
kapt("com.google.dagger:hilt-compiler:2.51")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Glance (Home Widget)
implementation("androidx.glance:glance-appwidget:1.0.0")

// Chart
implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.20")

// WorkManager (notifikasi terjadwal)
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("app.cash.turbine:turbine:1.1.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

*Last updated: May 2026 | Ngepet v0.1 Plan*
