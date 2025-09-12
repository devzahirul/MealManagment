package com.ugo.mhews.mealmanage.ui

import com.ugo.mhews.mealmanage.MainDispatcherRule
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeUsersRepo : UserRepository {
    override suspend fun getCurrentProfile(): Result<UserProfile> = Result.Success(UserProfile("u1", "Test", "t@example.com"))
    override suspend fun updateCurrentName(name: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getNames(uids: Set<String>): Result<Map<String, String>> = Result.Success(emptyMap())
}

private class FakeAuthRepo : AuthRepository {
    override suspend fun signIn(email: String, password: String) = Result.Success("u1")
    override suspend fun signUp(email: String, password: String) = Result.Success("u1")
    override fun currentUser(): String? = "u1"
    override fun signOut() {}
}

class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun load_and_save_and_signout() = runTest {
        val vm = ProfileViewModel(FakeUsersRepo(), FakeAuthRepo())
        vm.load()
        advanceUntilIdle()
        val s1 = vm.state.value
        assertEquals("Test", s1.name)

        vm.updateName("New")
        vm.save()
        advanceUntilIdle()
        val s2 = vm.state.value
        assertEquals(false, s2.saving)

        vm.signOut()
        val s3 = vm.state.value
        assertEquals(true, s3.signedOut)
    }
}

