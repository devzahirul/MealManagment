package com.ugo.mhews.mealmanage.di

import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import com.ugo.mhews.mealmanage.domain.usecase.GetAllMealsForDate
import com.ugo.mhews.mealmanage.domain.usecase.GetMealForDate
import com.ugo.mhews.mealmanage.domain.usecase.ObserveMealsForMonth
import com.ugo.mhews.mealmanage.domain.usecase.SetMealForDate
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
}

