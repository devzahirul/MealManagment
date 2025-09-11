package com.ugo.mhews.mealmanage.ui

import com.ugo.mhews.mealmanage.MainDispatcherRule
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeAuth : AuthRepository {
    override suspend fun signIn(email: String, password: String) = Result.Success("u1")
    override suspend fun signUp(email: String, password: String) = Result.Success("u1")
    override fun currentUser(): String? = null
    override fun signOut() {}
}

class LoginViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_success_sets_loggedIn() = runTest {
        val vm = LoginViewModel(FakeAuth())
        vm.setEmail("a@b.com")
        vm.setPassword("123456")
        vm.submit()
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals(true, s.loggedIn)
    }
}

