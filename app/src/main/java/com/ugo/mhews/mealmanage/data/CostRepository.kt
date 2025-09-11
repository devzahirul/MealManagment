package com.ugo.mhews.mealmanage.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class CostEntry(
    val name: String,
    val cost: Double,
    val timestampMillis: Long
)

class CostRepository(private val db: FirebaseFirestore = FirestoreProvider.db) {
    fun addCost(entry: CostEntry, onComplete: (Boolean, Throwable?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        fun write(withDisplayName: String) {
            val data = hashMapOf(
                "name" to entry.name,
                "cost" to entry.cost,
                "timestamp" to entry.timestampMillis,
                "uid" to (user?.uid ?: ""),
                "email" to (user?.email ?: ""),
                "user_display_name" to withDisplayName
            )
            db.collection("AddCost")
                .add(data)
                .addOnSuccessListener { onComplete(true, null) }
                .addOnFailureListener { ex -> onComplete(false, ex) }
        }

        if (user == null) {
            write("")
        } else {
            // Try to read profile name from Users/{uid}, fall back to FirebaseAuth values
            db.collection("Users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val profileName = doc.getString("name")
                        ?: user.displayName
                        ?: user.email
                        ?: ""
                    write(profileName)
                }
                .addOnFailureListener {
                    val fallback = user.displayName ?: user.email ?: ""
                    write(fallback)
                }
        }
    }

    fun getTotalCostForRange(
        startInclusiveMillis: Long,
        endExclusiveMillis: Long,
        uid: String? = null,
        onResult: (Double, Throwable?) -> Unit
    ) {
        var query = db.collection("AddCost")
            .whereGreaterThanOrEqualTo("timestamp", startInclusiveMillis)
            .whereLessThan("timestamp", endExclusiveMillis)

        if (!uid.isNullOrEmpty()) {
            query = query.whereEqualTo("uid", uid)
        }

        query.get()
            .addOnSuccessListener { snap ->
                var sum = 0.0
                for (doc in snap.documents) {
                    val c = (doc.get("cost") as? Number)?.toDouble() ?: 0.0
                    sum += c
                }
                onResult(sum, null)
            }
            .addOnFailureListener { ex -> onResult(0.0, ex) }
    }

    fun getTotalsByUserForRange(
        startInclusiveMillis: Long,
        endExclusiveMillis: Long,
        onResult: (Map<String, Double>, Throwable?) -> Unit
    ) {
        db.collection("AddCost")
            .whereGreaterThanOrEqualTo("timestamp", startInclusiveMillis)
            .whereLessThan("timestamp", endExclusiveMillis)
            .get()
            .addOnSuccessListener { snap ->
                val sums = mutableMapOf<String, Double>()
                for (doc in snap.documents) {
                    val uid = doc.getString("uid") ?: ""
                    val c = (doc.get("cost") as? Number)?.toDouble() ?: 0.0
                    if (uid.isNotEmpty()) {
                        sums[uid] = (sums[uid] ?: 0.0) + c
                    }
                }
                onResult(sums, null)
            }
            .addOnFailureListener { ex -> onResult(emptyMap(), ex) }
    }

    fun getCostsForUserRange(
        uid: String,
        startInclusiveMillis: Long,
        endExclusiveMillis: Long,
        onResult: (List<CostEntry>, Throwable?) -> Unit
    ) {
        db.collection("AddCost")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("timestamp", startInclusiveMillis)
            .whereLessThan("timestamp", endExclusiveMillis)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    CostEntry(
                        name = d.getString("name") ?: "",
                        cost = (d.get("cost") as? Number)?.toDouble() ?: 0.0,
                        timestampMillis = (d.get("timestamp") as? Number)?.toLong() ?: 0L
                    )
                }
                onResult(list, null)
            }
            .addOnFailureListener { ex -> onResult(emptyList(), ex) }
    }
}
