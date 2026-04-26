package com.sranker.shoppinglistmanager.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_history",
    indices = [Index(value = ["name"], unique = true)]
)
data class ItemHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
