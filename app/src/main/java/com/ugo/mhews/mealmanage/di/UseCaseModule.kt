package com.ugo.mhews.mealmanage.di

import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository
import com.ugo.mhews.mealmanage.domain.repository.CostRepository
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetMealForDate
import com.ugo.mhews.mealmanage.domain.usecase.ObserveMealsForMonth
import com.ugo.mhews.mealmanage.domain.usecase.SetMealForDate
import com.ugo.mhews.mealmanage.domain.usecase.AddCost
import com.ugo.mhews.mealmanage.domain.usecase.GetCostsForUserRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalCostForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetTotalMealsForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetMealsByUserForRange
import com.ugo.mhews.mealmanage.domain.usecase.GetCurrentProfile
import com.ugo.mhews.mealmanage.domain.usecase.GetCurrentUserId
import com.ugo.mhews.mealmanage.domain.usecase.GetUserNames
import com.ugo.mhews.mealmanage.domain.usecase.SignIn
import com.ugo.mhews.mealmanage.domain.usecase.SignOut
import com.ugo.mhews.mealmanage.domain.usecase.SignUp
import com.ugo.mhews.mealmanage.domain.usecase.UpdateCurrentName
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import com.ugo.mhews.mealmanage.domain.time.MonthRangeCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideObserveMealsForMonth(repo: MealRepository) = ObserveMealsForMonth(repo)

    @Provides
    @Singleton
    fun provideSetMealForDate(repo: MealRepository) = SetMealForDate(repo)

    @Provides
    @Singleton
    fun provideGetMealForDate(repo: MealRepository) = GetMealForDate(repo)

    @Provides
    @Singleton
    fun provideGetAllMealsForDate(repo: MealRepository) = GetAllMealsForDate(repo)

    @Provides
    @Singleton
    fun provideAddCost(repo: CostRepository) = AddCost(repo)

    @Provides
    @Singleton
    fun provideGetTotalCostForRange(repo: CostRepository) = GetTotalCostForRange(repo)

    @Provides
    @Singleton
    fun provideGetTotalsByUserForRange(repo: CostRepository) = GetTotalsByUserForRange(repo)

    @Provides
    @Singleton
    fun provideGetCostsForUserRange(repo: CostRepository) = GetCostsForUserRange(repo)

    @Provides
    @Singleton
    fun provideGetTotalMealsForRange(repo: MealRepository) = GetTotalMealsForRange(repo)

    @Provides
    @Singleton
    fun provideGetMealsByUserForRange(repo: MealRepository) = GetMealsByUserForRange(repo)

    @Provides
    @Singleton
    fun provideGetCurrentProfile(repo: UserRepository) = GetCurrentProfile(repo)

    @Provides
    @Singleton
    fun provideUpdateCurrentName(repo: UserRepository) = UpdateCurrentName(repo)

    @Provides
    @Singleton
    fun provideGetUserNames(repo: UserRepository) = GetUserNames(repo)

    @Provides
    @Singleton
    fun provideSignIn(repo: AuthRepository) = SignIn(repo)

    @Provides
    @Singleton
    fun provideSignUp(repo: AuthRepository) = SignUp(repo)

    @Provides
    @Singleton
    fun provideSignOut(repo: AuthRepository) = SignOut(repo)

    @Provides
    @Singleton
    fun provideGetCurrentUserId(repo: AuthRepository) = GetCurrentUserId(repo)

    @Provides
    @Singleton
    fun provideMonthRangeCalculator() = MonthRangeCalculator()
}
