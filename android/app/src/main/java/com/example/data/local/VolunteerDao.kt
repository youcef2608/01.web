package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerDao {
    @Query("SELECT * FROM volunteers WHERE id = :id")
    fun getVolunteerFlow(id: String): Flow<VolunteerEntity?>

    @Query("SELECT * FROM volunteers WHERE id = :id")
    suspend fun getVolunteer(id: String): VolunteerEntity?

    @Query("SELECT * FROM volunteers ORDER BY echoPoints DESC")
    fun getAllVolunteers(): Flow<List<VolunteerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(volunteer: VolunteerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(volunteers: List<VolunteerEntity>)
}
