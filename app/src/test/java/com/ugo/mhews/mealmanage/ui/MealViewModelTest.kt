package com.ugo.mhews.mealmanage.ui

import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetMealForDate
import com.ugo.mhews.mealmanage.domain.usecase.ObserveMealsForMonth
import com.ugo.mhews.mealmanage.domain.usecase.SetMealForDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

private class FakeUserRepo : UserRepository {
    override suspend fun getCurrentProfile() = Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("NA"))
    override suspend fun updateCurrentName(name: String) = Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("NA"))
    override suspend fun getNames(uids: Set<String>) = Result.Success(uids.associateWith { "User ${it.take(6)}" })
}

@OptIn(ExperimentalCoroutinesApi::class)
class MealViewModelTest {
    @Test
    fun observeMonth_thenOpenFutureDate_loadsCount() = runTest {
        val flow = MutableSharedFlow<Result<Map<LocalDate, Int>>>(replay = 1)
        flow.tryEmit(Result.Success(emptyMap()))
        val fakeRepo = object : MealRepository {
            override suspend fun getMealForDate(date: LocalDate) = Result.Success(Meal(date, 5))
            override suspend fun setMealForDate(date: LocalDate, count: Int) = Result.Success(Unit)
            override fun observeMealsForUserRange(start: LocalDate, end: LocalDate): Flow<Result<Map<LocalDate, Int>>> = flow
            override suspend fun getAllMealsForDate(date: LocalDate) = Result.Success(emptyList<UserMeal>())
            override suspend fun getTotalMealsForRange(start: LocalDate, end: LocalDate) = Result.Success(0)
            override suspend fun getMealsByUserForRange(start: LocalDate, end: LocalDate) = Result.Success(emptyMap<String, Int>())
        }
        val vm = MealViewModel(
            observeMealsForMonth = ObserveMealsForMonth(fakeRepo),
            getMealForDate = GetMealForDate(fakeRepo),
            setMealForDate = SetMealForDate(fakeRepo),
            getAllMealsForDate = GetAllMealsForDate(fakeRepo),
            userRepository = FakeUserRepo()
        )

        // Open today (future or today): should set count to 5
        val today = LocalDate.now()
        vm.openDate(today)
        val s = vm.state.first()
        assertEquals(5, s.count)
    }
}
