package com.team5.alarmmemo.data.repository

interface PrivacyAgreementSettingRepository {
    fun setPrivacy(agreement: Boolean)
    fun getPrivacy():Boolean
}