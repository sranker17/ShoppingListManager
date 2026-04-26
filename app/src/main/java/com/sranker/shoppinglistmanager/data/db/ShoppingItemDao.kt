package com.sranker.shoppinglistmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getById(id: Long): ShoppingItem?

    @Query("SELECT MAX(sortOrder) FROM shopping_items")
    suspend fun getMaxSortOrder(): Int?

    @Query("SELECT * FROM shopping_items WHERE isPurchased = 1")
    suspend fun getPurchasedItems(): List<ShoppingItem>

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun deletePurchasedItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItem): Long

    @Update
    suspend fun update(item: ShoppingItem)

    @Update
    suspend fun updateAll(items: List<ShoppingItem>)

    @Delete
    suspend fun delete(item: ShoppingItem)
}
