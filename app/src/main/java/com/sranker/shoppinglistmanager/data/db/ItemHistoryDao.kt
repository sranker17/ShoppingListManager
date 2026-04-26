package com.sranker.shoppinglistmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsert(item: ItemHistory)

    @Query("SELECT name FROM item_history WHERE name LIKE :prefix || '%' ORDER BY name ASC")
    fun searchByPrefix(prefix: String): Flow<List<String>>
}
