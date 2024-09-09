package com.team5.alarmmemo.data.repository.lastmodify

import com.team5.alarmmemo.data.source.local.LastModify
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
annotation class LocalLastModify

@Qualifier
annotation class ReMoteLastModify

@Module
@InstallIn(SingletonComponent::class)
abstract class LastModifyRepositoryModule {
    @LocalLastModify
    @Binds
    abstract fun bindLocalLastModifyRepository(localLastModifyRepositoryImpl: LocalLastModifyRepositoryImpl):LastModifyRepository

    @ReMoteLastModify
    @Binds
    abstract fun bindRemoteLastModifyRepository(remoteLastModifyRepositoryImpl: RemoteLastModifyRepositoryImpl):LastModifyRepository
}