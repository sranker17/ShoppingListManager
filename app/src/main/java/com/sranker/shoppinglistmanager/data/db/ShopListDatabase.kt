package com.sranker.shoppinglistmanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ShoppingItem::class,
        ArchiveSession::class,
        ArchivedItem::class,
        ItemHistory::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ShopListDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun archiveSessionDao(): ArchiveSessionDao
    abstract fun archivedItemDao(): ArchivedItemDao
    abstract fun itemHistoryDao(): ItemHistoryDao
}
