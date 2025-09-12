package com.ugo.mhews.mealmanage.data.di;

import com.ugo.mhews.mealmanage.data.meal.MealRepositoryImpl;
import com.ugo.mhews.mealmanage.data.cost.CostRepositoryImpl;
import com.ugo.mhews.mealmanage.data.auth.AuthRepositoryImpl;
import com.ugo.mhews.mealmanage.data.user.UserRepositoryImpl;
import com.ugo.mhews.mealmanage.domain.repository.MealRepository;
import com.ugo.mhews.mealmanage.domain.repository.UserRepository;
import com.ugo.mhews.mealmanage.domain.repository.CostRepository;
import com.ugo.mhews.mealmanage.domain.repository.AuthRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'\u00a8\u0006\u0010"}, d2 = {"Lcom/ugo/mhews/mealmanage/data/di/RepositoryModule;", "", "()V", "bindAuthRepository", "Lcom/ugo/mhews/mealmanage/domain/repository/AuthRepository;", "impl", "Lcom/ugo/mhews/mealmanage/data/auth/AuthRepositoryImpl;", "bindCostRepository", "Lcom/ugo/mhews/mealmanage/domain/repository/CostRepository;", "Lcom/ugo/mhews/mealmanage/data/cost/CostRepositoryImpl;", "bindMealRepository", "Lcom/ugo/mhews/mealmanage/domain/repository/MealRepository;", "Lcom/ugo/mhews/mealmanage/data/meal/MealRepositoryImpl;", "bindUserRepository", "Lcom/ugo/mhews/mealmanage/domain/repository/UserRepository;", "Lcom/ugo/mhews/mealmanage/data/user/UserRepositoryImpl;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.ugo.mhews.mealmanage.domain.repository.MealRepository bindMealRepository(@org.jetbrains.annotations.NotNull
    com.ugo.mhews.mealmanage.data.meal.MealRepositoryImpl impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.ugo.mhews.mealmanage.domain.repository.UserRepository bindUserRepository(@org.jetbrains.annotations.NotNull
    com.ugo.mhews.mealmanage.data.user.UserRepositoryImpl impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.ugo.mhews.mealmanage.domain.repository.CostRepository bindCostRepository(@org.jetbrains.annotations.NotNull
    com.ugo.mhews.mealmanage.data.cost.CostRepositoryImpl impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.ugo.mhews.mealmanage.domain.repository.AuthRepository bindAuthRepository(@org.jetbrains.annotations.NotNull
    com.ugo.mhews.mealmanage.data.auth.AuthRepositoryImpl impl);
}