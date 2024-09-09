package com.team5.alarmmemo.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteAuthRepositoryModule {

    @Binds
    abstract fun bindRemoteAuthRepository(remoteAuthRepositoryImpl: RemoteAuthRepositoryImpl):RemoteAuthRepository
}