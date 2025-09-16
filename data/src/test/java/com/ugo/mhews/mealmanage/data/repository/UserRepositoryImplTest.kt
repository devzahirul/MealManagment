package com.ugo.mhews.mealmanage.data.repository

import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.UserDataSource
import com.ugo.mhews.mealmanage.data.user.UserRepositoryImpl
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class AuthDataSourceStub(var userId: UserId? = "user") : AuthDataSource {
    override suspend fun signIn(email: String, password: String): UserId = throw UnsupportedOperationException()
    override suspend fun signUp(email: String, password: String): UserId = throw UnsupportedOperationException()
    override fun currentUserId(): UserId? = userId
    override fun signOut() = Unit
}

private class UserDataSourceStub : UserDataSource {
    var storedProfile: UserProfile? = null
    var storedNames: Map<UserId, String> = emptyMap()
    var throwOnLoad: Throwable? = null

    override suspend fun loadProfile(userId: UserId): UserProfile? {
        throwOnLoad?.let { throw it }
        return storedProfile
    }

    override suspend fun saveProfile(userId: UserId, profile: UserProfile) {
        storedProfile = profile
    }

    override suspend fun loadNames(userIds: Set<UserId>): Map<UserId, String> {
        throwOnLoad?.let { throw it }
        return storedNames
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun `getCurrentProfile returns stored profile`() = scope.runTest {
        val users = UserDataSourceStub().apply { storedProfile = UserProfile("user", "Name", "email") }
        val repo = UserRepositoryImpl(AuthDataSourceStub(), users, dispatcher)

        val result = repo.getCurrentProfile()
        assertTrue(result is Result.Success)
        assertEquals("Name", (result as Result.Success).value.name)
    }

    @Test
    fun `getCurrentProfile returns empty profile when none stored`() = scope.runTest {
        val users = UserDataSourceStub()
        val repo = UserRepositoryImpl(AuthDataSourceStub(), users, dispatcher)

        val result = repo.getCurrentProfile()
        assertTrue(result is Result.Success)
        assertEquals("user", (result as Result.Success).value.uid)
    }

    @Test
    fun `getCurrentProfile returns auth error when no user`() = scope.runTest {
        val repo = UserRepositoryImpl(AuthDataSourceStub(userId = null), UserDataSourceStub(), dispatcher)

        val result = repo.getCurrentProfile()
        assertTrue(result is Result.Error && result.error is DomainError.Auth)
    }

    @Test
    fun `updateCurrentName saves profile`() = scope.runTest {
        val users = UserDataSourceStub().apply { storedProfile = UserProfile("user", "Old", "email") }
        val repo = UserRepositoryImpl(AuthDataSourceStub(), users, dispatcher)

        val result = repo.updateCurrentName("New")
        assertTrue(result is Result.Success)
        assertEquals("New", users.storedProfile?.name)
    }

    @Test
    fun `getNames delegates to data source`() = scope.runTest {
        val users = UserDataSourceStub().apply { storedNames = mapOf("user" to "Display") }
        val repo = UserRepositoryImpl(AuthDataSourceStub(), users, dispatcher)

        val result = repo.getNames(setOf("user"))
        assertTrue(result is Result.Success)
        assertEquals("Display", (result as Result.Success).value["user"])
    }

    @Test
    fun `errors map to DomainError`() = scope.runTest {
        val users = UserDataSourceStub().apply { throwOnLoad = IllegalStateException("boom") }
        val repo = UserRepositoryImpl(AuthDataSourceStub(), users, dispatcher)

        val profile = repo.getCurrentProfile()
        assertTrue(profile is Result.Error && profile.error is DomainError.Unknown)

        val names = repo.getNames(setOf("user"))
        assertTrue(names is Result.Error && names.error is DomainError.Unknown)
    }
}
