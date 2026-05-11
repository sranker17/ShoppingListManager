package com.sranker.shoppinglistmanager.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.sranker.shoppinglistmanager.data.db.ShopListDatabase
import com.sranker.shoppinglistmanager.data.db.ShoppingItemDao
import com.sranker.shoppinglistmanager.data.repository.ArchiveRepository
import com.sranker.shoppinglistmanager.data.repository.SettingsRepository
import com.sranker.shoppinglistmanager.data.repository.ShoppingRepository
import com.sranker.shoppinglistmanager.data.repository.SuggestionRepository
import com.sranker.shoppinglistmanager.widget.WidgetUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShopListDatabase {
        return Room.databaseBuilder(
            context,
            ShopListDatabase::class.java,
            "shopping_list.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provideShoppingItemDao(db: ShopListDatabase): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    @Singleton
    fun provideShoppingRepository(db: ShopListDatabase, widgetUpdater: WidgetUpdater): ShoppingRepository {
        return ShoppingRepository(db, widgetUpdater)
    }

    @Provides
    @Singleton
    fun provideArchiveRepository(db: ShopListDatabase): ArchiveRepository {
        return ArchiveRepository(db)
    }

    @Provides
    @Singleton
    fun provideSuggestionRepository(db: ShopListDatabase): SuggestionRepository {
        return SuggestionRepository(db)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore)
    }
}
