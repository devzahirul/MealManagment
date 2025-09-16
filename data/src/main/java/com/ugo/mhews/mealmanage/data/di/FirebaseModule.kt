package com.ugo.mhews.mealmanage.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ugo.mhews.mealmanage.data.source.AuthDataSource
import com.ugo.mhews.mealmanage.data.source.CostDataSource
import com.ugo.mhews.mealmanage.data.source.MealDataSource
import com.ugo.mhews.mealmanage.data.source.UserDataSource
import com.ugo.mhews.mealmanage.data.source.firebase.FirebaseAuthDataSource
import com.ugo.mhews.mealmanage.data.source.firebase.FirebaseCostDataSource
import com.ugo.mhews.mealmanage.data.source.firebase.FirebaseMealDataSource
import com.ugo.mhews.mealmanage.data.source.firebase.FirebaseUserDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthDataSource(auth: FirebaseAuth): AuthDataSource = FirebaseAuthDataSource(auth)

    @Provides
    @Singleton
    fun provideMealDataSource(db: FirebaseFirestore): MealDataSource = FirebaseMealDataSource(db)

    @Provides
    @Singleton
    fun provideCostDataSource(db: FirebaseFirestore, auth: FirebaseAuth): CostDataSource = FirebaseCostDataSource(db, auth)

    @Provides
    @Singleton
    fun provideUserDataSource(db: FirebaseFirestore): UserDataSource = FirebaseUserDataSource(db)
}
