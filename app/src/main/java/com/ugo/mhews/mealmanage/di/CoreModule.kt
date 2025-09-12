package com.ugo.mhews.mealmanage.di

import com.ugo.mhews.mealmanage.core.DateProvider
import com.ugo.mhews.mealmanage.core.SystemDateProvider
import com.ugo.mhews.mealmanage.core.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideDateProvider(): DateProvider = SystemDateProvider()

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
