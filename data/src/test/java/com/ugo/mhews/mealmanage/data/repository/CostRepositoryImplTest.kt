package com.ugo.mhews.mealmanage.data.repository

import com.ugo.mhews.mealmanage.data.cost.CostRepositoryImpl
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.CostDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.UserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class AuthStubCost(private val id: UserId? = "user") : AuthDataSource {
    override suspend fun signIn(email: String, password: String): UserId = throw UnsupportedOperationException()
    override suspend fun signUp(email: String, password: String): UserId = throw UnsupportedOperationException()
    override fun currentUserId(): UserId? = id
    override fun signOut() = Unit
}

private class CostDataSourceStub : CostDataSource {
    var storedItems = mutableListOf<CostItem>()
    var totalCost: Double = 0.0
    var totalsByUser: Map<UserId, Double> = emptyMap()
    var throwOnCall: Throwable? = null

    override suspend fun addCost(userId: UserId?, item: CostItem) {
        throwOnCall?.let { throw it }
        storedItems += item.copy(name = item.name)
    }

    override suspend fun totalCost(startMs: Long, endMs: Long, userId: UserId?): Double {
        throwOnCall?.let { throw it }
        return totalCost
    }

    override suspend fun totalsByUser(startMs: Long, endMs: Long): Map<UserId, Double> {
        throwOnCall?.let { throw it }
        return totalsByUser
    }

    override suspend fun costsForUser(userId: UserId, startMs: Long, endMs: Long): List<CostItem> {
        throwOnCall?.let { throw it }
        return storedItems
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CostRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun `addCost includes user id and reports success`() = scope.runTest {
        val costs = CostDataSourceStub()
        val repo = CostRepositoryImpl(AuthStubCost("user"), costs, dispatcher)

        val result = repo.addCost(CostItem("Tea", 2.0, 0))
        assertTrue(result is Result.Success)
        assertEquals(1, costs.storedItems.size)
    }

    @Test
    fun `queries totals and per user`() = scope.runTest {
        val costs = CostDataSourceStub().apply {
            totalCost = 12.5
            totalsByUser = mapOf("user" to 7.0)
            storedItems = mutableListOf(CostItem("Tea", 2.0, 0))
        }
        val repo = CostRepositoryImpl(AuthStubCost("user"), costs, dispatcher)

        val total = repo.getTotalCostForRange(0, 10, null)
        assertEquals(12.5, (total as Result.Success).value, 0.0)

        val byUser = repo.getTotalsByUserForRange(0, 10)
        assertEquals(7.0, (byUser as Result.Success).value.getValue("user"), 0.0)

        val list = repo.getCostsForUserRange("user", 0, 10)
        assertEquals(1, (list as Result.Success).value.size)
    }

    @Test
    fun `maps exceptions to DomainError`() = scope.runTest {
        val costs = CostDataSourceStub().apply { throwOnCall = IllegalStateException("boom") }
        val repo = CostRepositoryImpl(AuthStubCost("user"), costs, dispatcher)

        val add = repo.addCost(CostItem("Tea", 2.0, 0))
        assertTrue(add is Result.Error && add.error is DomainError.Unknown)

        val total = repo.getTotalCostForRange(0, 10, null)
        assertTrue(total is Result.Error)
    }
}
