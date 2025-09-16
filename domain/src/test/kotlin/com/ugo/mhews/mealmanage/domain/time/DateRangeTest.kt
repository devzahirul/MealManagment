package com.ugo.mhews.mealmanage.domain.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class DateRangeTest {
    @Test
    fun `validates end after start`() {
        assertThrows(IllegalArgumentException::class.java) {
            DateRange(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 1))
        }
    }

    @Test
    fun `converts to epoch millis`() {
        val zone = ZoneId.of("UTC")
        val range = DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3))
        val epoch = range.toEpochMillis(zone)
        assertEquals(1704067200000L, epoch.startInclusive)
        assertEquals(1704240000000L, epoch.endExclusive)
    }

    @Test
    fun `month range calculator spans month`() {
        val ym = YearMonth.of(2024, 2)
        val range = MonthRangeCalculator().of(ym)
        assertEquals(LocalDate.of(2024, 2, 1), range.start)
        assertEquals(LocalDate.of(2024, 3, 1), range.endExclusive)
    }
}
