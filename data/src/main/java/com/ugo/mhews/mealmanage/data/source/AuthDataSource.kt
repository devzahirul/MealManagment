package com.ugo.mhews.mealmanage.data.source

import com.ugo.mhews.mealmanage.domain.model.UserId

interface AuthDataSource {
    suspend fun signIn(email: String, password: String): UserId
    suspend fun signUp(email: String, password: String): UserId
    fun currentUserId(): UserId?
    fun signOut()
}
