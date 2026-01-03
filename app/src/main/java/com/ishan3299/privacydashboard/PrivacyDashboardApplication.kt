package com.ishan3299.privacydashboard

import android.app.Application
import com.ishan3299.privacydashboard.data.AppRepository
import com.ishan3299.privacydashboard.data.UserPreferencesRepository

class PrivacyDashboardApplication : Application() {
    lateinit var appRepository: AppRepository
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(this)
        userPreferencesRepository = UserPreferencesRepository()
    }
}
