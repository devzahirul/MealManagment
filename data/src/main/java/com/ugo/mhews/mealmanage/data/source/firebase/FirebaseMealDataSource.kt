package com.ugo.mhews.mealmanage.data.source.firebase

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.data.source.MealDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

private const val MEALS_COLLECTION = "Meals"
private const val DAYS_COLLECTION = "days"

class FirebaseMealDataSource @Inject constructor(
    private val db: FirebaseFirestore
) : MealDataSource {
    private fun dayDoc(uid: UserId, date: LocalDate) =
        db.collection(MEALS_COLLECTION).document(uid).collection(DAYS_COLLECTION).document(date.toString())

    override suspend fun readUserMeal(userId: UserId, date: LocalDate): Meal {
        val snapshot = dayDoc(userId, date).get().await()
        val count = (snapshot.get("count") as? Number)?.toInt() ?: 0
        return Meal(date, count)
    }

    override suspend fun writeUserMeal(userId: UserId, date: LocalDate, count: Int) {
        val payload = hashMapOf(
            "count" to count,
            "date" to date.toString(),
            "uid" to userId
        )
        dayDoc(userId, date).set(payload).await()
    }

    override fun observeUserMeals(userId: UserId, start: LocalDate, endExclusive: LocalDate): Flow<Map<LocalDate, Int>> = callbackFlow {
        val registration = db.collection(MEALS_COLLECTION).document(userId).collection(DAYS_COLLECTION)
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), start.toString())
            .whereLessThan(FieldPath.documentId(), endExclusive.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val map = mutableMapOf<LocalDate, Int>()
                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val dateStr = doc.getString("date") ?: continue
                            val count = (doc.get("count") as? Number)?.toInt() ?: 0
                            try {
                                map[LocalDate.parse(dateStr)] = count
                            } catch (_: DateTimeParseException) {
                                // Ignore malformed dates to keep stream alive
                            }
                        }
                    }
                    trySend(map)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun readAllMealsForDate(date: LocalDate): List<UserMeal> {
        val snapshot = db.collectionGroup(DAYS_COLLECTION)
            .whereEqualTo("date", date.toString())
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            val uid = doc.getString("uid") ?: return@mapNotNull null
            val count = (doc.get("count") as? Number)?.toInt() ?: 0
            UserMeal(uid, count)
        }
    }

    override suspend fun readTotalMeals(start: LocalDate, endExclusive: LocalDate): Int {
        val snapshot = db.collectionGroup(DAYS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", start.toString())
            .whereLessThan("date", endExclusive.toString())
            .get().await()
        return snapshot.documents.sumOf { (it.get("count") as? Number)?.toInt() ?: 0 }
    }

    override suspend fun readMealsByUser(start: LocalDate, endExclusive: LocalDate): Map<UserId, Int> {
        val snapshot = db.collectionGroup(DAYS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", start.toString())
            .whereLessThan("date", endExclusive.toString())
            .get().await()
        val results = mutableMapOf<UserId, Int>()
        for (doc in snapshot.documents) {
            val uid = doc.getString("uid") ?: continue
            val count = (doc.get("count") as? Number)?.toInt() ?: 0
            results[uid] = (results[uid] ?: 0) + count
        }
        return results
    }
}
