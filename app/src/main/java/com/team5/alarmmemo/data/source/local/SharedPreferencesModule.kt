package com.team5.alarmmemo.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
annotation class LastModify

@Qualifier
annotation class SignUp

@Qualifier
annotation class Memo

@Qualifier
annotation class Draw

@Qualifier
annotation class Title

@Qualifier
annotation class AlarmSettings

@Qualifier
annotation class ActiveAlarms

@Qualifier
annotation class SpanCount

@Qualifier
annotation class UserPref

@Qualifier
annotation class Privacy

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @LastModify
    @Provides
    fun provideLastModifySharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("last_modify_pref", 0)
    }

    @SignUp
    @Provides
    fun provideSignUpSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("sign_up_pref", 0)
    }

    @Memo
    @Provides
    fun provideMemoSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("memo_pref", 0)
    }

    @Draw
    @Provides
    fun provideDrawSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("draw_pref", 0)
    }

    @Title
    @Provides
    fun provideTitleSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("title_pref", 0)
    }

    @AlarmSettings
    @Provides
    fun provideAlarmSettingSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("alarm_setting_pref", 0)
    }

    @ActiveAlarms
    @Provides
    fun provideActiveAlarmsSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("active_alarm_pref", 0)
    }

    @SpanCount
    @Provides
    fun provideSpanCountSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("span_count_pref", 0)
    }

    @UserPref
    @Provides
    fun provideUserSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "user_pref",
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Privacy
    @Provides
    fun providePrivacySharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("privacy_pref", 0)
    }
}
