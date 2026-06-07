# TEST.md — Ngepet Testing Plan
**App:** Ngepet (com.ngepet) — Android money tracker  
**Stack:** Kotlin, Jetpack Compose, Room, Hilt, Android SpeechRecognizer  
**Test tools:** MockK, Turbine (Flow testing), JUnit4, Android Instrumented Tests

---

## 1. Unit Tests

Target layer: **Domain** (UseCases) dan **Data** (Repository). Jalankan di JVM lokal, tanpa emulator.

### 1.1 UseCase Tests

| Test Class | Method to Test | Scenario |
|---|---|---|
| `AddTransactionUseCaseTest` | `execute()` | Input valid → transaksi tersimpan ke repo |
| `AddTransactionUseCaseTest` | `execute()` | Amount ≤ 0 → throw `InvalidAmountException` |
| `AddTransactionUseCaseTest` | `execute()` | Category kosong → throw `InvalidCategoryException` |
| `GetTransactionsUseCaseTest` | `execute(filter)` | Filter by inputType=VOICE → hanya return transaksi voice |
| `GetTransactionsUseCaseTest` | `execute(filter)` | Filter by dateRange → transaksi di luar range tidak muncul |
| `GetSummaryUseCaseTest` | `execute(month, year)` | Return total income, expense, dan balance yang benar |
| `GetBudgetStatusUseCaseTest` | `execute(categoryId)` | Budget terlampaui → return `BudgetStatus.EXCEEDED` |
| `GetBudgetStatusUseCaseTest` | `execute(categoryId)` | Budget aman → return `BudgetStatus.OK` |

**Template MockK:**
```kotlin
@Test
fun `addTransaction with valid input saves to repository`() = runTest {
    val repo = mockk<TransactionRepository>()
    val useCase = AddTransactionUseCase(repo)
    val transaction = fakeTransaction(amount = 50_000.0)

    coEvery { repo.save(any()) } just Runs

    useCase.execute(transaction)

    coVerify(exactly = 1) { repo.save(transaction) }
}
```

### 1.2 Repository Tests (Unit — dengan mock DAO)

| Test Class | Method | Scenario |
|---|---|---|
| `TransactionRepositoryImplTest` | `save()` | Memanggil `transactionDao.insert()` dengan entity yang benar |
| `TransactionRepositoryImplTest` | `getAll()` | Mengkonversi entity dari DAO ke domain model |
| `CategoryRepositoryImplTest` | `getDefaults()` | Return kategori default (Makan, Transport, dll) |

### 1.3 Strategy Pattern Tests (Report Calculation)

| Test Class | Strategy | Scenario |
|---|---|---|
| `MonthlyReportStrategyTest` | `MonthlyReportStrategy` | Aggregate transaksi per bulan dengan benar |
| `WeeklyReportStrategyTest` | `WeeklyReportStrategy` | Aggregate per minggu, boundary week benar |
| `CategoryReportStrategyTest` | `CategoryReportStrategy` | Group by category, persentase dihitung benar |

### 1.4 Abstract Factory Tests

| Test Class | Scenario |
|---|---|
| `ManualTransactionFactoryTest` | Produksi `ManualTransaction` + `ManualCategory` yang valid |
| `VoiceTransactionFactoryTest` | Produksi `VoiceTransaction` + `VoiceCategory`, inputType = VOICE |
| `VoiceTransactionFactoryTest` | Raw speech string di-parse ke amount + category dengan benar |

---

## 2. Integration Tests

Target: **Room DB + Repository** layer bersama. Jalankan dengan `@RunWith(AndroidJUnit4::class)` di emulator/device, pakai in-memory Room DB.

```kotlin
@get:Rule val instantTaskRule = InstantTaskExecutorRule()

private lateinit var db: NgepetDatabase
private lateinit var dao: TransactionDao
private lateinit var repo: TransactionRepositoryImpl

@Before
fun setup() {
    db = Room.inMemoryDatabaseBuilder(context, NgepetDatabase::class.java)
        .allowMainThreadQueries().build()
    dao = db.transactionDao()
    repo = TransactionRepositoryImpl(dao)
}

@After fun teardown() { db.close() }
```

| Test | Scenario |
|---|---|
| `insert then getAll returns inserted data` | Save 1 transaksi → query all → muncul 1 item |
| `delete removes record` | Save → delete → query all → empty |
| `filter by month returns correct subset` | Insert Juni + Juli → filter Juni → hanya Juni yang balik |
| `budget insert and query` | Set budget kategori → query → nilai tersimpan dengan benar |
| `Flow emission on insert` | Collect Flow dari `getAll()`, insert item baru → Turbine konfirmasi emission baru |

**Contoh Turbine:**
```kotlin
@Test
fun `inserting transaction emits new list via Flow`() = runTest {
    repo.getAllFlow().test {
        awaitItem() // initial empty list

        repo.save(fakeTransaction())

        val updated = awaitItem()
        assertThat(updated).hasSize(1)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 3. ViewModel Tests

Jalankan di JVM dengan `TestCoroutineDispatcher` + MockK untuk UseCase.

| ViewModel | Method/State | Test Scenario |
|---|---|---|
| `AddTransactionViewModelTest` | `onSave()` | Valid input → `uiState` berubah ke `Success` |
| `AddTransactionViewModelTest` | `onSave()` | Invalid amount → `uiState` berubah ke `Error` dengan pesan |
| `DashboardViewModelTest` | `init` | Load data → `summary` StateFlow emit data yang benar |
| `DashboardViewModelTest` | `onMonthChanged()` | Ganti bulan → re-fetch, summary terupdate |
| `HistoryViewModelTest` | `onFilterChanged(inputType=VOICE)` | List di-filter, hanya VOICE yang tampil |
| `BudgetViewModelTest` | `onBudgetSet()` | Set budget valid → disimpan ke use case |
| `ReportViewModelTest` | `onStrategyChanged()` | Ganti strategy (monthly/weekly) → data chart berubah |

---

## 4. System Tests (End-to-End)

Jalankan dengan Espresso atau Compose UI Test di emulator. Verifikasi full user flow.

| ID | Flow | Steps | Expected Result |
|---|---|---|---|
| SYS-01 | Tambah transaksi manual | Buka app → Tambah Transaksi → isi form → Save | Transaksi muncul di Riwayat & Dashboard terupdate |
| SYS-02 | Tambah transaksi voice | Tap mic → ucapkan "Makan siang dua puluh ribu" → konfirmasi | Transaksi dengan amount 20.000 tersimpan |
| SYS-03 | Set budget & alert | Set budget Makan 500rb → tambah transaksi 600rb ke Makan | Indikator budget exceeded muncul |
| SYS-04 | Filter riwayat | Buka Riwayat → filter by "Voice" | Hanya transaksi voice yang ditampilkan |
| SYS-05 | Ganti periode laporan | Buka Laporan → switch dari Monthly ke Weekly | Chart berubah, data weekly tampil |
| SYS-06 | Navigasi antar screen | Tap semua menu sidebar | Semua screen dapat diakses tanpa crash |

---

## 5. Acceptance Tests (UAT)

Validasi bahwa fitur utama memenuhi kebutuhan user. Bisa dilakukan manual oleh tim.

| ID | User Story | Kriteria Diterima |
|---|---|---|
| UAT-01 | Sebagai user, saya bisa mencatat pengeluaran dengan cepat | Form tambah transaksi bisa diisi dan disimpan < 30 detik |
| UAT-02 | Sebagai user, saya bisa melihat ringkasan keuangan bulan ini | Dashboard menampilkan total income, expense, dan saldo bulan berjalan |
| UAT-03 | Sebagai user, saya bisa melihat history transaksi saya | Riwayat menampilkan semua transaksi dengan filter yang berfungsi |
| UAT-04 | Sebagai user, saya bisa set budget per kategori | Budget bisa diset dan muncul indikator ketika mendekati/melewati limit |
| UAT-05 | Sebagai user, saya bisa input transaksi via suara | Voice input berhasil dikenali dan dikonversi ke transaksi yang valid |
| UAT-06 | Sebagai user, saya bisa lihat laporan visual pengeluaran | Laporan chart tampil dengan data yang akurat per periode |

---

## 6. Risk & Security Checklist

Sesuai LO3 dari rubrik AoL SE:

| Risiko | Mitigasi | Status |
|---|---|---|
| Data loss jika Room migration gagal | Verifikasi migration dengan `MigrationTestHelper` | TODO |
| Voice input salah parsing → transaksi salah | Fallback ke konfirmasi manual sebelum save | Implemented |
| Amount overflow (nilai terlalu besar) | Validasi batas maksimal di UseCase | TODO |
| Akses mic tanpa permission | Runtime permission check sebelum SpeechRecognizer aktif | TODO |
| Race condition pada concurrent writes ke Room | Room guarantee single-writer, verifikasi via concurrent test | TODO |

---

## 7. Test Report Template

Untuk setiap test yang dijalankan, dokumentasikan dengan format berikut:

```
Test ID    : [ID dari tabel di atas]
Tanggal    : [dd/mm/yyyy]
Tester     : [nama anggota]
Environment: [Emulator/Device + Android version]
Input      : [data yang digunakan]
Expected   : [hasil yang diharapkan]
Actual     : [hasil aktual]
Status     : PASS / FAIL
Notes      : [opsional: screenshot path / error message]
```

---

## 8. Cara Jalankan Tests

```bash
# Unit tests (local JVM)
./gradlew test

# Instrumented tests (butuh emulator/device)
./gradlew connectedAndroidTest

# Unit tests dengan coverage report
./gradlew testDebugUnitTestCoverage

# Run test class spesifik
./gradlew test --tests "com.ngepet.domain.usecase.AddTransactionUseCaseTest"
```

**Target coverage minimum:** 70% untuk layer Domain dan Data.

---

*File ini dibuat sebagai panduan untuk coding agent. Implementasikan test files di direktori `app/src/test/` (unit) dan `app/src/androidTest/` (instrumented).*
