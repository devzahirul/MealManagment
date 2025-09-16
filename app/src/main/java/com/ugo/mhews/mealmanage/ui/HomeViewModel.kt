package com.ugo.mhews.mealmanage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.core.DateProvider
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetCostsForUserRange
import com.ugo.mhews.mealmanage.domain.usecase.GetMealsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalCostForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalMealsForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetUserNames
import com.ugo.mhews.mealmanage.domain.time.MonthRangeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTotalCostForRange: GetTotalCostForRange,
    private val getTotalMealsForRange: GetTotalMealsForRange,
    private val getTotalsByUserForRange: GetTotalsByUserForRange,
    private val getMealsByUserForRange: GetMealsByUserForRange,
    private val getCostsForUserRange: GetCostsForUserRange,
    private val getAllMealsForDate: GetAllMealsForDate,
    private val getUserNames: GetUserNames,
    private val dateProvider: DateProvider,
    private val monthRangeCalculator: MonthRangeCalculator
) : ViewModel() {

    data class UserTotal(val uid: String, val name: String, val total: Double)

    data class UtilityEntry(val name: String, val cost: Double)

    data class UiState(
        val selectedMonth: YearMonth = YearMonth.now(),
        val zone: ZoneId = ZoneId.systemDefault(),

        val monthTotal: Double? = null,
        val monthMealsTotal: Int? = null,
        val monthLoading: Boolean = true,
        val monthErr: String? = null,

        val userTotals: List<UserTotal> = emptyList(),
        val byUserLoading: Boolean = true,
        val byUserErr: String? = null,
        val byUserMeals: Map<String, Int> = emptyMap(),

        val todayMealsLoading: Boolean = true,
        val todayMealsErr: String? = null,
        val todayMealsTotal: Int = 0,
        val todayMealsTop: List<Pair<String, Int>> = emptyList(),

        val selectedUser: UserTotal? = null,
        val userCostsLoading: Boolean = false,
        val userCostsErr: String? = null,
        val userCosts: List<CostItem> = emptyList(),

        val utilities: List<UtilityEntry> = emptyList(),
        val utilityTotal: Double = 0.0,
        val utilityPersons: Int = 1,
        val utilityPerPerson: Double = 0.0,

        val snackbar: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // Initialize the selected month based on provided date provider
        val today = dateProvider.today()
        _state.update { it.copy(selectedMonth = YearMonth.from(today)) }
        refreshAll()
        loadByUser()
        loadTodayMeals()
    }

    fun setMonth(month: YearMonth) {
        _state.update { it.copy(selectedMonth = month) }
        refreshAll(); loadByUser()
    }

    fun prevMonth() = setMonth(_state.value.selectedMonth.minusMonths(1))
    fun nextMonth() = setMonth(_state.value.selectedMonth.plusMonths(1))

    fun refreshAll() {
        _state.update { it.copy(monthLoading = true, monthErr = null, monthMealsTotal = null) }
        val ym = _state.value.selectedMonth
        val zone = _state.value.zone
        val dateRange = monthRangeCalculator.of(ym)
        val epochRange = dateRange.toEpochMillis(zone)
        viewModelScope.launch {
            var monthErr: String? = null
            var total: Double? = null
            when (val costRes = getTotalCostForRange(epochRange.startInclusive, epochRange.endExclusive, null)) {
                is Result.Error -> monthErr = costRes.error.message ?: "Unknown error"
                is Result.Success -> total = costRes.value
            }
            var mealsTotal: Int? = null
            when (val mealRes = getTotalMealsForRange(dateRange.start, dateRange.endExclusive)) {
                is Result.Error -> monthErr = monthErr ?: mealRes.error.message ?: "Unknown error"
                is Result.Success -> mealsTotal = mealRes.value
            }
            _state.update { it.copy(monthLoading = false, monthErr = monthErr, monthTotal = total, monthMealsTotal = mealsTotal) }
        }
    }

    fun loadTodayMeals() {
        _state.update { it.copy(todayMealsLoading = true, todayMealsErr = null, todayMealsTotal = 0, todayMealsTop = emptyList()) }
        val date = dateProvider.today(_state.value.zone)
        viewModelScope.launch {
            when (val listRes = getAllMealsForDate(date)) {
                is Result.Error -> _state.update { it.copy(todayMealsLoading = false, todayMealsErr = listRes.error.message ?: "Unknown error") }
                is Result.Success -> {
                    val list = listRes.value
                    val total = list.sumOf { it.count }
                    val uids = list.map { it.uid }.toSet()
                    val nameMap = when (val namesRes = getUserNames(uids)) {
                        is Result.Success -> namesRes.value
                        is Result.Error -> emptyMap()
                    }
                    val top = list.map { m ->
                        val name = nameMap[m.uid].orEmpty().ifBlank { "User ${m.uid.take(6)}" }
                        name to m.count
                    }.sortedByDescending { it.second }
                    _state.update { it.copy(todayMealsLoading = false, todayMealsTotal = total, todayMealsTop = top) }
                }
            }
        }
    }

    fun loadByUser() {
        _state.update { it.copy(byUserLoading = true, byUserErr = null) }
        val ym = _state.value.selectedMonth
        val zone = _state.value.zone
        val dateRange = monthRangeCalculator.of(ym)
        val epochRange = dateRange.toEpochMillis(zone)
        viewModelScope.launch {
            var byErr: String? = null
            val sumsRes = getTotalsByUserForRange(epochRange.startInclusive, epochRange.endExclusive)
            val sums = when (sumsRes) {
                is Result.Error -> { byErr = sumsRes.error.message ?: "Unknown error"; emptyMap() }
                is Result.Success -> sumsRes.value
            }
            val nameMap = when (val namesRes = getUserNames(sums.keys.toSet())) {
                is Result.Error -> { byErr = byErr ?: namesRes.error.message ?: "Unknown error"; emptyMap() }
                is Result.Success -> namesRes.value
            }
            val list = sums.entries.map { (uid, total) ->
                val name = nameMap[uid].orEmpty().ifBlank { "User ${uid.take(6)}" }
                UserTotal(uid, name, total)
            }.sortedByDescending { it.total }
            // Fetch meals by user for expected balances
            val byMeals = when (val mealsByUserRes = getMealsByUserForRange(dateRange.start, dateRange.endExclusive)) {
                is Result.Error -> { byErr = byErr ?: mealsByUserRes.error.message ?: "Unknown error"; emptyMap() }
                is Result.Success -> mealsByUserRes.value
            }
            _state.update { it.copy(byUserLoading = false, byUserErr = byErr, userTotals = list, byUserMeals = byMeals) }
        }
    }

    fun selectUser(user: UserTotal?) { _state.update { it.copy(selectedUser = user) } }

    fun loadCostsForSelectedUser() {
        val user = _state.value.selectedUser ?: return
        val ym = _state.value.selectedMonth
        val zone = _state.value.zone
        val epochRange = monthRangeCalculator.of(ym).toEpochMillis(zone)
        _state.update { it.copy(userCostsLoading = true, userCostsErr = null, userCosts = emptyList()) }
        viewModelScope.launch {
            when (val res = getCostsForUserRange(user.uid, epochRange.startInclusive, epochRange.endExclusive)) {
                is Result.Error -> _state.update { it.copy(userCostsLoading = false, userCostsErr = res.error.message ?: "Unknown error") }
                is Result.Success -> _state.update { it.copy(userCostsLoading = false, userCosts = res.value) }
            }
        }
    }

    fun addUtility(name: String, cost: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || cost <= 0) return
        _state.update { current ->
            val updatedList = current.utilities + UtilityEntry(trimmed, cost)
            val total = updatedList.sumOf { it.cost }
            val perPerson = if (current.utilityPersons > 0) total / current.utilityPersons else total
            current.copy(utilities = updatedList, utilityTotal = total, utilityPerPerson = perPerson)
        }
    }

    fun updateUtilityPersons(count: Int) {
        val safeCount = if (count < 1) 1 else count
        _state.update { current ->
            val perPerson = if (safeCount > 0) current.utilityTotal / safeCount else 0.0
            current.copy(utilityPersons = safeCount, utilityPerPerson = perPerson)
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbar = null) } }
}
