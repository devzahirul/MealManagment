package com.ugo.mhews.mealmanage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.core.DateProvider
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetMealForDate
import com.ugo.mhews.mealmanage.domain.usecase.ObserveMealsForMonth
import com.ugo.mhews.mealmanage.domain.usecase.SetMealForDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MealViewModel @Inject constructor(
    private val observeMealsForMonth: ObserveMealsForMonth,
    private val getMealForDate: GetMealForDate,
    private val setMealForDate: SetMealForDate,
    private val getAllMealsForDate: GetAllMealsForDate,
    private val userRepository: UserRepository,
    private val dateProvider: DateProvider
) : ViewModel() {

    data class DetailItem(val uid: String, val name: String, val count: Int)

    data class UiState(
        val month: YearMonth = YearMonth.now(),
        val today: LocalDate = LocalDate.now(),
        val monthMeals: Map<LocalDate, Int> = emptyMap(),
        val snackbarMessage: String? = null,

        val showEditorDate: LocalDate? = null,
        val loadingCount: Boolean = false,
        val count: Int = 0,
        val saving: Boolean = false,

        val showDetailsDate: LocalDate? = null,
        val detailsLoading: Boolean = false,
        val detailsError: String? = null,
        val details: List<DetailItem> = emptyList(),
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var monthJob: Job? = null

    init {
        // Initialize date state based on core DateProvider
        val today = dateProvider.today()
        _state.update { it.copy(today = today, month = YearMonth.from(today)) }
        // Start observing the current month on init
        observeCurrentMonth()
    }

    fun onPrevMonth() {
        _state.update { it.copy(month = it.month.minusMonths(1)) }
        observeCurrentMonth()
    }

    fun onNextMonth() {
        _state.update { it.copy(month = it.month.plusMonths(1)) }
        observeCurrentMonth()
    }

    private fun observeCurrentMonth() {
        monthJob?.cancel()
        val month = _state.value.month
        monthJob = viewModelScope.launch {
            observeMealsForMonth(month).collect { result ->
                when (result) {
                    is Result.Success -> _state.update { it.copy(monthMeals = result.value) }
                    is Result.Error -> _state.update { it.copy(snackbarMessage = result.error.message ?: "Failed to load meals") }
                }
            }
        }
    }

    fun openDate(date: LocalDate) {
        val today = _state.value.today
        if (date.isBefore(today)) {
            // Past date: show details
            _state.update { it.copy(showDetailsDate = date, detailsLoading = true, detailsError = null, details = emptyList()) }
            viewModelScope.launch {
                when (val res = getAllMealsForDate(date)) {
                    is Result.Error -> _state.update {
                        it.copy(detailsLoading = false, detailsError = res.error.message ?: "Failed to load")
                    }
                    is Result.Success -> {
                        val uids = res.value.map { it.uid }.toSet()
                        val namesRes = userRepository.getNames(uids)
                        val nameMap = when (namesRes) {
                            is Result.Success -> namesRes.value
                            is Result.Error -> emptyMap()
                        }
                        val list = res.value.map { m ->
                            DetailItem(
                                uid = m.uid,
                                name = nameMap[m.uid].orEmpty().ifBlank { "User ${m.uid.take(6)}" },
                                count = m.count
                            )
                        }.sortedByDescending { it.count }
                        _state.update { it.copy(detailsLoading = false, details = list) }
                    }
                }
            }
            return
        }
        _state.update { it.copy(showEditorDate = date, loadingCount = true) }
        viewModelScope.launch {
            when (val res = getMealForDate(date)) {
                is Result.Error -> _state.update {
                    it.copy(loadingCount = false, snackbarMessage = res.error.message ?: "Load failed", count = 0)
                }
                is Result.Success -> _state.update { it.copy(loadingCount = false, count = res.value.count) }
            }
        }
    }

    fun dismissEditor() { _state.update { if (!it.saving) it.copy(showEditorDate = null) else it } }
    fun dismissDetails() { _state.update { it.copy(showDetailsDate = null) } }

    fun decCount() { _state.update { if (!it.loadingCount && it.count > 0) it.copy(count = it.count - 1) else it } }
    fun incCount() { _state.update { if (!it.loadingCount) it.copy(count = it.count + 1) else it } }

    fun saveCount() {
        val date = _state.value.showEditorDate ?: return
        val count = _state.value.count
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val res = setMealForDate(date, count)) {
                is Result.Error -> _state.update {
                    it.copy(saving = false, snackbarMessage = res.error.message ?: "Save failed")
                }
                is Result.Success -> _state.update {
                    it.copy(saving = false, showEditorDate = null, snackbarMessage = "Saved")
                }
            }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbarMessage = null) } }
}
