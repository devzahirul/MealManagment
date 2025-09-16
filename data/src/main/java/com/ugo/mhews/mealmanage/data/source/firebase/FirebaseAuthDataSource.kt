package com.ugo.mhews.mealmanage.data.source.firebase

import com.google.firebase.auth.FirebaseAuth
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.domain.model.UserId
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) : AuthDataSource {
    override suspend fun signIn(email: String, password: String): UserId {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        return result.user?.uid ?: throw IllegalStateException("No user after sign in")
    }

    override suspend fun signUp(email: String, password: String): UserId {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return result.user?.uid ?: throw IllegalStateException("No user after sign up")
    }

    override fun currentUserId(): UserId? = auth.currentUser?.uid

    override fun signOut() {
        auth.signOut()
    }
}
