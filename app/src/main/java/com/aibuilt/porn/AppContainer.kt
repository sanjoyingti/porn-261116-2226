package com.aibuilt.porn

import android.content.Context
import com.aibuilt.porn.data.AppDatabase
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.data.NetworkMonitor
import com.aibuilt.porn.data.SettingsRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    val database: AppDatabase = AppDatabase.build(appContext)
    val settings = SettingsRepository(appContext)
    val catalogRepository = CatalogRepository(appContext, database, settings, client)
    val networkMonitor = NetworkMonitor(appContext)
}
