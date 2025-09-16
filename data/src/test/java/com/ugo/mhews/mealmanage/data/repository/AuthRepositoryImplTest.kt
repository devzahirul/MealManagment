package com.ugo.mhews.mealmanage.data.repository

import com.ugo.mhews.mealmanage.data.auth.AuthRepositoryImpl
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAuthDataSource : AuthDataSource {
    var currentUser: UserId? = "user"
    var lastEmail: String? = null
    var lastPassword: String? = null
    var signOutCalled = false
    var throwOnSignIn: Throwable? = null
    var throwOnSignUp: Throwable? = null

    override suspend fun signIn(email: String, password: String): UserId {
        throwOnSignIn?.let { throw it }
        lastEmail = email
        lastPassword = password
        return "signed-in"
    }

    override suspend fun signUp(email: String, password: String): UserId {
        throwOnSignUp?.let { throw it }
        lastEmail = email
        lastPassword = password
        return "signed-up"
    }

    override fun currentUserId(): UserId? = currentUser

    override fun signOut() {
        signOutCalled = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun `signIn delegates and wraps success`() = scope.runTest {
        val fake = FakeAuthDataSource()
        val repo = AuthRepositoryImpl(fake, dispatcher)

        val result = repo.signIn("mail@example.com", "secret")
        assertTrue(result is Result.Success)
        assertEquals("signed-in", (result as Result.Success).value)
        assertEquals("mail@example.com", fake.lastEmail)
        assertEquals("secret", fake.lastPassword)
    }

    @Test
    fun `signIn maps exceptions to DomainError`() = scope.runTest {
        val fake = FakeAuthDataSource().apply { throwOnSignIn = IllegalStateException("boom") }
        val repo = AuthRepositoryImpl(fake, dispatcher)

        val result = repo.signIn("bad", "pass")
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).error is DomainError.Unknown)
    }

    @Test
    fun `signUp delegates and handles failure`() = scope.runTest {
        val fake = FakeAuthDataSource()
        val repo = AuthRepositoryImpl(fake, dispatcher)

        val success = repo.signUp("mail@example.com", "secret")
        assertTrue(success is Result.Success)

        fake.throwOnSignUp = IllegalArgumentException("bad")
        val failure = repo.signUp("mail@example.com", "secret")
        assertTrue(failure is Result.Error)
    }

    @Test
    fun `current user and sign out use data source`() {
        val fake = FakeAuthDataSource()
        val repo = AuthRepositoryImpl(fake, dispatcher)

        assertEquals("user", repo.currentUser())
        repo.signOut()
        assertTrue(fake.signOutCalled)
    }
}
