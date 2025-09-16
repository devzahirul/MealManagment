package com.ugo.mhews.mealmanage.ui

import com.ugo.mhews.mealmanage.MainDispatcherRule
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import com.ugo.mhews.mealmanage.domain.usecase.AddCost
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeCostRepo : CostRepository {
    override suspend fun addCost(entry: CostItem): Result<Unit> = Result.Success(Unit)
    override suspend fun getTotalCostForRange(startMs: Long, endMs: Long, uid: String?): Result<Double> = Result.Success(0.0)
    override suspend fun getTotalsByUserForRange(startMs: Long, endMs: Long): Result<Map<String, Double>> = Result.Success(emptyMap())
    override suspend fun getCostsForUserRange(uid: String, startMs: Long, endMs: Long): Result<List<CostItem>> = Result.Success(emptyList())
}

class CostAddViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submit_success_shows_snackbar() = runTest {
        val vm = CostAddViewModel(AddCost(FakeCostRepo()))
        vm.submit("Tea", 10.0, 0) {}
        advanceUntilIdle()
        val s = vm.state.value
        assertEquals("Saved successfully", s.snackbar)
    }

    @Test
    fun addUtility_accumulates_total() {
        val vm = CostAddViewModel(AddCost(FakeCostRepo()))

        vm.addUtility("Wifi", 40.0)
        vm.addUtility("Water", 20.0)

        val state = vm.state.value
        assertEquals(2, state.utilities.size)
        assertEquals(60.0, state.totalUtility, 0.0)
    }

    @Test
    fun perPersonUtility_updates_with_person_count() {
        val vm = CostAddViewModel(AddCost(FakeCostRepo()))

        vm.addUtility("Wifi", 40.0)
        vm.addUtility("Water", 20.0)
        vm.updateUtilityPersons(4)

        val state = vm.state.value
        assertEquals(4, state.totalPersons)
        assertEquals(15.0, state.perPersonUtility, 0.0)
    }
}
