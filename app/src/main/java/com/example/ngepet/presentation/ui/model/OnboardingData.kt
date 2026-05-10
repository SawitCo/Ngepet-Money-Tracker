package com.example.ngepet.presentation.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import com.example.ngepet.presentation.ui.theme.Blue50
import com.example.ngepet.presentation.ui.theme.Blue600
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Orange50
import com.example.ngepet.presentation.ui.theme.Orange600
import com.example.ngepet.presentation.ui.theme.Pink400
import com.example.ngepet.presentation.ui.theme.Pink50

val onboardingPages = listOf(
    OnboardingPageUi(
        "Catat pengeluaran\ntanpa ribet",
        "Ngepet bantu kamu mencatat setiap rupiah yang keluar masuk dengan mudah. Nggak perlu lagi pusing ingat-ingat uang habis ke mana.",
        Icons.Filled.AccountBalanceWallet, Green600, Green50
    ),
    OnboardingPageUi(
        "Pahami kebiasaan\nbelanjamu",
        "Lihat laporan pengeluaran tiap bulan. Ngepet kasih tahu kategori mana yang paling sering bikin kantong jebol supaya kamu bisa lebih hemat.",
        Icons.Filled.AutoGraph, Blue600, Blue50
    ),
    OnboardingPageUi(
        "Atur budget\nbiar nggak boncos",
        "Tentukan batas pengeluaran untuk makan, main, dan lainnya. Ngepet siap ngingetin kalau pengeluaranmu udah hampir lewatin batas wajar.",
        Icons.Filled.Savings, Orange600, Orange50
    ),
    OnboardingPageUi(
        "Masukkan saldo awal",
        "Catat saldo kamu sekarang biar Ngepet bisa bantu ngitung saldo secara otomatis setiap ada pemasukan atau pengeluaran.",
        Icons.Filled.Wallet, Pink400, Pink50
    )
)