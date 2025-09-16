package com.ugo.mhews.mealmanage.data.source

import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealDataSource {
    suspend fun readUserMeal(userId: UserId, date: LocalDate): Meal
    suspend fun writeUserMeal(userId: UserId, date: LocalDate, count: Int)
    fun observeUserMeals(userId: UserId, start: LocalDate, endExclusive: LocalDate): Flow<Map<LocalDate, Int>>
    suspend fun readAllMealsForDate(date: LocalDate): List<UserMeal>
    suspend fun readTotalMeals(start: LocalDate, endExclusive: LocalDate): Int
    suspend fun readMealsByUser(start: LocalDate, endExclusive: LocalDate): Map<UserId, Int>
}
