package com.example.data.repository

import com.example.data.local.ChecklistDao
import com.example.data.model.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

class ChecklistRepository(private val checklistDao: ChecklistDao) {

  val allChecklistItems: Flow<List<ChecklistItemEntity>> = checklistDao.getAllChecklistItems()

  suspend fun insertChecklistItem(item: ChecklistItemEntity): Long =
    checklistDao.insertChecklistItem(item)

  suspend fun updateChecklistItem(item: ChecklistItemEntity) =
    checklistDao.updateChecklistItem(item)

  suspend fun deleteChecklistItem(item: ChecklistItemEntity) =
    checklistDao.deleteChecklistItem(item)

  suspend fun deleteById(id: Long) = checklistDao.deleteById(id)
}
