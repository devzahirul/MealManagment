package com.ugo.mhews.mealmanage.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.time.LocalDate

data class MealEntry(
    val date: LocalDate,
    val count: Int
)

class MealRepository(
    private val db: FirebaseFirestore = FirestoreProvider.db,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun dayDoc(uid: String, date: LocalDate) =
        db.collection("Meals").document(uid).collection("days").document(date.toString())

    fun getMealForDate(date: LocalDate, onResult: (MealEntry?, Throwable?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null, IllegalStateException("Not signed in"))
        dayDoc(user.uid, date).get()
            .addOnSuccessListener { snap ->
                val cnt = (snap.get("count") as? Number)?.toInt() ?: 0
                onResult(MealEntry(date, cnt), null)
            }
            .addOnFailureListener { ex -> onResult(null, ex) }
    }

    fun setMealForDate(date: LocalDate, count: Int, onComplete: (Boolean, Throwable?) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false, IllegalStateException("Not signed in"))
        val data = hashMapOf(
            "count" to count,
            "date" to date.toString(),
            "uid" to user.uid
        )
        dayDoc(user.uid, date).set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { ex -> onComplete(false, ex) }
    }

    data class MealUserCount(val uid: String, val count: Int)

    fun getAllMealsForDate(date: LocalDate, onResult: (List<MealUserCount>, Throwable?) -> Unit) {
        val dateStr = date.toString()
        db.collectionGroup("days")
            .whereEqualTo("date", dateStr)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { d ->
                    val uid = d.getString("uid") ?: return@mapNotNull null
                    val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                    MealUserCount(uid, cnt)
                }
                onResult(list, null)
            }
            .addOnFailureListener { ex -> onResult(emptyList(), ex) }
    }

    fun getTotalMealsForRange(
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        onResult: (Int, Throwable?) -> Unit
    ) {
        val startStr = startInclusive.toString()
        val endStr = endExclusive.toString()
        db.collectionGroup("days")
            .whereGreaterThanOrEqualTo("date", startStr)
            .whereLessThan("date", endStr)
            .get()
            .addOnSuccessListener { snap ->
                val total = snap.documents.sumOf { (it.get("count") as? Number)?.toInt() ?: 0 }
                onResult(total, null)
            }
            .addOnFailureListener { ex -> onResult(0, ex) }
    }

    fun getMealsByUserForRange(
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        onResult: (Map<String, Int>, Throwable?) -> Unit
    ) {
        val startStr = startInclusive.toString()
        val endStr = endExclusive.toString()
        db.collectionGroup("days")
            .whereGreaterThanOrEqualTo("date", startStr)
            .whereLessThan("date", endStr)
            .get()
            .addOnSuccessListener { snap ->
                val map = mutableMapOf<String, Int>()
                for (d in snap.documents) {
                    val uid = d.getString("uid") ?: continue
                    val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                    map[uid] = (map[uid] ?: 0) + cnt
                }
                onResult(map, null)
            }
            .addOnFailureListener { ex -> onResult(emptyMap(), ex) }
    }

    fun getMealsForUserRange(
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        onResult: (Map<LocalDate, Int>, Throwable?) -> Unit
    ) {
        val user = auth.currentUser ?: return onResult(emptyMap(), IllegalStateException("Not signed in"))
        val startStr = startInclusive.toString()
        val endStr = endExclusive.toString()
        db.collection("Meals").document(user.uid).collection("days")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), startStr)
            .whereLessThan(FieldPath.documentId(), endStr)
            .get()
            .addOnSuccessListener { snap ->
                val map = mutableMapOf<LocalDate, Int>()
                for (d in snap.documents) {
                    val dateStr = d.getString("date") ?: continue
                    val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                    try {
                        map[LocalDate.parse(dateStr)] = cnt
                    } catch (_: Throwable) {}
                }
                onResult(map, null)
            }
            .addOnFailureListener { ex -> onResult(emptyMap(), ex) }
    }

    fun observeMealsForUserRange(
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        onChange: (Map<LocalDate, Int>?, Throwable?) -> Unit
    ): ListenerRegistration? {
        val user = auth.currentUser ?: run {
            onChange(emptyMap(), IllegalStateException("Not signed in"))
            return null
        }
        val startStr = startInclusive.toString()
        val endStr = endExclusive.toString()
        return db.collection("Meals").document(user.uid).collection("days")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), startStr)
            .whereLessThan(FieldPath.documentId(), endStr)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onChange(null, err)
                } else {
                    val map = mutableMapOf<LocalDate, Int>()
                    if (snap != null) {
                        for (d in snap.documents) {
                            val dateStr = d.getString("date") ?: continue
                            val cnt = (d.get("count") as? Number)?.toInt() ?: 0
                            try { map[LocalDate.parse(dateStr)] = cnt } catch (_: Throwable) {}
                        }
                    }
                    onChange(map, null)
                }
            }
    }
}
