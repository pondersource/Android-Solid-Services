package com.pondersource.androidsolidservices.repository.datasource.local.accessgrant

import android.content.SharedPreferences
import com.pondersource.androidsolidservices.model.GrantedApp
import kotlinx.serialization.json.Json

class AccessGrantLocalDataSourceImplementation(
    private val sharedPreferences: SharedPreferences
) : AccessGrantLocalDataSource {

    companion object {
        private const val APP_LIST_KEY = "granted_app_list"
    }

    override fun hasAccessGrant(appPackageName: String): Boolean {
        val apps = grantedApplications()
        val app = apps.find { it.packageName == appPackageName }
        return app != null
    }

    override fun addAccessGrant(
        appPackageName: String,
        appName: String
    ) {
        val apps = grantedApplications()
        val app = apps.find { it.packageName == appPackageName }
        if (app == null) {
            saveGrantedApps(apps + GrantedApp(appPackageName, appName))
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
        val savedString = sharedPreferences.getString(APP_LIST_KEY, "[]")!!
        return Json.decodeFromString<List<GrantedApp>>(savedString)
    }

    private fun saveGrantedApps(grantedApps: List<GrantedApp>) {
        sharedPreferences.edit().apply {
            putString(APP_LIST_KEY, Json.encodeToString(grantedApps))
            apply()
        }
    }
}