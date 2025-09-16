package com.ugo.mhews.mealmanage.data.repository

import com.ugo.mhews.mealmanage.data.auth.AuthRepositoryImpl
import com.ugo.mhews.mealmanage.data.cost.CostRepositoryImpl
import com.ugo.mhews.mealmanage.data.meal.MealRepositoryImpl
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.CostDataSource
import com.ugo.mhews.mealmanage.data.source.MealDataSource
import com.ugo.mhews.mealmanage.data.source.UserDataSource
import com.ugo.mhews.mealmanage.data.user.UserRepositoryImpl
import com.ugo.mhews.mealmanage.domain.Result
import com.ugo.mhews.mealmanage.domain.model.CostItem
import com.ugo.mhews.mealmanage.domain.model.Meal
import com.ugo.mhews.mealmanage.domain.model.UserMeal
import com.ugo.mhews.mealmanage.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private class FakeAuthDataSource : AuthDataSource {
    var currentUserId: String? = "user"
    var signOutCalled = false
    var nextSignInResult: Result<String> = Result.Success("user")
    var nextSignUpResult: Result<String> = Result.Success("user")

    override suspend fun signIn(email: String, password: String): String {
        val result = nextSignInResult
        return when (result) {
            is Result.Success -> result.value
            is Result.Error -> throw RuntimeException(result.error.message)
        }
    }

    override suspend fun signUp(email: String, password: String): String {
        val result = nextSignUpResult
        return when (result) {
            is Result.Success -> result.value
            is Result.Error -> throw RuntimeException(result.error.message)
        }
    }

    override fun currentUserId(): String? = currentUserId

    override fun signOut() {
        signOutCalled = true
    }
}

private class FakeUserDataSource : UserDataSource {
    var storedProfile: UserProfile? = null
    var storedNames: Map<String, String> = emptyMap()

    override suspend fun loadProfile(userId: String): UserProfile? = storedProfile

    override suspend fun saveProfile(userId: String, profile: UserProfile) {
        storedProfile = profile
    }

    override suspend fun loadNames(userIds: Set<String>): Map<String, String> = storedNames
}

private class FakeMealDataSource : MealDataSource {
    var meals: MutableMap<Pair<String, LocalDate>, Int> = mutableMapOf()
    var totalByUser: Map<String, Int> = emptyMap()
    val flow = MutableSharedFlow<Map<LocalDate, Int>>(replay = 1)
    var shouldThrow = false

    override suspend fun readUserMeal(userId: String, date: LocalDate): Meal {
        if (shouldThrow) throw IllegalStateException("boom")
        val count = meals[userId to date] ?: 0
        return Meal(date, count)
    }

    override suspend fun writeUserMeal(userId: String, date: LocalDate, count: Int) {
        if (shouldThrow) throw IllegalStateException("boom")
        meals[userId to date] = count
    }

    override fun observeUserMeals(userId: String, start: LocalDate, endExclusive: LocalDate): Flow<Map<LocalDate, Int>> {
        return flow
    }

    override suspend fun readAllMealsForDate(date: LocalDate): List<UserMeal> {
        return meals.filter { it.key.second == date }.map { UserMeal(it.key.first, it.value) }
    }

    override suspend fun readTotalMeals(start: LocalDate, endExclusive: LocalDate): Int {
        return meals.filter { it.key.second >= start && it.key.second < endExclusive }.values.sum()
    }

    override suspend fun readMealsByUser(start: LocalDate, endExclusive: LocalDate): Map<String, Int> = totalByUser
}

private class FakeCostDataSource : CostDataSource {
    var storedItems: MutableList<CostItem> = mutableListOf()
    var totalsByUser: Map<String, Double> = emptyMap()
    var totalCost: Double = 0.0
    var throwOnAdd = false

    override suspend fun addCost(userId: String?, item: CostItem) {
        if (throwOnAdd) throw IllegalStateException("boom")
        storedItems += item.copy(name = item.name)
    }

    override suspend fun totalCost(startMs: Long, endMs: Long, userId: String?): Double = totalCost

    override suspend fun totalsByUser(startMs: Long, endMs: Long): Map<String, Double> = totalsByUser

    override suspend fun costsForUser(userId: String, startMs: Long, endMs: Long): List<CostItem> = storedItems
}

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Test
    fun authRepository_successAndFailure() = scope.runTest {
        val auth = FakeAuthDataSource()
        val repo = AuthRepositoryImpl(auth, dispatcher)

        val success = repo.signIn("a", "b")
        assertTrue(success is Result.Success)

        auth.nextSignInResult = Result.Error(com.ugo.mhews.mealmanage.domain.DomainError.Unknown("fail"))
        val failure = repo.signIn("a", "b")
        assertTrue(failure is Result.Error)

        repo.signOut()
        assertTrue(auth.signOutCalled)
    }

    @Test
    fun userRepository_readsAndWritesProfiles() = scope.runTest {
        val auth = FakeAuthDataSource()
        val users = FakeUserDataSource()
        users.storedProfile = UserProfile("user", "Name", "Email")
        val repo = UserRepositoryImpl(auth, users, dispatcher)

        val profile = repo.getCurrentProfile()
        assertEquals("Name", (profile as Result.Success).value.name)

        repo.updateCurrentName("Changed")
        assertEquals("Changed", users.storedProfile?.name)

        users.storedNames = mapOf("user" to "Display")
        val names = repo.getNames(setOf("user"))
        assertEquals("Display", (names as Result.Success).value["user"])
    }

    @Test
    fun mealRepository_coversHappyPathAndErrors() = scope.runTest {
        val auth = FakeAuthDataSource()
        val meals = FakeMealDataSource()
        meals.meals["user" to LocalDate.of(2024, 1, 1)] = 3
        meals.totalByUser = mapOf("user" to 5)
        meals.flow.tryEmit(mapOf(LocalDate.of(2024, 1, 1) to 3))
        val repo = MealRepositoryImpl(auth, meals, dispatcher)

        val meal = repo.getMealForDate(LocalDate.of(2024, 1, 1))
        assertEquals(3, (meal as Result.Success).value.count)

        repo.setMealForDate(LocalDate.of(2024, 1, 2), 4)
        assertEquals(4, meals.meals["user" to LocalDate.of(2024, 1, 2)])

        val flowResult = repo.observeMealsForUserRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3)).first()
        assertTrue(flowResult is Result.Success)

        val totals = repo.getTotalMealsForRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3))
        assertEquals(7, (totals as Result.Success).value)

        val byUser = repo.getMealsByUserForRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3))
        assertEquals(5, (byUser as Result.Success).value["user"])

        meals.shouldThrow = true
        val error = repo.getMealForDate(LocalDate.of(2024, 1, 10))
        assertTrue(error is Result.Error)
    }

    @Test
    fun costRepository_handlesAllOperations() = scope.runTest {
        val auth = FakeAuthDataSource()
        val costs = FakeCostDataSource()
        costs.totalCost = 12.0
        costs.totalsByUser = mapOf("user" to 7.5)
        costs.storedItems = mutableListOf()
        val repo = CostRepositoryImpl(auth, costs, dispatcher)

        val addRes = repo.addCost(CostItem("Tea", 2.0, 0))
        assertTrue(addRes is Result.Success)

        val totalRes = repo.getTotalCostForRange(0, 10, null)
        assertEquals(12.0, (totalRes as Result.Success).value, 0.0)

        val totalsByUser = repo.getTotalsByUserForRange(0, 10)
        assertEquals(7.5, (totalsByUser as Result.Success).value.getValue("user"), 0.0)

        val perUser = repo.getCostsForUserRange("user", 0, 10)
        assertEquals(1, (perUser as Result.Success).value.size)

        costs.throwOnAdd = true
        val addFail = repo.addCost(CostItem("Coffee", 3.0, 0))
        assertTrue(addFail is Result.Error)
    }
}
