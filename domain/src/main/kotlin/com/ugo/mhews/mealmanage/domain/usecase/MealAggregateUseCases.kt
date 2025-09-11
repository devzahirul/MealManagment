package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import java.time.LocalDate

class GetTotalMealsForRange(
    private val repo: MealRepository
) {
    suspend operator fun invoke(start: LocalDate, end: LocalDate) = repo.getTotalMealsForRange(start, end)
}

class GetMealsByUserForRange(
    private val repo: MealRepository
) {
    suspend operator fun invoke(start: LocalDate, end: LocalDate) = repo.getMealsByUserForRange(start, end)
}

