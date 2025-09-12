package com.ugo.mhews.mealmanage.data.common

import com.google.firebase.firestore.FirebaseFirestoreException
import com.ugo.mhews.mealmanage.domain.DomainError

internal fun Throwable.toDomainError(): DomainError = when (this) {
    is FirebaseFirestoreException -> when (this.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> DomainError.PermissionDenied(message, this)
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DomainError.Auth(message, this)
        FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.ABORTED -> DomainError.Network(message, this)
        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
            if ((message ?: "").contains("requires a", ignoreCase = true) && (message ?: "").contains("index", ignoreCase = true)) {
                DomainError.IndexRequired(message, this)
            } else DomainError.Unknown(message, this)
        }
        else -> DomainError.Unknown(message, this)
    }
    else -> DomainError.Unknown(message, this)
}

