package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

  @Query("SELECT * FROM checklist_items ORDER BY orderIndex ASC, id ASC")
  fun getAllChecklistItems(): Flow<List<ChecklistItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChecklistItem(item: ChecklistItemEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<ChecklistItemEntity>)

  @Update
  suspend fun updateChecklistItem(item: ChecklistItemEntity)

  @Delete
  suspend fun deleteChecklistItem(item: ChecklistItemEntity)

  @Query("DELETE FROM checklist_items WHERE id = :id")
  suspend fun deleteById(id: Long)
}
