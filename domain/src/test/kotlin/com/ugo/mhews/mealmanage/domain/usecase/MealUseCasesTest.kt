package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

private class FakeMealRepo : MealRepository {
    var next: Result<Unit> = Result.Success(Unit)
    override suspend fun getMealForDate(date: LocalDate) = Result.Success(Meal(date, 2))
    override suspend fun setMealForDate(date: LocalDate, count: Int) = next
    override fun observeMealsForUserRange(start: LocalDate, end: LocalDate): Flow<Result<Map<LocalDate, Int>>> =
        flowOf(Result.Success(mapOf(start to 1)))
    override suspend fun getAllMealsForDate(date: LocalDate) = Result.Success(emptyList<com.ugo.mhews.mealmanage.domain.model.UserMeal>())
    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate) = Result.Success(0)
    override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate) = Result.Success(emptyMap<String, Int>())
}

@OptIn(ExperimentalCoroutinesApi::class)
class MealUseCasesTest {
    @Test
    fun setMealForDate_success() = runTest {
        val repo = FakeMealRepo().apply { next = Result.Success(Unit) }
        val uc = SetMealForDate(repo)
        val res = uc(LocalDate.parse("2025-09-11"), 3)
        assert(res is Result.Success)
    }

    @Test
    fun observeMealsForMonth_emits() = runTest {
        val repo = FakeMealRepo()
        val uc = ObserveMealsForMonth(repo)
        val month = java.time.YearMonth.of(2025, 9)
        val first = uc(month)
        val result = first.first()
        assert(result is Result.Success)
        val map = (result as Result.Success).value
        assertEquals(1, map.size)
    }
}
