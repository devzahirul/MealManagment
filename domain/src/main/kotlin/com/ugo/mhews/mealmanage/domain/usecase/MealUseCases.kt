package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class ObserveMealsForMonth(
    private val repo: MealRepository
) {
    operator fun invoke(month: YearMonth): Flow<Result<Map<LocalDate, Int>>> {
        val start = month.atDay(1)
        val end = month.plusMonths(1).atDay(1)
        return repo.observeMealsForUserRange(start, end)
    }
}

class SetMealForDate(
    private val repo: MealRepository
) {
    suspend operator fun invoke(date: LocalDate, count: Int): Result<Unit> = repo.setMealForDate(date, count)
}

