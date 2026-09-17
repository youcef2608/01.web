package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EchoActionDao {
    @Query("SELECT * FROM echo_actions ORDER BY timestamp DESC")
    fun getAllEchoes(): Flow<List<EchoActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: EchoActionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<EchoActionEntity>)
}
