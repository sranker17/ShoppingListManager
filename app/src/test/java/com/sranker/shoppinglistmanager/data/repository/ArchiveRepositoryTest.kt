package com.sranker.shoppinglistmanager.data.repository

import androidx.room.withTransaction
import com.sranker.shoppinglistmanager.data.db.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import app.cash.turbine.test

class ArchiveRepositoryTest {

    private lateinit var db: ShopListDatabase
    private lateinit var archiveSessionDao: ArchiveSessionDao
    private lateinit var archivedItemDao: ArchivedItemDao
    private lateinit var shoppingItemDao: ShoppingItemDao
    private lateinit var repository: ArchiveRepository

    @BeforeEach
    fun setup() {
        db = mockk()
        archiveSessionDao = mockk(relaxed = true)
        archivedItemDao = mockk(relaxed = true)
        shoppingItemDao = mockk(relaxed = true)

        every { db.archiveSessionDao() } returns archiveSessionDao
        every { db.archivedItemDao() } returns archivedItemDao
        every { db.shoppingItemDao() } returns shoppingItemDao

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionSlot = slot<suspend () -> Unit>()
        coEvery { (db as androidx.room.RoomDatabase).withTransaction(capture(transactionSlot)) } coAnswers {
            transactionSlot.captured.invoke()
        }

        repository = ArchiveRepository(db)
    }

    @Test
    fun `getSessions returns flow from dao`() = runTest {
        val sessions = listOf(ArchiveSession(1, "Session", 100L))
        every { archiveSessionDao.getAll() } returns flowOf(sessions)

        repository.getSessions().test {
            assertEquals(sessions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getItems returns flow from dao`() = runTest {
        val items = listOf(ArchivedItem(1, 1L, "Apple", 2))
        every { archivedItemDao.getBySession(1L) } returns flowOf(items)

        repository.getItems(1L).test {
            assertEquals(items, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `renameSession updates name in dao`() = runTest {
        repository.renameSession(1L, "New Name")

        coVerify { archiveSessionDao.rename(1L, "New Name") }
    }

    @Test
    fun `reloadSession appends items to shopping list`() = runTest {
        val archivedItems = listOf(ArchivedItem(1, 1L, "Apple", 2))
        coEvery { archivedItemDao.getItemsForSession(1L) } returns archivedItems
        coEvery { shoppingItemDao.getMaxSortOrder() } returns 10

        repository.reloadSession(1L)

        coVerify {
            shoppingItemDao.insert(ShoppingItem(name = "Apple", quantity = 2, isPurchased = false, sortOrder = 11))
        }
    }
}
