package com.ugo.mhews.mealmanage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.usecase.AddCost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CostAddViewModel @Inject constructor(
    private val addCost: AddCost
) : ViewModel() {
    data class UtilityEntry(val name: String, val cost: Double)

    data class UiState(
        val isSubmitting: Boolean = false,
        val snackbar: String? = null,
        val utilities: List<UtilityEntry> = emptyList(),
        val totalUtility: Double = 0.0,
        val totalPersons: Int = 1,
        val perPersonUtility: Double = 0.0
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun addUtility(name: String, cost: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || cost <= 0) return
        _state.update { current ->
            val updated = current.utilities + UtilityEntry(trimmed, cost)
            val total = updated.sumOf { it.cost }
            val perPerson = if (current.totalPersons > 0) total / current.totalPersons else 0.0
            current.copy(utilities = updated, totalUtility = total, perPersonUtility = perPerson)
        }
    }

    fun updateUtilityPersons(count: Int) {
        val safeCount = if (count < 1) 1 else count
        _state.update { current ->
            val perPerson = if (safeCount > 0) current.totalUtility / safeCount else 0.0
            current.copy(totalPersons = safeCount, perPersonUtility = perPerson)
        }
    }

    fun submit(name: String, cost: Double, timestampMillis: Long, onSuccess: () -> Unit) {
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val res = addCost(CostItem(name = name, cost = cost, timestampMillis = timestampMillis))) {
                is Result.Error -> _state.update { it.copy(isSubmitting = false, snackbar = res.error.message ?: "Failed") }
                is Result.Success -> {
                    onSuccess()
                    _state.update { it.copy(isSubmitting = false, snackbar = "Saved successfully") }
                }
            }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbar = null) } }
}
