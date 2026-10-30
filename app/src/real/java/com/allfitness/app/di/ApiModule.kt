package com.allfitness.app.di

import com.allfitness.app.auth.AuthApi
import com.allfitness.app.nutrition.DietApi
import com.allfitness.app.onboarding.ProfileApi
import com.allfitness.app.progress.ProgressApi
import com.allfitness.app.routines.RoutineApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

/** Flavor real: las mismas API contra el backend de `allfitness.baseUrl`. */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides @Singleton fun auth(retrofit: Retrofit): AuthApi = retrofit.create()
    @Provides @Singleton fun profile(retrofit: Retrofit): ProfileApi = retrofit.create()
    @Provides @Singleton fun routine(retrofit: Retrofit): RoutineApi = retrofit.create()
    @Provides @Singleton fun diet(retrofit: Retrofit): DietApi = retrofit.create()
    @Provides @Singleton fun progress(retrofit: Retrofit): ProgressApi = retrofit.create()
}
