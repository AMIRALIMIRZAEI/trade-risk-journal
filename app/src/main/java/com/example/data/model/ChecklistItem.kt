package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: String = "Analysis", // "Analysis", "Risk", "Execution", "Psychology"
  val isEnabled: Boolean = true,
  val isRequired: Boolean = true,
  val orderIndex: Int = 0
)
