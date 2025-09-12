package com.ugo.mhews.mealmanage.data.common

import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMapperTest {
    @Test
    fun mapsUnknown() {
        val ex = IllegalStateException("oops")
        val err = ex.toDomainError()
        assertTrue(err is com.ugo.mhews.mealmanage.domain.DomainError.Unknown)
    }
}
