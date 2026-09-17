package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpCallDao {
    @Query("SELECT * FROM help_calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<HelpCallEntity>>

    @Query("SELECT * FROM help_calls WHERE id = :id")
    suspend fun getCallById(id: String): HelpCallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(call: HelpCallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calls: List<HelpCallEntity>)

    @Query("UPDATE help_calls SET status = :newStatus WHERE id = :callId")
    suspend fun updateStatus(callId: String, newStatus: String)

    @Query("DELETE FROM help_calls WHERE id = :id")
    suspend fun deleteById(id: String)
}
