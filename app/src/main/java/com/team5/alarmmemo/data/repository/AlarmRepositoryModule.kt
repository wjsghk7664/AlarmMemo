package com.team5.alarmmemo.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmRepositoryModule {
    @Binds
    abstract fun bindAlarmRepositoryModule(alarmRepositoryImpl: AlarmRepositoryImpl):AlarmRepository
}