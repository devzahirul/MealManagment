package com.ugo.mhews.mealmanage.data.di

import com.ugo.mhews.mealmanage.data.meal.MealRepositoryImpl
import com.ugo.mhews.mealmanage.data.user.UserRepositoryImpl
import com.ugo.mhews.mealmanage.domain.repository.MealRepository
import com.ugo.mhews.mealmanage.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMealRepository(impl: MealRepositoryImpl): MealRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

