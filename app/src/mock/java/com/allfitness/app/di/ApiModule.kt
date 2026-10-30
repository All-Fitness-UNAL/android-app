package com.allfitness.app.di

import com.allfitness.app.auth.AuthApi
import com.allfitness.app.mock.FakeBackend
import com.allfitness.app.nutrition.DietApi
import com.allfitness.app.onboarding.ProfileApi
import com.allfitness.app.progress.ProgressApi
import com.allfitness.app.routines.RoutineApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Flavor mock: todas las API las responde [FakeBackend] (constitución II). */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides fun auth(fake: FakeBackend): AuthApi = fake
    @Provides fun profile(fake: FakeBackend): ProfileApi = fake
    @Provides fun routine(fake: FakeBackend): RoutineApi = fake
    @Provides fun diet(fake: FakeBackend): DietApi = fake.diet
    @Provides fun progress(fake: FakeBackend): ProgressApi = fake
}
