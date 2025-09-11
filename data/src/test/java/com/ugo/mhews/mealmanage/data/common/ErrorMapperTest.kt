package com.ugo.mhews.mealmanage.data.common

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMapperTest {
    @Test
    fun mapsIndexRequired() {
        val ex = FirebaseFirestoreException(
            "The query requires a index",
            FirebaseFirestoreException.Code.FAILED_PRECONDITION
        )
        val err = ex.toDomainError()
        assertTrue(err is com.ugo.mhews.mealmanage.domain.DomainError.IndexRequired)
    }

    @Test
    fun mapsUnknown() {
        val ex = IllegalStateException("oops")
        val err = ex.toDomainError()
        assertTrue(err is com.ugo.mhews.mealmanage.domain.DomainError.Unknown)
    }
}

