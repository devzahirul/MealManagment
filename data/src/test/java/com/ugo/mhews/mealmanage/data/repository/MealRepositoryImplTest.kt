package com.ugo.mhews.mealmanage.data.repository

import com.ugo.mhews.mealmanage.data.meal.MealRepositoryImpl
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.MealDataSource
import com.ugo.mhews.mealmanage.domain.DomainError
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserId
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private class AuthStub(var userId: UserId? = "user") : AuthDataSource {
    override suspend fun signIn(email: String, password: String): UserId = throw UnsupportedOperationException()
    override suspend fun signUp(email: String, password: String): UserId = throw UnsupportedOperationException()
    override fun currentUserId(): UserId? = userId
    override fun signOut() = Unit
}

private class MealDataSourceStub : MealDataSource {
    val meals = mutableMapOf<Pair<UserId, LocalDate>, Int>()
    val sharedFlow = MutableSharedFlow<Map<LocalDate, Int>>(replay = 1)
    var totalByUser: Map<UserId, Int> = emptyMap()
    var throwOnRead: Throwable? = null
    var flowProvider: () -> Flow<Map<LocalDate, Int>> = { sharedFlow }

    override suspend fun readUserMeal(userId: UserId, date: LocalDate): Meal {
        throwOnRead?.let { throw it }
        return Meal(date, meals[userId to date] ?: 0)
    }

    override suspend fun writeUserMeal(userId: UserId, date: LocalDate, count: Int) {
        throwOnRead?.let { throw it }
        meals[userId to date] = count
    }

    override fun observeUserMeals(userId: UserId, start: LocalDate, endExclusive: LocalDate): Flow<Map<LocalDate, Int>> = flowProvider()

    override suspend fun readAllMealsForDate(date: LocalDate): List<UserMeal> {
        throwOnRead?.let { throw it }
        return meals.filter { it.key.second == date }.map { UserMeal(it.key.first, it.value) }
    }

    override suspend fun readTotalMeals(start: LocalDate, endExclusive: LocalDate): Int {
        throwOnRead?.let { throw it }
        return meals.filter { it.key.second >= start && it.key.second < endExclusive }.values.sum()
    }

    override suspend fun readMealsByUser(start: LocalDate, endExclusive: LocalDate): Map<UserId, Int> {
        throwOnRead?.let { throw it }
        return totalByUser
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MealRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun `requires signed in user`() = scope.runTest {
        val repo = MealRepositoryImpl(AuthStub(userId = null), MealDataSourceStub(), dispatcher)
        val result = repo.getMealForDate(LocalDate.now())
        assertTrue(result is Result.Error && result.error is DomainError.Auth)

        val flowResult = repo.observeMealsForUserRange(LocalDate.now(), LocalDate.now().plusDays(1)).first()
        assertTrue(flowResult is Result.Error && flowResult.error is DomainError.Auth)
    }

    @Test
    fun `reads and writes meals`() = scope.runTest {
        val meals = MealDataSourceStub().apply {
            meals["user" to LocalDate.of(2024, 1, 1)] = 3
            totalByUser = mapOf("user" to 5)
            sharedFlow.tryEmit(mapOf(LocalDate.of(2024, 1, 1) to 3))
        }
        val repo = MealRepositoryImpl(AuthStub(), meals, dispatcher)

        val meal = repo.getMealForDate(LocalDate.of(2024, 1, 1))
        assertEquals(3, (meal as Result.Success).value.count)

        repo.setMealForDate(LocalDate.of(2024, 1, 2), 4)
        assertEquals(4, meals.meals["user" to LocalDate.of(2024, 1, 2)])

        val observed = repo.observeMealsForUserRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3)).first()
        assertTrue(observed is Result.Success)

        val all = repo.getAllMealsForDate(LocalDate.of(2024, 1, 1))
        assertEquals(1, (all as Result.Success).value.size)

        val total = repo.getTotalMealsForRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3))
        assertEquals(7, (total as Result.Success).value)

        val byUser = repo.getMealsByUserForRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3))
        assertEquals(5, (byUser as Result.Success).value["user"])
    }

    @Test
    fun `maps exceptions to DomainError`() = scope.runTest {
        val meals = MealDataSourceStub().apply { throwOnRead = IllegalStateException("boom") }
        val repo = MealRepositoryImpl(AuthStub(), meals, dispatcher)

        val single = repo.getMealForDate(LocalDate.now())
        assertTrue(single is Result.Error && single.error is DomainError.Unknown)

        val all = repo.getAllMealsForDate(LocalDate.now())
        assertTrue(all is Result.Error)

        meals.throwOnRead = null
        meals.flowProvider = {
            kotlinx.coroutines.flow.flow {
                throw IllegalStateException("flow error")
            }
        }
        val flowResult = repo.observeMealsForUserRange(LocalDate.now(), LocalDate.now().plusDays(1)).first()
        assertTrue(flowResult is Result.Error && flowResult.error is DomainError.Unknown)
    }
}
