package com.ugo.mhews.mealmanage.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<UserId> {
        return try {
            val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = res.user?.uid ?: return Result.Error(DomainError.Auth("No user after sign in"))
            Result.Success(uid)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun signUp(email: String, password: String): Result<UserId> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = res.user?.uid ?: return Result.Error(DomainError.Auth("No user after sign up"))
            Result.Success(uid)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override fun currentUser(): UserId? = auth.currentUser?.uid

    override fun signOut() { auth.signOut() }
}

