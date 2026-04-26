package com.sranker.shoppinglistmanager.data.repository

import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepository @Inject constructor(
    private val db: ShopListDatabase
) {
    private val historyDao = db.itemHistoryDao()

    fun getSuggestions(prefix: String): Flow<List<String>> {
        return historyDao.searchByPrefix(prefix)
    }
}
