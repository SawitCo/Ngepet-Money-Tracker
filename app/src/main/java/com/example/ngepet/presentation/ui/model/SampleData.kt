package com.example.ngepet.presentation.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import com.example.ngepet.ui.theme.CardSoft
import com.example.ngepet.ui.theme.Danger
import com.example.ngepet.ui.theme.DangerBg
import com.example.ngepet.ui.theme.Green50
import com.example.ngepet.ui.theme.Green600
import com.example.ngepet.ui.theme.Muted
import com.example.ngepet.ui.theme.Pink400
import com.example.ngepet.ui.theme.Pink50
import com.example.ngepet.ui.theme.Warning
import com.example.ngepet.ui.theme.WarningBg

val recentTransactions = listOf(
    TransactionUi("Warung makan", "Makanan", "-Rp 25.000", false, "Suara", Icons.Filled.Restaurant, Green600, Green50),
    TransactionUi("Grab ke kampus", "Transport", "-Rp 18.000", false, "Manual", Icons.Filled.DirectionsBus, Color(0xFF185FA5), Color(0xFFE6F1FB)),
    TransactionUi("Gaji freelance", "Pekerjaan", "+Rp 500.000", true, "Manual", Icons.Filled.Work, Green600, Green50)
)

val historyTransactions = recentTransactions + listOf(
    TransactionUi("Indomaret", "Belanja", "-Rp 47.000", false, "Suara", Icons.Filled.ShoppingBag, Pink400, Pink50),
    TransactionUi("Tagihan listrik", "Tagihan", "-Rp 120.000", false, "Manual", Icons.Filled.Receipt, Warning, WarningBg),
    TransactionUi("Apotek Kimia Farma", "Kesehatan", "-Rp 35.000", false, "Suara", Icons.Filled.Favorite, Danger, DangerBg)
)

val transactionCategories = listOf(
    CategoryUi("Makanan", Icons.Filled.Restaurant, Green600, Green50),
    CategoryUi("Transport", Icons.Filled.DirectionsBus, Color(0xFF185FA5), Color(0xFFE6F1FB)),
    CategoryUi("Belanja", Icons.Filled.ShoppingBag, Pink400, Pink50),
    CategoryUi("Tagihan", Icons.Filled.Receipt, Warning, WarningBg),
    CategoryUi("Pekerjaan", Icons.Filled.Work, Green600, Green50),
    CategoryUi("Kesehatan", Icons.Filled.Favorite, Danger, DangerBg),
    CategoryUi("Lainnya", Icons.Filled.MoreHoriz, Muted, CardSoft)
)

val onboardingPages = listOf(
    OnboardingPageUi(
        title = "Catat uang harianmu",
        description = "Pantau pemasukan dan pengeluaran tanpa ribet, dari makan siang sampai gaji freelance.",
        icon = Icons.Filled.Receipt,
        color = Green600,
        bg = Green50
    ),
    OnboardingPageUi(
        title = "Pakai suara kalau lagi cepat",
        description = "Bilang transaksimu, cek hasil deteksi, lalu simpan setelah kamu konfirmasi.",
        icon = Icons.Filled.Mic,
        color = Pink400,
        bg = Pink50
    ),
    OnboardingPageUi(
        title = "Jaga budget sebelum kebablasan",
        description = "Lihat progres kategori dan dapatkan peringatan saat pengeluaran mulai mendekati limit.",
        icon = Icons.Filled.TrackChanges,
        color = Warning,
        bg = WarningBg
    )
)
