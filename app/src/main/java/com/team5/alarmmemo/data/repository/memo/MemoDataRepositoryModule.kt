package com.team5.alarmmemo.data.repository.memo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Qualifier

@Qualifier
annotation class LocalRepository

@Qualifier
annotation class RemoteRepository

@Module
@InstallIn(ViewModelComponent::class)
abstract class MemoDataRepositoryModule {
    @LocalRepository
    @Binds
    abstract fun bindLocalMemoDataRepository(localMemoDataRepositoryImpl: LocalMemoDataRepositoryImpl): MemoDataRepository

    @RemoteRepository
    @Binds
    abstract fun bindRemoteMemoDataRepository(remoteMemoDataRepositoryImpl: RemoteMemoDataRepositoryImpl): MemoDataRepository
}