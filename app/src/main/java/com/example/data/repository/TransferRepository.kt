package com.example.data.repository

import com.example.data.local.TransferDao
import com.example.data.model.TransferRecord
import kotlinx.coroutines.flow.Flow

class TransferRepository(private val transferDao: TransferDao) {
    val allTransfers: Flow<List<TransferRecord>> = transferDao.getAllTransfers()
    val totalOutgoing: Flow<Long?> = transferDao.getTotalOutgoing()
    val totalIncoming: Flow<Long?> = transferDao.getTotalIncoming()

    suspend fun addTransfer(record: TransferRecord): Long =
        transferDao.insertTransfer(record)

    suspend fun deleteTransfer(id: Long) =
        transferDao.deleteTransferById(id)

    suspend fun deleteTransfers(ids: List<Long>) =
        transferDao.deleteTransfersByIds(ids)

    suspend fun clearAll() =
        transferDao.deleteAllTransfers()
}
