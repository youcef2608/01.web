package com.example.di

import com.example.AchdaApp
import com.example.data.local.AchdaDatabase
import com.example.data.repository.GeminiRepository
import com.example.data.repository.HelpCallRepository
import com.example.data.repository.LocationRepository
import com.example.data.repository.VolunteerRepository

/**
 * حاوية التبعيات للتطبيق (Dependency Injection Container)
 * توفر كائنات موحدة وقاعدة بيانات حقيقية (Room SQLite) للمشروع
 */
interface AppContainer {
    val database: AchdaDatabase
    val geminiRepository: GeminiRepository
    val helpCallRepository: HelpCallRepository
    val volunteerRepository: VolunteerRepository
    val locationRepository: LocationRepository
}

class DefaultAppContainer : AppContainer {
    override val database: AchdaDatabase by lazy {
        AchdaDatabase.getDatabase(AchdaApp.appContext)
    }

    override val geminiRepository: GeminiRepository by lazy {
        GeminiRepository()
    }

    override val helpCallRepository: HelpCallRepository by lazy {
        HelpCallRepository(
            helpCallDao = database.helpCallDao(),
            echoActionDao = database.echoActionDao()
        )
    }

    override val volunteerRepository: VolunteerRepository by lazy {
        VolunteerRepository(
            volunteerDao = database.volunteerDao()
        )
    }

    override val locationRepository: LocationRepository by lazy {
        LocationRepository(AchdaApp.appContext)
    }
}

object AppModule {
    val container: AppContainer by lazy {
        DefaultAppContainer()
    }
}
