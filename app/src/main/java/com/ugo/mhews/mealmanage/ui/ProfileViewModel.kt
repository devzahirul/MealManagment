package com.ugo.mhews.mealmanage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val auth: AuthRepository
) : ViewModel() {
    data class UiState(
        val name: String = "",
        val email: String = "",
        val loading: Boolean = true,
        val saving: Boolean = false,
        val snackbar: String? = null,
        val signedOut: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (val res = users.getCurrentProfile()) {
                is Result.Error -> _state.update { it.copy(loading = false, snackbar = res.error.message ?: "Failed to load profile") }
                is Result.Success -> _state.update { it.copy(loading = false, name = res.value.name, email = res.value.email) }
            }
        }
    }

    fun updateName(newName: String) { _state.update { it.copy(name = newName) } }

    fun save() {
        val name = _state.value.name.trim()
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            when (val res = users.updateCurrentName(name)) {
                is Result.Error -> _state.update { it.copy(saving = false, snackbar = res.error.message ?: "Save failed") }
                is Result.Success -> _state.update { it.copy(saving = false, snackbar = "Saved") }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _state.update { it.copy(signedOut = true, snackbar = "Signed out") }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbar = null) } }
}

