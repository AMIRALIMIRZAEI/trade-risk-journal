package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChecklistItemEntity
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary

@Composable
fun ChecklistDialog(
  itemToEdit: ChecklistItemEntity? = null,
  onDismiss: () -> Unit,
  onSave: (title: String, category: String) -> Unit
) {
  var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
  var selectedCategory by remember { mutableStateOf(itemToEdit?.category ?: "Analysis") }
  val categories = listOf("Analysis", "Risk", "Execution", "Psychology")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (itemToEdit == null) "Add Pre-Trade Rule" else "Edit Rule",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Checkpoint / Rule") },
          placeholder = { Text("e.g. Risk strictly <= 2%, Trend confirmed") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("checklist_title_input"),
          maxLines = 2,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))

        categories.forEach { category ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            RadioButton(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = category,
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimary
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(title, selectedCategory)
          }
        },
        enabled = title.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
        modifier = Modifier.testTag("save_checklist_rule_button")
      ) {
        Text("Save Rule")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cancel")
      }
    },
    containerColor = SurfaceWhite,
    shape = RoundedCornerShape(20.dp)
  )
}
