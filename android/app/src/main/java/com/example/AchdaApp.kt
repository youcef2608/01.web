package com.example

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

class AchdaApp : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // تهيئة إعدادات osmdroid للخرائط المفتوحة OpenStreetMap
        try {
            Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
