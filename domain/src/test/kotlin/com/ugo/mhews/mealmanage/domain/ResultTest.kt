package com.ugo.mhews.mealmanage.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {
    @Test fun success_holds_value() {
        val r: Result<Int> = Result.Success(42)
        assertTrue(r is Result.Success)
        assertEquals(42, (r as Result.Success).value)
    }

    @Test fun error_holds_domain_error() {
        val e = DomainError.Network("down", RuntimeException("x"))
        val r: Result<Nothing> = Result.Error(e)
        assertTrue(r is Result.Error)
        assertEquals("down", (r as Result.Error).error.message)
    }

    @Test fun domain_errors_construct() {
        listOf(
            DomainError.Auth("m", null),
            DomainError.PermissionDenied("m", null),
            DomainError.NotFound("m", null),
            DomainError.Network("m", null),
            DomainError.IndexRequired("m", null),
            DomainError.Unknown("m", null)
        ).forEach { err -> assertEquals("m", err.message) }
    }
}

