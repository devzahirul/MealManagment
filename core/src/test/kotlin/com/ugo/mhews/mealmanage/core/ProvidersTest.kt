package com.ugo.mhews.mealmanage.core

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ProvidersTest {
    @Test fun defaultDispatchers_match() {
        assertEquals(Dispatchers.IO, DefaultDispatchersProvider.io)
        assertEquals(Dispatchers.Default, DefaultDispatchersProvider.default)
        assertEquals(Dispatchers.Main, DefaultDispatchersProvider.main)
    }

    @Test fun systemDateProvider_with_fixed_clock() {
        val zone: ZoneId = ZoneOffset.UTC
        val fixed = Clock.fixed(LocalDate.of(2024, 1, 15).atStartOfDay(zone).toInstant(), zone)
        val dp = SystemDateProvider(fixed)
        assertEquals(LocalDate.of(2024, 1, 15), dp.today(zone))
    }
}

