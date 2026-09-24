package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransferRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_records ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records WHERE type = :type ORDER BY timestamp DESC")
    fun getTransfersByType(type: String): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records WHERE operatorType = :operator ORDER BY timestamp DESC")
    fun getTransfersByOperator(operator: String): Flow<List<TransferRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(record: TransferRecord): Long

    @Query("DELETE FROM transfer_records WHERE id = :id")
    suspend fun deleteTransferById(id: Long)

    @Query("DELETE FROM transfer_records WHERE id IN (:ids)")
    suspend fun deleteTransfersByIds(ids: List<Long>)

    @Query("DELETE FROM transfer_records")
    suspend fun deleteAllTransfers()

    @Query("SELECT SUM(amount) FROM transfer_records WHERE type = 'OUTGOING'")
    fun getTotalOutgoing(): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transfer_records WHERE type = 'INCOMING'")
    fun getTotalIncoming(): Flow<Long?>
}
