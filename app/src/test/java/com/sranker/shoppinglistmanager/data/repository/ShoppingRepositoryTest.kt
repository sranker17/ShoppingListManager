package com.sranker.shoppinglistmanager.data.repository

import com.sranker.shoppinglistmanager.data.db.*
import androidx.room.withTransaction
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import app.cash.turbine.test

class ShoppingRepositoryTest {

    private lateinit var db: ShopListDatabase
    private lateinit var itemDao: ShoppingItemDao
    private lateinit var historyDao: ItemHistoryDao
    private lateinit var archiveSessionDao: ArchiveSessionDao
    private lateinit var archivedItemDao: ArchivedItemDao
    private lateinit var repository: ShoppingRepository

    @BeforeEach
    fun setup() {
        db = mockk()
        itemDao = mockk(relaxed = true)
        historyDao = mockk(relaxed = true)
        archiveSessionDao = mockk(relaxed = true)
        archivedItemDao = mockk(relaxed = true)

        every { db.shoppingItemDao() } returns itemDao
        every { db.itemHistoryDao() } returns historyDao
        every { db.archiveSessionDao() } returns archiveSessionDao
        every { db.archivedItemDao() } returns archivedItemDao

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionSlot = slot<suspend () -> Unit>()
        coEvery { (db as androidx.room.RoomDatabase).withTransaction(capture(transactionSlot)) } coAnswers {
            transactionSlot.captured.invoke()
        }

        repository = ShoppingRepository(db)
    }

    @Test
    fun `getItems returns flow from dao`() = runTest {
        val items = listOf(ShoppingItem(1, "Apple", 2, false, 1))
        every { itemDao.getAll() } returns flowOf(items)

        repository.getItems().test {
            assertEquals(items, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `addItem inserts item and adds history`() = runTest {
        coEvery { itemDao.getMaxSortOrder() } returns 5
        
        repository.addItem("Banana", 3)

        coVerify { 
            itemDao.insert(ShoppingItem(name = "Banana", quantity = 3, sortOrder = 6)) 
            historyDao.upsert(ItemHistory(name = "Banana"))
        }
    }

    @Test
    fun `toggleItem updates isPurchased flag`() = runTest {
        val item = ShoppingItem(1, "Apple", 1, false, 1)
        coEvery { itemDao.getById(1) } returns item

        repository.toggleItem(1)

        coVerify { itemDao.update(item.copy(isPurchased = true)) }
    }

    @Test
    fun `updateQuantity updates quantity`() = runTest {
        val item = ShoppingItem(1, "Apple", 1, false, 1)
        coEvery { itemDao.getById(1) } returns item

        repository.updateQuantity(1, 5)

        coVerify { itemDao.update(item.copy(quantity = 5)) }
    }

    @Test
    fun `deleteItem deletes and returns item`() = runTest {
        val item = ShoppingItem(1, "Apple", 1, false, 1)
        coEvery { itemDao.getById(1) } returns item

        val result = repository.deleteItem(1)

        assertEquals(item, result)
        coVerify { itemDao.delete(item) }
    }

    @Test
    fun `restoreItem inserts item`() = runTest {
        val item = ShoppingItem(1, "Apple", 1, false, 1)
        
        repository.restoreItem(item)

        coVerify { itemDao.insert(item) }
    }

    @Test
    fun `reorderItems updates all items`() = runTest {
        val items = listOf(ShoppingItem(1, "Apple", 1, false, 1))
        
        repository.reorderItems(items)

        coVerify { itemDao.updateAll(items) }
    }

    @Test
    fun `archiveSession moves purchased items to archive`() = runTest {
        val purchasedItems = listOf(ShoppingItem(1, "Apple", 2, true, 1))
        coEvery { itemDao.getPurchasedItems() } returns purchasedItems
        coEvery { archiveSessionDao.insert(any()) } returns 100L

        repository.archiveSession()

        coVerify {
            archiveSessionDao.insert(any())
            archivedItemDao.insertAll(listOf(ArchivedItem(sessionId = 100L, name = "Apple", quantity = 2)))
            itemDao.deletePurchasedItems()
        }
    }

    @Test
    fun `archiveSession does nothing if no purchased items`() = runTest {
        coEvery { itemDao.getPurchasedItems() } returns emptyList()

        repository.archiveSession()

        coVerify(exactly = 0) {
            archiveSessionDao.insert(any())
            archivedItemDao.insertAll(any())
            itemDao.deletePurchasedItems()
        }
    }
}
