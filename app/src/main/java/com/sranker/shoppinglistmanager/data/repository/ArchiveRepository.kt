package com.sranker.shoppinglistmanager.data.repository

import androidx.room.withTransaction
import com.sranker.shoppinglistmanager.data.db.ArchiveSession
import com.sranker.shoppinglistmanager.data.db.ArchivedItem
import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import com.sranker.shoppinglistmanager.data.db.ShoppingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepository @Inject constructor(
    private val db: ShopListDatabase
) {
    private val archiveSessionDao = db.archiveSessionDao()
    private val archivedItemDao = db.archivedItemDao()
    private val shoppingItemDao = db.shoppingItemDao()

    fun getSessions(): Flow<List<ArchiveSession>> = archiveSessionDao.getAll()

    fun getItems(sessionId: Long): Flow<List<ArchivedItem>> = archivedItemDao.getBySession(sessionId)

    suspend fun renameSession(id: Long, name: String) {
        archiveSessionDao.rename(id, name)
    }

    suspend fun reloadSession(sessionId: Long) {
        db.withTransaction {
            val archivedItems = archivedItemDao.getItemsForSession(sessionId)
            val maxSortOrder = shoppingItemDao.getMaxSortOrder() ?: 0
            
            archivedItems.forEachIndexed { index, archivedItem ->
                shoppingItemDao.insert(
                    ShoppingItem(
                        name = archivedItem.name,
                        quantity = archivedItem.quantity,
                        isPurchased = false,
                        sortOrder = maxSortOrder + index + 1
                    )
                )
            }
        }
    }
}
