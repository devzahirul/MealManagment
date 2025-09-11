package com.ugo.mhews.mealmanage.data.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {
    private fun users() = db.collection("Users")

    override suspend fun getCurrentProfile(): Result<UserProfile> {
        val user = auth.currentUser ?: return Result.Error(DomainError.Auth("Not signed in"))
        return try {
            val doc = users().document(user.uid).get().await()
            val name = doc.getString("name") ?: ""
            val email = doc.getString("email") ?: (user.email ?: "")
            Result.Success(UserProfile(user.uid, name, email))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun updateCurrentName(name: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error(DomainError.Auth("Not signed in"))
        val data = hashMapOf(
            "name" to name,
            "email" to (user.email ?: "")
        )
        return try {
            users().document(user.uid).set(data).await()
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getNames(uids: Set<UserId>): Result<Map<UserId, String>> {
        if (uids.isEmpty()) return Result.Success(emptyMap())
        return try {
            val result = mutableMapOf<UserId, String>()
            val chunks = uids.toList().chunked(10)
            for (chunk in chunks) {
                val snap = users().whereIn(FieldPath.documentId(), chunk).get().await()
                for (doc in snap.documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: ""
                    result[uid] = name
                }
            }
            Result.Success(result)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }
}

