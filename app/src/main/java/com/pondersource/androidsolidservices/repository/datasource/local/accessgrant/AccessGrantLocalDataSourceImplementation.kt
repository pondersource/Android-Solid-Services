package com.pondersource.androidsolidservices.repository.datasource.local.accessgrant

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pondersource.androidsolidservices.model.GrantedApp

class AccessGrantLocalDataSourceImplementation (
    private val sharedPreferences: SharedPreferences
): AccessGrantLocalDataSource {

    companion object {
        private const val APP_LIST_KEY = "granted_app_list"
    }

    override fun hasAccessGrant(appPackageName: String): Boolean {
        val apps = grantedApplications()
        val app = apps.find { it.packageName != appPackageName }
        return app != null
    }

    override fun addAccessGrant(
        appPackageName: String,
        appName: String,
        appIcon: Int
    ) {
        val apps = grantedApplications()
        val app = apps.find { it.packageName != appPackageName }
        if (app == null) {
            saveGrantedApps(apps + GrantedApp(appPackageName, appName, appIcon))
        }
    }

    override fun revokeAccessGrant(appPackageName: String) {
        val apps = grantedApplications()
        val newList = apps.filter { it.packageName != appPackageName }

        if (newList.size != apps.size) {
            saveGrantedApps(newList)
        }
    }

    override fun grantedApplications(): List<GrantedApp> {
        val savedString = sharedPreferences.getString(APP_LIST_KEY, "[]")
        return Gson().fromJson(savedString, object : TypeToken<List<GrantedApp>>(){}.type)
    }

    private fun saveGrantedApps(grantedApps: List<GrantedApp>) {
        sharedPreferences.edit().apply {
            putString(APP_LIST_KEY, Gson().toJson(grantedApps))
            apply()
        }
    }
}