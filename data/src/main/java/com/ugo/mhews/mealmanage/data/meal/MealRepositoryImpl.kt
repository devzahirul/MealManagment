package com.ugo.mhews.mealmanage.data.meal

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject

/**
 * Placeholder implementation to be completed in Phase 2.
 * Not bound via Hilt yet; safe while UI still uses legacy repository.
 */
class MealRepositoryImpl @Inject constructor() : MealRepository {
    override suspend fun getMealForDate(date: LocalDate): Result<Meal> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))

    override suspend fun setMealForDate(date: LocalDate, count: Int): Result<Unit> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))

    override fun observeMealsForUserRange(
        start: LocalDate,
        end: LocalDate
    ): Flow<Result<Map<LocalDate, Int>>> = flowOf(
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))
    )

    override suspend fun getAllMealsForDate(date: LocalDate): Result<List<UserMeal>> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))

    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate): Result<Int> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))

    override suspend fun getMealsByUserForRange(
        start: LocalDate,
        end: LocalDate
    ): Result<Map<UserId, Int>> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("Not implemented"))
}

