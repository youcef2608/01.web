package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        HelpCallEntity::class,
        VolunteerEntity::class,
        EchoActionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AchdaDatabase : RoomDatabase() {
    abstract fun helpCallDao(): HelpCallDao
    abstract fun volunteerDao(): VolunteerDao
    abstract fun echoActionDao(): EchoActionDao

    companion object {
        @Volatile
        private var INSTANCE: AchdaDatabase? = null

        fun getDatabase(context: Context): AchdaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AchdaDatabase::class.java,
                    "achda_community.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
