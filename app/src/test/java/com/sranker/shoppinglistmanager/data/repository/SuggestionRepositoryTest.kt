package com.sranker.shoppinglistmanager.data.repository

import com.sranker.shoppinglistmanager.data.db.ItemHistoryDao
import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import app.cash.turbine.test

class SuggestionRepositoryTest {

    private lateinit var db: ShopListDatabase
    private lateinit var historyDao: ItemHistoryDao
    private lateinit var repository: SuggestionRepository

    @BeforeEach
    fun setup() {
        db = mockk()
        historyDao = mockk(relaxed = true)
        every { db.itemHistoryDao() } returns historyDao

        repository = SuggestionRepository(db)
    }

    @Test
    fun `getSuggestions returns matching items from dao`() = runTest {
        val suggestions = listOf("Apple", "Apricot")
        every { historyDao.searchByPrefix("Ap") } returns flowOf(suggestions)

        repository.getSuggestions("Ap").test {
            assertEquals(suggestions, awaitItem())
            awaitComplete()
        }
    }
}
