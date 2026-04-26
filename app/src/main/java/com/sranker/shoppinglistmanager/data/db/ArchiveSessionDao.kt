package com.sranker.shoppinglistmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveSessionDao {
    @Query("SELECT * FROM archive_sessions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ArchiveSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ArchiveSession): Long

    @Update
    suspend fun update(session: ArchiveSession)

    @Delete
    suspend fun delete(session: ArchiveSession)
}
