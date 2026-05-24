package com.example.vehiclecost.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vehiclecost.data.dao.CostDao
import com.example.vehiclecost.data.entity.VehicleCost
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.example.vehiclecost.data.repository.SettingsRepository

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float
)

class CostViewModel(
    private val costDao: CostDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val currentMonthStr = MutableStateFlow(dateFormat.format(Date()))
    
    val selectedMonth: StateFlow<String> = currentMonthStr.asStateFlow()

    fun changeMonth(monthStr: String) {
        currentMonthStr.value = monthStr
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthCosts: StateFlow<List<VehicleCost>> = currentMonthStr
        .flatMapLatest { month ->
            costDao.getCostsByPeriod("$month%")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthTotal: StateFlow<Double> = currentMonthStr
        .flatMapLatest { month ->
            costDao.getTotalAmountByPeriod("$month%")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = currentMonthCosts
        .map { costs ->
            val total = costs.sumOf { it.amount }
            if (total <= 0.0) return@map emptyList()

            costs.groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    CategoryBreakdown(cat, sum, (sum / total).toFloat())
                }
                .sortedByDescending { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val purchaseDateFlow = settingsRepository.purchaseDateFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carPhotoUriFlow = settingsRepository.carPhotoUriFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun savePurchaseDate(dateMillis: Long) {
        viewModelScope.launch { settingsRepository.savePurchaseDate(dateMillis) }
    }

    fun saveCarPhotoUri(uri: String) {
        viewModelScope.launch { settingsRepository.saveCarPhotoUri(uri) }
    }

    fun addCost(amount: Double, category: String, dateMillis: Long, note: String, tag: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val monthStr = dateFormat.format(Date(dateMillis))
            val cost = VehicleCost(
                amount = amount,
                category = category,
                date = dateMillis,
                monthStr = monthStr,
                note = note,
                tag = tag
            )
            costDao.insertCost(cost)
        }
    }

    fun deleteCost(cost: VehicleCost) {
        viewModelScope.launch(Dispatchers.IO) {
            costDao.deleteCost(cost)
        }
    }

    fun updateCost(cost: VehicleCost) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMonthStr = dateFormat.format(Date(cost.date))
            costDao.updateCost(cost.copy(monthStr = updatedMonthStr))
        }
    }
}

class CostViewModelFactory(
    private val costDao: CostDao,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CostViewModel(costDao, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
