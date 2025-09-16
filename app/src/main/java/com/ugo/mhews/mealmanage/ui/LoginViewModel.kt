package com.ugo.mhews.mealmanage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.usecase.SignIn
import com.ugo.mhews.mealmanage.domain.usecase.SignUp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signIn: SignIn,
    private val signUp: SignUp
) : ViewModel() {
    data class UiState(
        val email: String = "",
        val password: String = "",
        val isSignUp: Boolean = false,
        val isLoading: Boolean = false,
        val snackbar: String? = null,
        val loggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun setEmail(e: String) { _state.update { it.copy(email = e) } }
    fun setPassword(p: String) { _state.update { it.copy(password = p) } }
    fun toggleMode() { _state.update { it.copy(isSignUp = !it.isSignUp) } }

    fun submit() {
        val e = _state.value.email.trim()
        val p = _state.value.password
        if (!e.contains("@") || p.length < 6) {
            _state.update { it.copy(snackbar = "Enter valid email and 6+ char password") }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val res = if (_state.value.isSignUp) signUp(e, p) else signIn(e, p)
            when (res) {
                is Result.Error -> _state.update { it.copy(isLoading = false, snackbar = res.error.message ?: "Auth failed") }
                is Result.Success -> _state.update { it.copy(isLoading = false, loggedIn = true) }
            }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbar = null) } }
    fun consumeLoggedIn() { _state.update { it.copy(loggedIn = false) } }
}
