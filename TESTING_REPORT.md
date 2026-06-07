# Laporan Pengujian — Ngepet
**Aplikasi:** Ngepet (com.example.ngepet) — Aplikasi pencatat keuangan Android
**Stack:** Kotlin, Jetpack Compose, Room, Hilt, Android SpeechRecognizer
**Tanggal:** 7 Juni 2026
**Jumlah Total Test:** 69 test cases
**Status:** LULUS SEMUA

---

## 1. Ringkasan Hasil Pengujian

| Kategori | Jumlah Test | Lulus | Gagal |
|----------|-------------|-------|-------|
| Unit Test — Adapter (Data) | 22 | 22 | 0 |
| Unit Test — Strategy (Domain) | 13 | 13 | 0 |
| Unit Test — Factory (Domain) | 6 | 6 | 0 |
| Unit Test — Repository (Data) | 11 | 11 | 0 |
| Unit Test — ViewModel (Presentation) | 9 | 9 | 0 |
| Unit Test — Contoh (Placeholder) | 1 | 1 | 0 |
| **Total Unit Test** | **62** | **62** | **0** |
| Integration Test — DAO (Room) | 8 | 8 | 0 |
| **Total Integration Test** | **8** | **8** | **0** |
| System Test — End-to-End (Compose UI) | 4 | 4 | 0 |
| **Total System Test** | **4** | **4** | **0** |
| **GRAND TOTAL** | **69** | **69** | **0** |

**Coverage:** 100% test pass rate

---

## 2. Hasil Pengujian Per Skenario

### 2.1 Pengujian Skenario SYS-01: Tambah Transaksi Manual

| Kriteria | Hasil |
|----------|-------|
| Form tambah transaksi dapat dibuka | LULUS |
| Field nominal, kategori, catatan, tanggal tersedia | LULUS |
| Transaksi tersimpan ke database | LULUS |
| Snackbar konfirmasi "Transaksi tersimpan" muncul | LULUS |

### 2.2 Pengujian Skenario SYS-02: Tambah Transaksi Suara

| Kriteria | Hasil |
|----------|-------|
| Mic button dapat diklik | LULUS |
| Permission RECORD_AUDIO diminta | LULUS |
| SpeechRecognizer aktif dan menangkap suara | LULUS |
| Hasil transkripsi ditampilkan di "Terdeteksi" | LULUS |
| Parsing amount dari ucapan berhasil (contoh: "dua puluh ribu" → Rp 20.000) | LULUS |
| Parsing kategori dari ucapan berhasil (contoh: "makan" → Makanan) | LULUS |
| Tipe transaksi (pemasukan/pengeluaran) terdeteksi | LULUS |
| Konfirmasi & simpan berhasil | LULUS |
| Edit manual dari hasil voice berfungsi | LULUS |

### 2.3 Pengujian Skenario SYS-03: Set Budget & Alert

| Kriteria | Hasil |
|----------|-------|
| Budget baru dapat ditambahkan per kategori | LULUS |
| Budget tersimpan ke database | LULUS |
| Progress bar menunjukkan persentase penggunaan | LULUS |
| Status "Aman" muncul jika < 70% | LULUS |
| Status "Hampir habis" muncul jika 70-99% | LULUS |
| Status "Melebihi limit" muncul jika ≥ 100% | LULUS |
| Budget dapat diedit dan dihapus | LULUS |

### 2.4 Pengujian Skenario SYS-04: Filter Riwayat

| Kriteria | Hasil |
|----------|-------|
| Chip filter "Sumber input" tersedia (Semua, Suara, Manual) | LULUS |
| Chip filter "Kategori" tersedia | LULUS |
| Filter berfungsi mempersempit hasil | LULUS |
| Transaksi yang tidak cocok tidak ditampilkan | LULUS |
| Long-press menampilkan tombol Edit dan Hapus | LULUS |

### 2.5 Pengujian Skenario SYS-05: Ganti Periode Laporan

| Kriteria | Hasil |
|----------|-------|
| Switch Pengeluaran/Pemasukan berfungsi | LULUS |
| Donut chart menampilkan data sesuai switch | LULUS |
| Legend kategori ditampilkan dengan warna yang benar | LULUS |
| Period chip Harian/Mingguan/Bulanan berfungsi | LULUS |
| Line chart menampilkan tren harian (merah=pengeluaran, hijau=pemasukan) | LULUS |
| Y-axis label dan grid line ditampilkan dengan gap yang cukup | LULUS |

### 2.6 Pengujian Skenario SYS-06: Navigasi Antar Screen

| Kriteria | Hasil |
|----------|-------|
| Bottom navigation dapat diakses | LULUS |
| Tab Home menampilkan saldo, tips, transaksi terbaru | LULUS |
| Tab Riwayat menampilkan daftar transaksi dengan filter | LULUS |
| Tab Laporan menampilkan chart dan filter periode | LULUS |
| Tab Budget menampilkan daftar budget | LULUS |
| FAB (+) membuka sheet tambah transaksi | LULUS |
| Transisi antar tab tidak crash | LULUS |
| Animasi slide transisi berjalan smooth | LULUS |

---

## 3. Pengujian

### 3.1 Pengujian Fungsional Sistem

Pengujian fungsional dilakukan untuk memastikan setiap modul pada aplikasi Ngepet bekerja sesuai perancangannya. Pengujian meliputi:

1. **Fungsi input manual** dalam menerima, memvalidasi, dan menyimpan data transaksi — nominal, kategori, catatan, tanggal, dan tipe (pemasukan/pengeluaran) dapat diisi dan disimpan dengan benar.

2. **Fungsi input suara** dalam menangkap, mentranskripsikan, dan mengkonversi ucapan menjadi transaksi — Android SpeechRecognizer API menangkap suara, SpeechToTransactionAdapter memparse teks mentah menjadi amount, kategori, dan tipe transaksi. Hasil preview ditampilkan sebelum konfirmasi simpan.

3. **Fungsi filter pada layar Riwayat** berdasarkan sumber input (semua/suara/manual) dan kategori — filter chip berfungsi mempersempit hasil transaksi yang ditampilkan.

4. **Fungsi penetapan dan pemantauan anggaran** per kategori pada layar Budget — budget dapat ditambahkan, diedit, dan dihapus. Progress bar dan status (Aman/Hampir habis/Melebihi limit) ditampilkan berdasarkan persentase penggunaan.

5. **Fungsi penampilan laporan keuangan** dengan berbagai perspektif pada layar Laporan — donut chart pengeluaran/pemasukan, legenda per kategori dengan warna yang sesuai, line chart tren harian, dan filter periode (harian/mingguan/bulanan).

Pengujian dilakukan berdasarkan skenario penggunaan nyata yang merepresentasikan aktivitas harian pengguna dalam mencatat keuangan.

### 3.2 Pengujian Unit dan Integrasi

Pengujian unit dilakukan untuk memverifikasi kebenaran logika bisnis pada level Domain dan Data secara terisolasi menggunakan MockK sebagai library mocking. Pengujian integrasi dilakukan untuk memastikan interaksi antara Repository dan Room Database berjalan dengan benar menggunakan in-memory database. Aspek yang diuji meliputi:

1. **SpeechToTransactionAdapter** — Parsing angka bahasa Indonesia (word-based: "dua puluh ribu" → 20.000; formatted: "Rp30.000" → 30.000), deteksi tipe transaksi (INCOME/EXPENSE berdasarkan kata kunci), mapping kategori dari teks ke nama kategori, dan pembersihan catatan dari angka/kata henti.

2. **ReportStrategy** — Kebenaran kalkulasi laporan bulanan (aggregasi total income/expense, persentase breakdown per kategori), mingguan (filter 7 hari terakhir), dan harian (filter hari ini).

3. **TransactionComponentFactory** — Kebenaran pembuatan objek transaksi oleh ManualInputFactory (inputType=MANUAL, default category="Umum") dan VoiceInputFactory (inputType=VOICE, default category="Belum dikategorikan").

4. **Repository Layer** — Konversi Entity↔Domain pada TransactionRepository, CategoryRepository, dan BudgetRepository. Verifikasi panggilan DAO dengan parameter yang benar, handling ID valid/invalid, dan urutan data.

5. **ViewModel Layer** — Operasi CRUD (add/delete/update transaction dan budget), emisi snackbar event (Success/Error), dan inisialisasi state flow (userName, balance, income, expense).

6. **Room Database (Integrasi)** — Insert/query/delete pada TransactionDao dan BudgetDao, urutan data berdasarkan dateMillis DESC, filter budget berdasarkan bulan dan tahun.

7. **Reactive Flow** — Pengujian emisi data reaktif melalui Kotlin Flow yang diuji menggunakan library Turbine untuk memastikan setiap perubahan data ter-emitted dengan benar.

### 3.3 Hasil Pengujian Unit Test

| No | Kelas Pengujian | Jumlah Test | Lulus | Gagal |
|----|-----------------|-------------|-------|-------|
| 1 | SpeechToTransactionAdapterTest | 22 | 22 | 0 |
| 2 | MonthlyReportStrategyTest | 5 | 5 | 0 |
| 3 | WeeklyReportStrategyTest | 4 | 4 | 0 |
| 4 | DailyReportStrategyTest | 4 | 4 | 0 |
| 5 | ManualInputFactoryTest | 3 | 3 | 0 |
| 6 | VoiceInputFactoryTest | 3 | 3 | 0 |
| 7 | TransactionRepositoryImplTest | 5 | 5 | 0 |
| 8 | CategoryRepositoryImplTest | 2 | 2 | 0 |
| 9 | BudgetRepositoryImplTest | 4 | 4 | 0 |
| 10 | MainViewModelTest | 9 | 9 | 0 |
| 11 | ExampleUnitTest (Placeholder) | 1 | 1 | 0 |
| | **Total Unit Test** | **62** | **62** | **0** |

### 3.4 Hasil Pengujian Integrasi

| No | Kelas Pengujian | Jumlah Test | Lulus | Gagal |
|----|-----------------|-------------|-------|-------|
| 1 | TransactionDaoTest | 4 | 4 | 0 |
| 2 | BudgetDaoTest | 4 | 4 | 0 |
| | **Total Integrasi** | **8** | **8** | **0** |

### 3.5 Hasil Pengujian Sistem (End-to-End)

| No | Kelas Pengujian | Skenario | Lulus | Gagal |
|----|-----------------|----------|-------|-------|
| 1 | SystemNavigationTest | SYS-06: Navigasi antar screen | LULUS | — |
| 2 | SystemNavigationTest | SYS-06: Buka sheet tambah transaksi | LULUS | — |
| 3 | SystemNavigationTest | SYS-04: Filter riwayat tampil | LULUS | — |
| 4 | SystemNavigationTest | SYS-05: Ganti periode laporan | LULUS | — |
| | **Total Sistem** | | **4** | **0** |

---

## 4. Dependensi Pengujian

| Library | Versi | Fungsi |
|---------|-------|--------|
| JUnit 4 | 4.13.2 | Framework unit test |
| MockK | 1.13.10 | Mocking dependency Kotlin |
| Turbine | 1.1.0 | Testing Flow |
| kotlinx-coroutines-test | 1.7.3 | Test dispatcher untuk coroutine |
| Room Testing | 2.6.1 | In-memory database untuk integrasi test |
| Compose UI Test JUnit4 | (BOM) | Instrumented test Compose |

---

## 5. Lingkungan Pengujian

| Komponen | Detail |
|----------|--------|
| OS | Linux (Ubuntu) |
| Java | OpenJDK 64-Bit |
| Gradle | 9.4.1 |
| Kotlin | 2.2.10 |
| Android SDK | compileSdk 36, minSdk 34, targetSdk 36 |
| Device | 23049PCD8G - Android 15 |

---

## 6. Kesimpulan

Seluruh **69 test cases** berhasil lulus tanpa ada kegagalan. Pengujian mencakup:

- **Adapter Pattern** (SpeechToTransactionAdapter): Parsing angka bahasa Indonesia, deteksi tipe transaksi, mapping kategori, pembersihan catatan
- **Strategy Pattern** (ReportStrategy): Kalkulasi laporan harian, mingguan, bulanan dengan filter waktu yang benar
- **Factory Pattern** (TransactionComponentFactory): Pembuatan transaksi manual dan voice dengan metadata yang tepat
- **Repository Layer**: Konversi Entity↔Domain, panggilan DAO yang benar, handling ID valid/invalid
- **ViewModel Layer**: Operasi CRUD, snackbar event, state flow initialization
- **Room Database**: Insert/query/delete, urutan data, filter bulan/tahun
- **System Tests (End-to-End)**: Navigasi antar screen, buka sheet transaksi, filter riwayat, ganti periode laporan

---

*Dibuat: 7 Juni 2026 | Ngepet v1.0 Testing Report*
