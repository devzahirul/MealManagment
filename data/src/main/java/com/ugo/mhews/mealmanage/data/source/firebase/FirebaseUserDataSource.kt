package com.ugo.mhews.mealmanage.data.source.firebase

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.data.source.UserDataSource
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val USERS_COLLECTION = "Users"

class FirebaseUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataSource {
    override suspend fun loadProfile(userId: UserId): UserProfile? {
        val snapshot = firestore.collection(USERS_COLLECTION).document(userId).get().await()
        if (!snapshot.exists()) return null
        val name = snapshot.getString("name") ?: ""
        val email = snapshot.getString("email") ?: ""
        return UserProfile(uid = userId, name = name, email = email)
    }

    override suspend fun saveProfile(userId: UserId, profile: UserProfile) {
        val payload = hashMapOf(
            "name" to profile.name,
            "email" to profile.email
        )
        firestore.collection(USERS_COLLECTION).document(userId).set(payload).await()
    }

    override suspend fun loadNames(userIds: Set<UserId>): Map<UserId, String> {
        if (userIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<UserId, String>()
        for (chunk in userIds.chunked(10)) {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereIn(FieldPath.documentId(), chunk)
                .get().await()
            for (doc in snapshot.documents) {
                result[doc.id] = doc.getString("name") ?: ""
            }
        }
        return result
    }
}
