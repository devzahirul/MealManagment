package com.ugo.mhews.mealmanage.data.meal

import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import com.ugo.mhews.mealmanage.data.common.toDomainError
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.MealDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val auth: AuthDataSource,
    private val mealDataSource: MealDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MealRepository {

    private fun currentUserId(): UserId? = auth.currentUserId()

    override suspend fun getMealForDate(date: LocalDate): Result<Meal> = withContext(ioDispatcher) {
        val uid = currentUserId() ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        try {
            Result.Success(mealDataSource.readUserMeal(uid, date))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun setMealForDate(date: LocalDate, count: Int): Result<Unit> = withContext(ioDispatcher) {
        val uid = currentUserId() ?: return@withContext Result.Error(DomainError.Auth("Not signed in"))
        try {
            mealDataSource.writeUserMeal(uid, date, count)
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override fun observeMealsForUserRange(
        start: LocalDate,
        end: LocalDate
    ): Flow<Result<Map<LocalDate, Int>>> {
        val uid = currentUserId() ?: return flowOf(Result.Error(DomainError.Auth("Not signed in")))
        return mealDataSource.observeUserMeals(uid, start, end)
            .map<Map<LocalDate, Int>, Result<Map<LocalDate, Int>>> { data -> Result.Success(data) }
            .catch { e -> emit(Result.Error(e.toDomainError())) }
    }

    override suspend fun getAllMealsForDate(date: LocalDate): Result<List<UserMeal>> = withContext(ioDispatcher) {
        try {
            Result.Success(mealDataSource.readAllMealsForDate(date))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate): Result<Int> = withContext(ioDispatcher) {
        try {
            Result.Success(mealDataSource.readTotalMeals(start, end))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }

    override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate): Result<Map<UserId, Int>> = withContext(ioDispatcher) {
        try {
            Result.Success(mealDataSource.readMealsByUser(start, end))
        } catch (t: Throwable) {
            Result.Error(t.toDomainError())
        }
    }
}
