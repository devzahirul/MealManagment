package com.ugo.mhews.mealmanage.data.auth

import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: AuthDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<UserId> = withContext(ioDispatcher) {
        try {
            Result.Success(auth.signIn(email, password))
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun signUp(email: String, password: String): Result<UserId> = withContext(ioDispatcher) {
        try {
            Result.Success(auth.signUp(email, password))
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override fun currentUser(): UserId? = auth.currentUserId()

    override fun signOut() { auth.signOut() }
}
