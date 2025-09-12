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
}

