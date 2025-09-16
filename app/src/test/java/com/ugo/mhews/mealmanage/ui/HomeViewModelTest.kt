package com.ugo.mhews.mealmanage.ui

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.core.DateProvider
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetCostsForUserRange
import com.ugo.mhews.mealmanage.domain.usecase.GetMealsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalCostForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalMealsForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetUserNames
import com.ugo.mhews.mealmanage.domain.time.MonthRangeCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import com.ugo.mhews.mealmanage.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

import com.ugo.mhews.mealmanage.domain.model.UserProfile
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserMeal


private class FakeUsers {
    suspend fun names(uids: Set<String>): Result<Map<String, String>> =
        Result.Success(uids.associateWith { "User ${it.take(6)}" })
}

private class FakeMeals : MealRepository {
    override suspend fun getMealForDate(date: LocalDate): Result<Meal> =
        Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("NA"))
    override suspend fun setMealForDate(date: LocalDate, count: Int): Result<Unit> = Result.Success(Unit)
    override fun observeMealsForUserRange(start: LocalDate, end: LocalDate): Flow<Result<Map<LocalDate, Int>>> =
        flowOf(Result.Success(emptyMap()))
    override suspend fun getAllMealsForDate(date: LocalDate): Result<List<UserMeal>> =
        Result.Success(listOf(UserMeal("u1", 3)))
    override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate): Result<Int> = Result.Success(10)
    override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate): Result<Map<String, Int>> =
        Result.Success(mapOf("u1" to 5))
}

private class FakeCosts : CostRepository {
    override suspend fun addCost(entry: CostItem): Result<Unit> = Result.Success(Unit)
    override suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: String?): Result<Double> = Result.Success(100.0)
    override suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long): Result<Map<String, Double>> = Result.Success(mapOf("u1" to 60.0))
    override suspend fun getCostsForUserRange(uid: String, startMs: Long, endMs: Long): Result<List<CostItem>> = Result.Success(listOf(CostItem("Rice", 20.0, startMs)))
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun initialLoads_populateState() = runTest {
        val fakeUsers = FakeUsers()
        val vm = HomeViewModel(
            getTotalCostForRange = GetTotalCostForRange(FakeCosts()),
            getTotalMealsForRange = GetTotalMealsForRange(FakeMeals()),
            getTotalsByUserForRange = GetTotalsByUserForRange(FakeCosts()),
            getMealsByUserForRange = GetMealsByUserForRange(FakeMeals()),
            getCostsForUserRange = GetCostsForUserRange(FakeCosts()),
            getAllMealsForDate = GetAllMealsForDate(FakeMeals()),
            getUserNames = GetUserNames(object : com.ugo.mhews.mealmanage.domain.repository.UserRepository {
                override suspend fun getCurrentProfile(): Result<UserProfile> = Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("NA"))
                override suspend fun updateCurrentName(name: String): Result<Unit> = Result.Success(Unit)
                override suspend fun getNames(uids: Set<String>) = fakeUsers.names(uids)
            }),
            dateProvider = object : DateProvider {
                override fun today(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)
            },
            monthRangeCalculator = MonthRangeCalculator()
        )
        // Allow init to run; state should have computed values
        vm.refreshAll(); vm.loadByUser(); vm.loadTodayMeals()
        advanceUntilIdle()
        val s = vm.state.value
        // Validate some computed state from fakes
        assertEquals(false, s.monthLoading)
        assertEquals(100.0, s.monthTotal!!, 0.001)
        assertEquals(10, s.monthMealsTotal)
    }

    @Test
    fun utility_addition_accumulates_totals() = runTest {
        val vm = createHomeViewModel()
        vm.addUtility("Wifi", 40.0)
        vm.addUtility("Water", 20.0)
        val state = vm.state.value
        assertEquals(2, state.utilities.size)
        assertEquals(60.0, state.utilityTotal, 0.0)
    }

    @Test
    fun utility_per_person_updates_with_count() = runTest {
        val vm = createHomeViewModel()
        vm.addUtility("Wifi", 40.0)
        vm.addUtility("Water", 20.0)
        vm.updateUtilityPersons(4)
        val state = vm.state.value
        assertEquals(4, state.utilityPersons)
        assertEquals(15.0, state.utilityPerPerson, 0.0)
    }

    private fun createHomeViewModel(): HomeViewModel {
        val fakeUsers = FakeUsers()
        return HomeViewModel(
            getTotalCostForRange = GetTotalCostForRange(FakeCosts()),
            getTotalMealsForRange = GetTotalMealsForRange(FakeMeals()),
            getTotalsByUserForRange = GetTotalsByUserForRange(FakeCosts()),
            getMealsByUserForRange = GetMealsByUserForRange(FakeMeals()),
            getCostsForUserRange = GetCostsForUserRange(FakeCosts()),
            getAllMealsForDate = GetAllMealsForDate(FakeMeals()),
            getUserNames = GetUserNames(object : com.ugo.mhews.mealmanage.domain.repository.UserRepository {
                override suspend fun getCurrentProfile(): Result<UserProfile> = Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("NA"))
                override suspend fun updateCurrentName(name: String): Result<Unit> = Result.Success(Unit)
                override suspend fun getNames(uids: Set<String>) = fakeUsers.names(uids)
            }),
            dateProvider = object : DateProvider {
                override fun today(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)
            },
            monthRangeCalculator = MonthRangeCalculator()
        )
    }
}
