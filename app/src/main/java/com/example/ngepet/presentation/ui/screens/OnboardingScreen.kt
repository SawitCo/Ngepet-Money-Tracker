package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.onboardingPages
import com.example.ngepet.presentation.ui.theme.Green100
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.SurfaceWarm

@Composable
fun OnboardingScreen(initialName: String, initialBalance: Long, onComplete: (String, Long) -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf(initialName) }
    var balanceInput by remember { mutableStateOf(if (initialBalance > 0) initialBalance.toString() else "") }
    val isLastPage = currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ngepet", color = Green800, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("Ngedukasi Dompet", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = { onComplete(name, balanceInput.toLongOrNull() ?: 0L) }) {
                Text("Lewati", color = Green600)
            }
        }

        AnimatedContent(targetState = currentPage, transitionSpec = {
            fadeIn(tween(150)) togetherWith fadeOut(tween(150))
        }) { pageIndex ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val p = onboardingPages[pageIndex]
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(p.bg),
                    contentAlignment = Alignment.Center
                ) {
                    SymbolBox(p.icon, p.color, Color.White.copy(alpha = 0.72f), 86.dp)
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    p.title,
                    color = Ink,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    p.description,
                    color = Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                if (pageIndex == 0) {
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nama panggilan") },
                        placeholder = { Text("Contoh: Budi") },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                if (pageIndex == 3) {
                    Spacer(Modifier.height(24.dp))
                    val displayBalance = formatRupiah(balanceInput)
                    OutlinedTextField(
                        value = displayBalance,
                        onValueChange = { balanceInput = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Saldo awal") },
                        placeholder = { Text("Contoh: 500.000") },
                        leadingIcon = { Text("Rp", color = Muted) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    val dotWidth by animateDpAsState(
                        targetValue = if (index == currentPage) 22.dp else 7.dp,
                        animationSpec = tween(150)
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = dotWidth, height = 7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == currentPage) Green600 else Green100)
                    )
                }
            }
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete(name, balanceInput.toLongOrNull() ?: 0L)
                    } else {
                        currentPage += 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(if (isLastPage) "Mulai pakai Ngepet" else "Lanjut")
            }
        }
    }
}
