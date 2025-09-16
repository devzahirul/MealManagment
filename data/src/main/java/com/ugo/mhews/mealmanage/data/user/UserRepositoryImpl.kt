package com.ugo.mhews.mealmanage.data.user

import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.UserDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: AuthDataSource,
    private val users: UserDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    override suspend fun getCurrentProfile(): Result<UserProfile> = withContext(ioDispatcher) {
        val userId = auth.currentUserId() ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        try {
            val stored = users.loadProfile(userId)
            if (stored != null) {
                Result.Success(stored)
            } else {
                Result.Success(UserProfile(userId, name = "", email = ""))
            }
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun updateCurrentName(name: String): Result<Unit> = withContext(ioDispatcher) {
        val userId = auth.currentUserId() ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        try {
            val existing = users.loadProfile(userId) ?: UserProfile(userId, name = name, email = "")
            users.saveProfile(userId, existing.copy(name = name))
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getNames(uids: Set<UserId>): Result<Map<UserId, String>> = withContext(ioDispatcher) {
        if (uids.isEmpty()) return@withContext Result.Success(emptyMap())
        try {
            Result.Success(users.loadNames(uids))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }
}
