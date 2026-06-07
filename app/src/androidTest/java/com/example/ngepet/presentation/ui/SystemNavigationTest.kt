package com.example.ngepet.presentation.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.ngepet.MainActivity
import org.junit.Rule
import org.junit.Test

class SystemNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun SYS06_navigateToAllScreens_noCrash() {
        // Wait for onboarding or main screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes().isNotEmpty()
        }

        // Skip onboarding if present
        val lewatiNodes = composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes()
        if (lewatiNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Lewati").performClick()
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // Tap History tab
        composeTestRule.onNodeWithContentDescription("Navigasi ke Riwayat").performClick()
        composeTestRule.waitForIdle()

        // Tap Report tab
        composeTestRule.onNodeWithContentDescription("Navigasi ke Laporan").performClick()
        composeTestRule.waitForIdle()

        // Tap Budget tab
        composeTestRule.onNodeWithContentDescription("Navigasi ke Budget").performClick()
        composeTestRule.waitForIdle()

        // Tap Home tab
        composeTestRule.onNodeWithContentDescription("Navigasi ke Home").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun SYS06_openAddTransactionSheet() {
        // Wait for main screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("Tambah transaksi").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes().isNotEmpty()
        }

        // Skip onboarding if present
        val lewatiNodes = composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes()
        if (lewatiNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Lewati").performClick()
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule.onAllNodesWithContentDescription("Tambah transaksi").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // Tap FAB to open add transaction
        composeTestRule.onNodeWithContentDescription("Tambah transaksi").performClick()
        composeTestRule.waitForIdle()

        // Verify sheet opens with title
        composeTestRule.onNodeWithText("Tambah transaksi").assertIsDisplayed()

        // Close sheet
        composeTestRule.onNodeWithContentDescription("Tutup").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun SYS04_historyScreen_displaysFilterChips() {
        // Wait for main screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes().isNotEmpty()
        }

        // Skip onboarding if present
        val lewatiNodes = composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes()
        if (lewatiNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Lewati").performClick()
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // Navigate to History
        composeTestRule.onNodeWithContentDescription("Navigasi ke Riwayat").performClick()
        composeTestRule.waitForIdle()

        // Verify filter chips exist
        composeTestRule.onAllNodesWithText("Riwayat").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Filter").onFirst().assertIsDisplayed()
    }

    @Test
    fun SYS05_reportScreen_switchPeriod() {
        // Wait for main screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes().isNotEmpty()
        }

        // Skip onboarding if present
        val lewatiNodes = composeTestRule.onAllNodesWithText("Lewati").fetchSemanticsNodes()
        if (lewatiNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Lewati").performClick()
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // Navigate to Report
        composeTestRule.onNodeWithContentDescription("Navigasi ke Laporan").performClick()
        composeTestRule.waitForIdle()

        // Verify report screen
        composeTestRule.onAllNodesWithText("Laporan").onFirst().assertIsDisplayed()

        // Switch to Weekly
        composeTestRule.onNodeWithText("Mingguan").performClick()
        composeTestRule.waitForIdle()

        // Switch to Daily
        composeTestRule.onNodeWithText("Harian").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Monthly
        composeTestRule.onNodeWithText("Bulanan").performClick()
        composeTestRule.waitForIdle()
    }
}
