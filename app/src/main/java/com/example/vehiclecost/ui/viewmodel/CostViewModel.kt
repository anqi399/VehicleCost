package com.example.vehiclecost.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vehiclecost.data.dao.CostDao
import com.example.vehiclecost.data.entity.VehicleCost
import com.example.vehiclecost.notification.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.vehiclecost.data.repository.SettingsRepository

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float
)

enum class DashboardPeriod { WEEK, MONTH, YEAR, ALL }

data class TrendData(val diff: Double, val isPositive: Boolean, val hasHistory: Boolean)

class CostViewModel(
    private val costDao: CostDao,
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {

    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    
    // --- Records Screen States ---
    private val currentMonthStr = MutableStateFlow(dateFormat.format(Date()))
    val selectedMonth: StateFlow<String> = currentMonthStr.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("全部")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    fun changeMonth(monthStr: String) { currentMonthStr.value = monthStr }
    fun setCategoryFilter(category: String) { _selectedCategoryFilter.value = category }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthCosts: StateFlow<List<VehicleCost>> = combine(currentMonthStr, _selectedCategoryFilter) { month, category ->
        Pair(month, category)
    }.flatMapLatest { (month, category) ->
        when (category) {
            "全部" -> costDao.getCostsByPeriod("$month%")
            "固定停车" -> costDao.getCostsByPeriodAndTag("$month%", "固定月租")
            "临时停车" -> costDao.getCostsByPeriodAndTag("$month%", "临时停车")
            else -> costDao.getCostsByPeriodAndCategory("$month%", category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Dashboard States ---
    private val _dashboardPeriodType = MutableStateFlow(DashboardPeriod.MONTH)
    val dashboardPeriodType: StateFlow<DashboardPeriod> = _dashboardPeriodType.asStateFlow()

    fun setDashboardPeriod(period: DashboardPeriod) { _dashboardPeriodType.value = period }

    private fun getPeriodDates(period: DashboardPeriod, offset: Int = 0): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        when (period) {
            DashboardPeriod.ALL -> {
                if (offset == 0) {
                    return Pair(0L, Long.MAX_VALUE)
                } else {
                    return Pair(0L, 0L)
                }
            }
            DashboardPeriod.WEEK -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.add(Calendar.WEEK_OF_YEAR, offset)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                return Pair(start, cal.timeInMillis)
            }
            DashboardPeriod.MONTH -> {
                cal.add(Calendar.MONTH, offset)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                return Pair(start, cal.timeInMillis)
            }
            DashboardPeriod.YEAR -> {
                cal.add(Calendar.YEAR, offset)
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                return Pair(start, cal.timeInMillis)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardTotal: StateFlow<Double> = _dashboardPeriodType.flatMapLatest { period ->
        val dates = getPeriodDates(period, 0)
        costDao.getTotalAmountBetween(dates.first, dates.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardCosts: StateFlow<List<VehicleCost>> = _dashboardPeriodType.flatMapLatest { period ->
        val dates = getPeriodDates(period, 0)
        costDao.getCostsBetween(dates.first, dates.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardCategoryBreakdown: StateFlow<List<CategoryBreakdown>> = dashboardCosts
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardTrend: StateFlow<TrendData> = _dashboardPeriodType.flatMapLatest { period ->
        val currentDates = getPeriodDates(period, 0)
        val prevDates = getPeriodDates(period, -1)
        
        combine(
            costDao.getTotalAmountBetween(currentDates.first, currentDates.second),
            costDao.getTotalAmountBetween(prevDates.first, prevDates.second)
        ) { current, prev ->
            val diff = current - prev
            TrendData(diff = diff, isPositive = diff > 0, hasHistory = prev > 0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrendData(0.0, false, false))


    // --- Settings & Base Ops ---
    val purchaseDateFlow = settingsRepository.purchaseDateFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val carPhotoUriFlow = settingsRepository.carPhotoUriFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val reminderEnabledFlow = settingsRepository.reminderEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun savePurchaseDate(dateMillis: Long) { viewModelScope.launch { settingsRepository.savePurchaseDate(dateMillis) } }
    fun saveCarPhotoUri(uri: String) { viewModelScope.launch { settingsRepository.saveCarPhotoUri(uri) } }

    fun addCost(amount: Double, category: String, dateMillis: Long, note: String, tag: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val monthStr = dateFormat.format(Date(dateMillis))
            val cost = VehicleCost(amount = amount, category = category, date = dateMillis, monthStr = monthStr, note = note, tag = tag)
            costDao.insertCost(cost)
        }
    }

    fun deleteCost(cost: VehicleCost) { viewModelScope.launch(Dispatchers.IO) { costDao.deleteCost(cost) } }

    fun updateCost(cost: VehicleCost) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMonthStr = dateFormat.format(Date(cost.date))
            costDao.updateCost(cost.copy(monthStr = updatedMonthStr))
        }
    }

    fun importLegacyData(context: android.content.Context, onComplete: (Boolean, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = context.assets.open("import_template.json").bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonStr)
                val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val existingCosts = costDao.getAllCosts().first()
                val costsToInsert = mutableListOf<VehicleCost>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val dateStr = obj.getString("dateStr")
                    val category = obj.getString("category")
                    val amount = obj.getDouble("amount")
                    val note = obj.optString("note", "")
                    val tag = obj.optString("tag", "")
                    
                    val parsedDate = parseFormat.parse(dateStr)
                    if (parsedDate != null) {
                        val isDuplicate = existingCosts.any {
                            it.date == parsedDate.time &&
                            it.amount == amount &&
                            it.category == category &&
                            it.note == note
                        }
                        
                        if (!isDuplicate) {
                            costsToInsert.add(
                                VehicleCost(
                                    amount = amount,
                                    category = category,
                                    date = parsedDate.time,
                                    monthStr = dateFormat.format(parsedDate),
                                    note = note,
                                    tag = tag
                                )
                            )
                        }
                    }
                }
                if (costsToInsert.isNotEmpty()) {
                    costDao.insertCosts(costsToInsert)
                }
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete(true, costsToInsert.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete(false, 0)
                }
            }
        }
    }

    fun exportDataToUri(context: android.content.Context, uri: android.net.Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingCosts = costDao.getAllCosts().first()
                val jsonArray = org.json.JSONArray()
                
                for (cost in existingCosts) {
                    val obj = org.json.JSONObject()
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(cost.date))
                    obj.put("dateStr", dateStr)
                    obj.put("category", cost.category)
                    obj.put("amount", cost.amount)
                    if (cost.note.isNotBlank()) obj.put("note", cost.note)
                    if (cost.tag.isNotBlank()) obj.put("tag", cost.tag)
                    jsonArray.put(obj)
                }

                val jsonStr = jsonArray.toString(2)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonStr.toByteArray())
                }
                
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    // --- Daily Reminder ---
    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderEnabled(enabled)
            val context = getApplication<Application>()
            if (enabled) {
                ReminderScheduler.scheduleDailyReminder(context)
            } else {
                ReminderScheduler.cancelReminder(context)
            }
        }
    }

}

class CostViewModelFactory(
    private val costDao: CostDao,
    private val settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CostViewModel(costDao, settingsRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
