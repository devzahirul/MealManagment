package com.ugo.mhews.mealmanage.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun signIn(email: String, password: String, onResult: (FirebaseUser?, Throwable?) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { res -> onResult(res.user, null) }
            .addOnFailureListener { ex -> onResult(null, ex) }
    }

    fun signUp(email: String, password: String, onResult: (FirebaseUser?, Throwable?) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { res -> onResult(res.user, null) }
            .addOnFailureListener { ex -> onResult(null, ex) }
    }

    fun signOut() { auth.signOut() }
}

