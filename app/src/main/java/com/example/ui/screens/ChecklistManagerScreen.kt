package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChecklistItemEntity
import com.example.ui.components.ChecklistDialog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradeViewModel

@Composable
fun ChecklistManagerScreen(
  viewModel: TradeViewModel,
  modifier: Modifier = Modifier
) {
  val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()
  var itemToEdit by remember { mutableStateOf<ChecklistItemEntity?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LightBg)
      .testTag("checklist_manager_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Bento Tile
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(IndigoLightBg, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.FormatListNumbered,
                  contentDescription = null,
                  tint = IndigoPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Trading Discipline Rules",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Slate900
                )
                Text(
                  text = "Customizable Pre-Trade Checklist",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate400
                )
              }
            }

            Button(
              onClick = { showAddDialog = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier.testTag("add_checklist_rule_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Rule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Enforce systematic execution. Checkpoints are automatically integrated into your trade entry logger to prevent impulsive trades.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )
        }
      }
    }

    // 2. Rules List Bento Cards
    items(checklistItems, key = { it.id }) { item ->
      ChecklistRuleItemCard(
        item = item,
        onToggle = { isEnabled ->
          viewModel.updateChecklistItem(item.copy(isEnabled = isEnabled))
        },
        onEdit = { itemToEdit = item },
        onDelete = { viewModel.deleteChecklistItem(item) }
      )
    }
  }

  // Add Rule Dialog
  if (showAddDialog) {
    ChecklistDialog(
      onDismiss = { showAddDialog = false },
      onSave = { title, category ->
        viewModel.addChecklistItem(title, category)
        showAddDialog = false
      }
    )
  }

  // Edit Rule Dialog
  itemToEdit?.let { item ->
    ChecklistDialog(
      itemToEdit = item,
      onDismiss = { itemToEdit = null },
      onSave = { title, category ->
        viewModel.updateChecklistItem(item.copy(title = title, category = category))
        itemToEdit = null
      }
    )
  }
}

@Composable
private fun ChecklistRuleItemCard(
  item: ChecklistItemEntity,
  onToggle: (Boolean) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("checklist_item_${item.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    border = BorderStroke(1.dp, BorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = IndigoLightBg
            ) {
              Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IndigoDark,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (item.isEnabled) Slate900 else Slate400
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        Switch(
          checked = item.isEnabled,
          onCheckedChange = onToggle,
          colors = SwitchDefaults.colors(
            checkedThumbColor = SurfaceWhite,
            checkedTrackColor = EmeraldGreen
          )
        )
      }
    }
  }
}
