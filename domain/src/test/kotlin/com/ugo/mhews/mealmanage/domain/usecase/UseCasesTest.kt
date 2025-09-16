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

private class FakeAuthRepo : com.ugo.mhews.mealmanage.domain.repository.AuthRepository {
    var signedOut = false
    var lastEmail: String? = null
    var lastPassword: String? = null
    override suspend fun signIn(email: String, password: String) =
        com.ugo.mhews.mealmanage.domain.Result.Success("uid-signin").also {
            lastEmail = email; lastPassword = password
        }
    override suspend fun signUp(email: String, password: String) =
        com.ugo.mhews.mealmanage.domain.Result.Success("uid-signup").also {
            lastEmail = email; lastPassword = password
        }
    override fun currentUser(): String? = "uid-current"
    override fun signOut() { signedOut = true }
}

private class FakeUserRepo : com.ugo.mhews.mealmanage.domain.repository.UserRepository {
    override suspend fun getCurrentProfile() =
        com.ugo.mhews.mealmanage.domain.Result.Success(
            com.ugo.mhews.mealmanage.domain.model.UserProfile("uid", "name", "email")
        )
    override suspend fun updateCurrentName(name: String) =
        com.ugo.mhews.mealmanage.domain.Result.Success(Unit)
    override suspend fun getNames(uids: Set<String>) =
        com.ugo.mhews.mealmanage.domain.Result.Success(uids.associateWith { "User-$it" })
}

class AuthAndUserUseCasesTest {
    private val auth = FakeAuthRepo()
    private val users = FakeUserRepo()

    @Test fun signIn_delegates() = runBlocking {
        val res = SignIn(auth)("a@b.com", "secret")
        assertEquals("uid-signin", (res as Result.Success).value)
        assertEquals("a@b.com", auth.lastEmail)
    }

    @Test fun signUp_delegates() = runBlocking {
        val res = SignUp(auth)("c@d.com", "secret")
        assertEquals("uid-signup", (res as Result.Success).value)
        assertEquals("c@d.com", auth.lastEmail)
    }

    @Test fun signOut_invokesRepo() {
        SignOut(auth)()
        assertEquals(true, auth.signedOut)
    }

    @Test fun currentUser_readsRepo() {
        val uid = GetCurrentUserId(auth)()
        assertEquals("uid-current", uid)
    }

    @Test fun getCurrentProfile_callsRepo() = runBlocking {
        val profile = GetCurrentProfile(users)()
        assertEquals("name", (profile as Result.Success).value.name)
    }

    @Test fun updateCurrentName_callsRepo() = runBlocking {
        val res = UpdateCurrentName(users)("New")
        assertEquals(true, res is Result.Success)
    }

    @Test fun getUserNames_callsRepo() = runBlocking {
        val res = GetUserNames(users)(setOf("u1", "u2"))
        val names = (res as Result.Success).value
        assertEquals("User-u1", names["u1"])
    }
}
