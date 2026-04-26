package com.sranker.shoppinglistmanager.data.repository

import androidx.room.withTransaction
import com.sranker.shoppinglistmanager.data.db.ArchiveSession
import com.sranker.shoppinglistmanager.data.db.ArchivedItem
import com.sranker.shoppinglistmanager.data.db.ItemHistory
import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepository @Inject constructor(
    private val db: ShopListDatabase
) {
    private val itemDao = db.shoppingItemDao()
    private val historyDao = db.itemHistoryDao()
    private val archiveSessionDao = db.archiveSessionDao()
    private val archivedItemDao = db.archivedItemDao()

    fun getItems(): Flow<List<ShoppingItem>> = itemDao.getAll()

    suspend fun addItem(name: String, quantity: Int) {
        db.withTransaction {
            val maxSortOrder = itemDao.getMaxSortOrder() ?: 0
            val newItem = ShoppingItem(
                name = name,
                quantity = quantity,
                sortOrder = maxSortOrder + 1
            )
            itemDao.insert(newItem)
            historyDao.upsert(ItemHistory(name = name))
        }
    }

    suspend fun toggleItem(id: Long) {
        itemDao.getById(id)?.let { item ->
            itemDao.update(item.copy(isPurchased = !item.isPurchased))
        }
    }

    suspend fun updateQuantity(id: Long, qty: Int) {
        itemDao.getById(id)?.let { item ->
            itemDao.update(item.copy(quantity = qty))
        }
    }

    suspend fun deleteItem(id: Long): ShoppingItem? {
        val item = itemDao.getById(id)
        if (item != null) {
            itemDao.delete(item)
        }
        return item
    }

    suspend fun restoreItem(item: ShoppingItem) {
        itemDao.insert(item)
    }

    suspend fun reorderItems(items: List<ShoppingItem>) {
        itemDao.updateAll(items)
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
    }
}
