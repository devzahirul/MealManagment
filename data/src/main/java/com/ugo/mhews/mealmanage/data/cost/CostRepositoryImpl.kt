package com.ugo.mhews.mealmanage.data.cost

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CostRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CostRepository {
    override suspend fun addCost(entry: CostItem): Result<Unit> = withContext(ioDispatcher) {
        val user = auth.currentUser
        val data = hashMapOf(
            "name" to entry.name,
            "cost" to entry.cost,
            "timestamp" to entry.timestampMillis,
            "uid" to (user?.uid ?: ""),
            "email" to (user?.email ?: ""),
            "user_display_name" to (user?.displayName ?: user?.email ?: "")
        )
        try {
            db.collection("AddCost").add(data).await()
            Result.Success(Unit)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: UserId?): Result<Double> = withContext(ioDispatcher) {
        try {
            var query = db.collection("AddCost")
                .whereGreaterThanOrEqualTo("timestamp", startMs)
                .whereLessThan("timestamp", endMs)
            if (!uid.isNullOrEmpty()) query = query.whereEqualTo("uid", uid)
            val snap = query.get().await()
            val sum = snap.documents.sumOf { (it.get("cost") as? Number)?.toDouble() ?: 0.0 }
            Result.Success(sum)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long): Result<Map<UserId, Double>> = withContext(ioDispatcher) {
        try {
            val snap = db.collection("AddCost")
                .whereGreaterThanOrEqualTo("timestamp", startMs)
                .whereLessThan("timestamp", endMs)
                .get().await()
            val sums = mutableMapOf<UserId, Double>()
            for (doc in snap.documents) {
                val uid = doc.getString("uid") ?: ""
                val c = (doc.get("cost") as? Number)?.toDouble() ?: 0.0
                if (uid.isNotEmpty()) {
                    sums[uid] = (sums[uid] ?: 0.0) + c
                }
            }
            Result.Success(sums)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }

    override suspend fun getCostsForUserRange(uid: UserId, startMs: Long, endMs: Long): Result<List<CostItem>> = withContext(ioDispatcher) {
        try {
            val snap = db.collection("AddCost")
                .whereEqualTo("uid", uid)
                .whereGreaterThanOrEqualTo("timestamp", startMs)
                .whereLessThan("timestamp", endMs)
                .get().await()
            val list = snap.documents.map { d ->
                CostItem(
                    name = d.getString("name") ?: "",
                    cost = (d.get("cost") as? Number)?.toDouble() ?: 0.0,
                    timestampMillis = (d.get("timestamp") as? Number)?.toLong() ?: 0L
                )
            }
            Result.Success(list)
        } catch (t: Throwable) { Result.Error(t.toDomainError()) }
    }
}
