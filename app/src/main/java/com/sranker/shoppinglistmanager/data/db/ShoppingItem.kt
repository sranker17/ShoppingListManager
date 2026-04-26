package com.sranker.shoppinglistmanager.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: Int = 1,
    val isPurchased: Boolean = false,
    val sortOrder: Int
)
