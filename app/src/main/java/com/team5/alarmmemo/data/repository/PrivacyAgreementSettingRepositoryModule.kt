package com.team5.alarmmemo.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PrivacyAgreementSettingRepositoryModule {
    @Binds
    abstract fun bindPrivacyAgreementSettingRepository(privacyAgreementSettingRepositoryImpl: PrivacyAgreementSettingRepositoryImpl):PrivacyAgreementSettingRepository
}