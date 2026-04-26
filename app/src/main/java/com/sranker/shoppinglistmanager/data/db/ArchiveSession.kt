package com.sranker.shoppinglistmanager.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "archive_sessions")
data class ArchiveSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val createdAt: Long = System.currentTimeMillis()
)
