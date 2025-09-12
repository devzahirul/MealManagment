package com.ugo.mhews.mealmanage.data.meal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MealRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MealRepository {

    private fun dayDoc(uid: String, date: LocalDate) =
        db.collection("Meals").document(uid).collection("days").document(date.toString())

    override suspend fun getMealForDate(date: LocalDate): Result<Meal> = withContext(ioDispatcher) {
        val user = auth.currentUser ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        try {
            val snap = dayDoc(user.uid, date).get().await()
            val cnt = (snap.get("count") as? Number)?.toInt() ?: 0
            Result.Success(Meal(date, cnt))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun setMealForDate(date: LocalDate, count: Int): Result<Unit> = withContext(ioDispatcher) {
        val user = auth.currentUser ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        val data = hashMapOf(
            "count" to count,
            "date" to date.toString(),
            "uid" to user.uid
        )
        try {
            dayDoc(user.uid, date).set(data).await()
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override fun observeMealsForUserRange(
        start: LocalDate,
        end: LocalDate
    ): Flow<Result<Map<LocalDate, Int>>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(Result.Error(DomainError.Auth("Not signed in")))
            close()
            return@callbackFlow
        }
        val startStr = start.toString()
        val endStr = end.toString()
        val reg = db.collection("Meals").document(user.uid).collection("days")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), startStr)
            .whereLessThan(FieldPath.documentId(), endStr)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Result.Error(err.toDomainError()))
                } else {
                    val map = mutableMapOf<LocalDate, Int>()
                    if (snap != null) {
                        for (d in snap.documents) {
                            val dateStr = d.getString("date") ?: continue
                            val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                            try { map[LocalDate.parse(dateStr)] = cnt } catch (_: Throwable) {}
                        }
                    }
                    trySend(Result.Success(map))
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getAllMealsForDate(date: LocalDate): Result<List<UserMeal>> = withContext(ioDispatcher) {
        val dateStr = date.toString()
        try {
            val snap = db.collectionGroup("days")
                .whereEqualTo("date", dateStr)
                .get().await()
            val list = snap.documents.mapNotNull { d ->
                val uid = d.getString("uid") ?: return@mapNotNull null
                val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                UserMeal(uid, cnt)
            }
            Result.Success(list)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate): Result<Int> {
        val startStr = start.toString()
        val endStr = end.toString()
        return try {
            val snap = db.collectionGroup("days")
                .whereGreaterThanOrEqualTo("date", startStr)
                .whereLessThan("date", endStr)
                .get().await()
            val total = snap.documents.sumOf { (it.get("count") as? Number)?.toInt() ?: 0 }
            Result.Success(total)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate): Result<Map<UserId, Int>> {
        val startStr = start.toString()
        val endStr = end.toString()
        return try {
            val snap = db.collectionGroup("days")
                .whereGreaterThanOrEqualTo("date", startStr)
                .whereLessThan("date", endStr)
                .get().await()
            val map = mutableMapOf<UserId, Int>()
            for (d in snap.documents) {
                val uid = d.getString("uid") ?: continue
                val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                map[uid] = (map[uid] ?: 0) + cnt
            }
            Result.Success(map)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }
}
