package com.example.ngepet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngepet.domain.strategy.MonthlyReportStrategy
import com.example.ngepet.domain.strategy.ReportStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor() : ViewModel() {

    private var strategy: ReportStrategy = MonthlyReportStrategy()

    fun setStrategy(newStrategy: ReportStrategy) {
        strategy = newStrategy
    }

    fun getCurrentStrategy(): ReportStrategy = strategy
}
