package com.ugo.mhews.mealmanage.domain.usecase

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

private class FakeMealRepo2 : MealRepository {
    override suspend fun getMealForDate(date: LocalDate) = Result.Success(Meal(date, 1))
    override suspend fun setMealForDate(date: LocalDate, count: Int) = Result.Success(Unit)
    override fun observeMealsForUserRange(start: LocalDate, end: LocalDate): Flow<Result<Map<LocalDate, Int>>> =
        flowOf(Result.Success(mapOf(start to 2)))
    override suspend fun getAllMealsForDate(date: LocalDate) = Result.Success(listOf(UserMeal("u1", 3)))
    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate) = Result.Success(5)
    override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate) = Result.Success(mapOf("u1" to 4))
}

private class FakeCostRepo : CostRepository {
    override suspend fun addCost(entry: CostItem) = Result.Success(Unit)
    override suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: String?) = Result.Success(10.0)
    override suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long) = Result.Success(mapOf("u1" to 6.0))
    override suspend fun getCostsForUserRange(uid: String, startMs: Long, endMs: Long) = Result.Success(listOf(CostItem("Tea", 2.0, startMs)))
}

class UseCasesTest {
    private val mealRepo = FakeMealRepo2()
    private val costRepo = FakeCostRepo()

    @Test fun observeMealsForMonth_emits() = runBlocking {
        val ym = YearMonth.of(2024, 1)
        var count = 0
        ObserveMealsForMonth(mealRepo)(ym).collect { count++; if (count >= 1) return@collect }
        assertEquals(true, count >= 1)
    }

    @Test fun setMealForDate_calls_repo() = runBlocking {
        val res = SetMealForDate(mealRepo)(LocalDate.of(2024,1,2), 2)
        assertEquals(true, res is Result.Success)
    }

    @Test fun getMealForDate_calls_repo() = runBlocking {
        val res = GetMealForDate(mealRepo)(LocalDate.of(2024,1,3))
        assertEquals(1, (res as Result.Success).value.count)
    }

    @Test fun getAllMealsForDate_calls_repo() = runBlocking {
        val res = GetAllMealsForDate(mealRepo)(LocalDate.of(2024,1,4))
        assertEquals(1, (res as Result.Success).value.size)
    }

    @Test fun getTotalMealsForRange_calls_repo() = runBlocking {
        val res = GetTotalMealsForRange(mealRepo)(LocalDate.of(2024,1,1), LocalDate.of(2024,2,1))
        assertEquals(5, (res as Result.Success).value)
    }

    @Test fun getMealsByUserForRange_calls_repo() = runBlocking {
        val res = GetMealsByUserForRange(mealRepo)(LocalDate.of(2024,1,1), LocalDate.of(2024,2,1))
        assertEquals(4, (res as Result.Success).value["u1"])
    }

    @Test fun addCost_calls_repo() = runBlocking {
        val res = AddCost(costRepo)(CostItem("Tea", 1.0, 0))
        assertEquals(true, res is Result.Success)
    }

    @Test fun getTotalCostForRange_calls_repo() = runBlocking {
        val res = GetTotalCostForRange(costRepo)(0, 1_000)
        assertEquals(10.0, (res as Result.Success).value, 0.0)
    }

    @Test fun getTotalsByUserForRange_calls_repo() = runBlocking {
        val res = GetTotalsByUserForRange(costRepo)(0, 1_000)
        assertEquals(6.0, (res as Result.Success).value["u1"]!!, 0.0)
    }

    @Test fun getCostsForUserRange_calls_repo() = runBlocking {
        val res = GetCostsForUserRange(costRepo)("u1", 0, 1_000)
        assertEquals(1, (res as Result.Success).value.size)
    }
}
