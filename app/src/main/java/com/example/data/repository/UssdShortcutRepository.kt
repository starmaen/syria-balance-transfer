package com.example.data.repository

import com.example.data.local.UssdShortcutDao
import com.example.data.model.UssdShortcut
import kotlinx.coroutines.flow.Flow

class UssdShortcutRepository(private val dao: UssdShortcutDao) {
    val all: Flow<List<UssdShortcut>> = dao.getAll()

    suspend fun add(label: String, code: String): Long =
        dao.insert(UssdShortcut(label = label.trim(), code = code.trim()))

    suspend fun delete(id: Long) =
        dao.deleteById(id)
}
