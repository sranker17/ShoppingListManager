package com.sranker.shoppinglistmanager.data.repository

import androidx.room.withTransaction
import com.sranker.shoppinglistmanager.data.db.ArchiveSession
import com.sranker.shoppinglistmanager.data.db.ArchivedItem
import com.sranker.shoppinglistmanager.data.db.ItemHistory
import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import com.sranker.shoppinglistmanager.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepository @Inject constructor(
    private val db: ShopListDatabase,
    private val widgetUpdater: WidgetUpdater
) {
    private val itemDao = db.shoppingItemDao()
    private val historyDao = db.itemHistoryDao()
    private val archiveSessionDao = db.archiveSessionDao()
    private val archivedItemDao = db.archivedItemDao()

    fun getItems(): Flow<List<ShoppingItem>> = itemDao.getAll()

    suspend fun addItem(name: String, quantity: Int): Boolean {
        val added = db.withTransaction {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) return@withTransaction false
            if (itemDao.existsByNameCaseInsensitive(trimmedName)) return@withTransaction false

            val maxSortOrder = itemDao.getMaxSortOrder() ?: 0
            val newItem = ShoppingItem(
                name = trimmedName,
                quantity = quantity,
                sortOrder = maxSortOrder + 1
            )
            itemDao.insert(newItem)
            historyDao.upsert(ItemHistory(name = trimmedName))
            true
        }
        // Push widget update only when an item was actually added.
        if (added) widgetUpdater.update()
        return added
    }

    suspend fun toggleItem(id: Long) {
        itemDao.getById(id)?.let { item ->
            itemDao.update(item.copy(isPurchased = !item.isPurchased))
        }
        widgetUpdater.update()
    }

    suspend fun updateQuantity(id: Long, qty: Int) {
        itemDao.getById(id)?.let { item ->
            itemDao.update(item.copy(quantity = qty))
        }
        // Quantity changes do not affect checked state, so no widget update needed.
    }

    suspend fun deleteItem(id: Long): ShoppingItem? {
        val item = itemDao.getById(id)
        if (item != null) {
            itemDao.delete(item)
            widgetUpdater.update()
        }
        return item
    }

    suspend fun restoreItem(item: ShoppingItem) {
        itemDao.insert(item)
        widgetUpdater.update()
    }

    suspend fun reorderItems(items: List<ShoppingItem>) {
        itemDao.updateAll(items)
        widgetUpdater.update()
    }

    suspend fun archiveSession() {
        db.withTransaction {
            val purchasedItems = itemDao.getPurchasedItems()
            if (purchasedItems.isEmpty()) return@withTransaction

            val sessionId = archiveSessionDao.insert(ArchiveSession())

            val archivedItems = purchasedItems.map {
                ArchivedItem(
                    sessionId = sessionId,
                    name = it.name,
                    quantity = it.quantity
                )
            }
            archivedItemDao.insertAll(archivedItems)
            itemDao.deletePurchasedItems()
        }
        // Archiving removes all purchased items — list may now be fully done.
        widgetUpdater.update()
    }
}
