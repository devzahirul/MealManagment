package com.ugo.mhews.mealmanage.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirestoreProvider {
    val db: FirebaseFirestore by lazy { Firebase.firestore }
}

