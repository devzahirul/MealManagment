package com.ugo.mhews.mealmanage.data.source.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.data.source.CostDataSource
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COST_COLLECTION = "AddCost"

class FirebaseCostDataSource @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CostDataSource {
    override suspend fun addCost(userId: UserId?, item: CostItem) {
        val user = auth.currentUser
        val payload = hashMapOf(
            "name" to item.name,
            "cost" to item.cost,
            "timestamp" to item.timestampMillis,
            "uid" to (userId ?: user?.uid ?: ""),
            "email" to (user?.email ?: ""),
            "user_display_name" to (user?.displayName ?: user?.email ?: "")
        )
        db.collection(COST_COLLECTION).add(payload).await()
    }

    override suspend fun totalCost(startMs: Long, endMs: Long, userId: UserId?): Double {
        var query = db.collection(COST_COLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startMs)
            .whereLessThan("timestamp", endMs)
        if (!userId.isNullOrEmpty()) {
            query = query.whereEqualTo("uid", userId)
        }
        val snapshot = query.get().await()
        return snapshot.documents.sumOf { (it.get("cost") as? Number)?.toDouble() ?: 0.0 }
    }

    override suspend fun totalsByUser(startMs: Long, endMs: Long): Map<UserId, Double> {
        val snapshot = db.collection(COST_COLLECTION)
            .whereGreaterThanOrEqualTo("timestamp", startMs)
            .whereLessThan("timestamp", endMs)
            .get().await()
        val results = mutableMapOf<UserId, Double>()
        for (doc in snapshot.documents) {
            val uid = doc.getString("uid") ?: continue
            val cost = (doc.get("cost") as? Number)?.toDouble() ?: 0.0
            results[uid] = (results[uid] ?: 0.0) + cost
        }
        return results
    }

    override suspend fun costsForUser(userId: UserId, startMs: Long, endMs: Long): List<CostItem> {
        val snapshot = db.collection(COST_COLLECTION)
            .whereEqualTo("uid", userId)
            .whereGreaterThanOrEqualTo("timestamp", startMs)
            .whereLessThan("timestamp", endMs)
            .get().await()
        return snapshot.documents.map { doc ->
            CostItem(
                name = doc.getString("name") ?: "",
                cost = (doc.get("cost") as? Number)?.toDouble() ?: 0.0,
                timestampMillis = (doc.get("timestamp") as? Number)?.toLong() ?: 0L
            )
        }
    }
}
