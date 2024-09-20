package com.team5.alarmmemo.data.repository

import android.content.SharedPreferences
import com.team5.alarmmemo.data.source.local.Privacy
import javax.inject.Inject

class PrivacyAgreementSettingRepositoryImpl @Inject constructor(@Privacy private val sharedPreferences: SharedPreferences):PrivacyAgreementSettingRepository {
    override fun setPrivacy(agreement: Boolean) {
        sharedPreferences.edit().putBoolean("agreement",agreement).apply()
    }

    override fun getPrivacy(): Boolean {
        return sharedPreferences.getBoolean("agreement",false)
    }
}