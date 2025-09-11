package com.ugo.mhews.mealmanage.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

class UserRepository(
    private val db: FirebaseFirestore = FirestoreProvider.db,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun users() = db.collection("Users")

    fun getCurrentProfile(onResult: (UserProfile?, Throwable?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null, IllegalStateException("Not signed in"))
        users().document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                val email = doc.getString("email") ?: (user.email ?: "")
                onResult(UserProfile(user.uid, name, email), null)
            }
            .addOnFailureListener { ex -> onResult(null, ex) }
    }

    fun updateCurrentName(name: String, onComplete: (Boolean, Throwable?) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false, IllegalStateException("Not signed in"))
        val data = hashMapOf(
            "name" to name,
            "email" to (user.email ?: "")
        )
        users().document(user.uid).set(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { ex -> onComplete(false, ex) }
    }

    fun getNames(uids: Set<String>, onResult: (Map<String, String>, Throwable?) -> Unit) {
        if (uids.isEmpty()) return onResult(emptyMap(), null)
        val chunks = uids.toList().chunked(10)
        val result = mutableMapOf<String, String>()
        var remaining = chunks.size
        var error: Throwable? = null
        for (chunk in chunks) {
            users().whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { snap ->
                    for (doc in snap.documents) {
                        val uid = doc.id
                        val name = doc.getString("name") ?: ""
                        result[uid] = name
                    }
                    remaining -= 1
                    if (remaining == 0) onResult(result, error)
                }
                .addOnFailureListener { ex ->
                    if (error == null) error = ex
                    remaining -= 1
                    if (remaining == 0) onResult(result, error)
                }
        }
    }
}
