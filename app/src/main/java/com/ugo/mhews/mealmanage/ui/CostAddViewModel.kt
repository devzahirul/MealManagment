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
    data class UiState(
        val isSubmitting: Boolean = false,
        val snackbar: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

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

