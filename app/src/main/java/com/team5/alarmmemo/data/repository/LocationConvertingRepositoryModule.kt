package com.team5.alarmmemo.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationConvertingRepositoryModule {
    @Binds
    abstract fun bindLocationConvertingRepository(locationConvertingRepositoryImpl: LocationConvertingRepositoryImpl):LocationConvertingRepository
}