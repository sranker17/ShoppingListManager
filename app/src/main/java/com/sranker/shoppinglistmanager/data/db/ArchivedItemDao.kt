package com.sranker.shoppinglistmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivedItemDao {
    @Query("SELECT * FROM archived_items WHERE sessionId = :sessionId")
    fun getBySession(sessionId: Long): Flow<List<ArchivedItem>>

    @Query("SELECT * FROM archived_items WHERE sessionId = :sessionId")
    suspend fun getItemsForSession(sessionId: Long): List<ArchivedItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ArchivedItem>)

    @Query("DELETE FROM archived_items WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
}
