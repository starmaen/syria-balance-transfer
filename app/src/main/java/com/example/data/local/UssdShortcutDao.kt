package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UssdShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface UssdShortcutDao {
    @Query("SELECT * FROM ussd_shortcuts ORDER BY id ASC")
    fun getAll(): Flow<List<UssdShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: UssdShortcut): Long

    @Query("DELETE FROM ussd_shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
