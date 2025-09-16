package com.ugo.mhews.mealmanage.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

/** Inclusive start, exclusive end range of [LocalDate] values. */
data class DateRange(
    val start: LocalDate,
    val endExclusive: LocalDate
) {
    init {
        require(!endExclusive.isBefore(start)) {
            "End date $endExclusive must be on or after start $start"
        }
    }

    fun toEpochMillis(zoneId: ZoneId): EpochMillisRange {
        val startInstant = start.atStartOfDay(zoneId).toInstant()
        val endInstant = endExclusive.atStartOfDay(zoneId).toInstant()
        return EpochMillisRange(startInstant.toEpochMilli(), endInstant.toEpochMilli())
    }
}

data class EpochMillisRange(val startInclusive: Long, val endExclusive: Long) {
    init {
        require(endExclusive >= startInclusive) {
            "End epoch must be >= start"
        }
    }
}

class MonthRangeCalculator {
    fun of(month: YearMonth): DateRange =
        DateRange(month.atDay(1), month.plusMonths(1).atDay(1))
}

