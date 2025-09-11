package com.ugo.mhews.mealmanage.domain.repository

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealRepository {
    suspend fun getMealForDate(date: LocalDate): Result<Meal>
    suspend fun setMealForDate(date: LocalDate, count: Int): Result<Unit>
    fun observeMealsForUserRange(start: LocalDate, end: LocalDate): Flow<Result<Map<LocalDate, Int>>>
    suspend fun getAllMealsForDate(date: LocalDate): Result<List<UserMeal>>
    suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate): Result<Int>
    suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate): Result<Map<UserId, Int>>
}

interface CostRepository {
    suspend fun addCost(entry: CostItem): Result<Unit>
    suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: UserId? = null): Result<Double>
    suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long): Result<Map<UserId, Double>>
    suspend fun getCostsForUserRange(uid: UserId, startMs: Long, endMs: Long): Result<List<CostItem>>
}

interface UserRepository {
    suspend fun getCurrentProfile(): Result<UserProfile>
    suspend fun updateCurrentName(name: String): Result<Unit>
    suspend fun getNames(uids: Set<UserId>): Result<Map<UserId, String>>
}

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<UserId>
    suspend fun signUp(email: String, password: String): Result<UserId>
    fun currentUser(): UserId?
    fun signOut()
}

