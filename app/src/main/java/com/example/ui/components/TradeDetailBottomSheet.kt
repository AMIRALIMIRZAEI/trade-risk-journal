package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateTimeUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailBottomSheet(
  trade: TradeEntity,
  sheetState: SheetState,
  checklistItems: List<ChecklistItemEntity>,
  onDismiss: () -> Unit,
  onCloseTradeClick: () -> Unit,
  onEditTradeClick: () -> Unit,
  onDeleteTradeClick: () -> Unit
) {
  var showFullScreenImage by remember { mutableStateOf(false) }
  val isLong = trade.tradeType == TradeType.LONG
  val isOpen = trade.status == TradeStatus.OPEN
  val pnl = trade.realizedPnl ?: 0.0
  val isWin = pnl >= 0

  val checkedIds = remember(trade.completedChecklistIds) {
    trade.completedChecklistIds.split(",")
      .mapNotNull { it.trim().toLongOrNull() }
      .toSet()
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SurfaceWhite
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header: Symbol, Status & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(if (isLong) EmeraldGreenBg else CrimsonRedBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
              contentDescription = null,
              tint = if (isLong) EmeraldGreen else CrimsonRed,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = trade.symbol,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isLong) EmeraldGreenBg else CrimsonRedBg
              ) {
                Text(
                  text = trade.tradeType.name,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isLong) EmeraldGreenDark else CrimsonRed,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Logged on ${DateTimeUtils.formatDateTime(trade.openTimestamp)}",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEditTradeClick,
            modifier = Modifier.testTag("edit_trade_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Trade", tint = IndigoPrimary)
          }

          IconButton(
            onClick = onDeleteTradeClick,
            modifier = Modifier.testTag("delete_trade_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // PnL or Open Status Banner
      if (isOpen) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = IndigoLightBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LockClock, contentDescription = null, tint = IndigoPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Position is Active / Open",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = IndigoDark
                )
                Text(
                  text = "Target Profit: +${DateTimeUtils.formatCurrency(trade.potentialProfit)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }
            }

            Button(
              onClick = onCloseTradeClick,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
              modifier = Modifier.testTag("close_trade_from_detail_button")
            ) {
              Text("Close Trade", fontSize = 12.sp)
            }
          }
        }
      } else {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isWin) EmeraldGreenBg else CrimsonRedBg,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isWin) "REALIZED WIN" else "REALIZED LOSS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWin) EmeraldGreenDark else CrimsonRed
              )
              Text(
                text = "${if (isWin) "+" else ""}${DateTimeUtils.formatCurrency(pnl)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWin) EmeraldGreenDark else CrimsonRed
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              if (trade.realizedPnlPercent != null) {
                Text(
                  text = DateTimeUtils.formatPercent(trade.realizedPnlPercent),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isWin) EmeraldGreenDark else CrimsonRed
                )
              }
              if (trade.exitPrice != null) {
                Text(
                  text = "Exit @ ${trade.exitPrice}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Key Trade Numbers Grid
      Text(
        text = "TRADE PARAMETERS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = TextMuted
      )
      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceSubtle,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ParameterItem(label = "Entry Price", value = "${trade.entryPrice}")
            ParameterItem(label = "Stop Loss", value = "${trade.stopLoss} (-${DateTimeUtils.formatNumber(trade.stopLossPercent, 1)}%)", valueColor = CrimsonRed)
            ParameterItem(label = "Take Profit", value = "${trade.takeProfit} (+${DateTimeUtils.formatNumber(trade.takeProfitPercent, 1)}%)", valueColor = EmeraldGreen)
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ParameterItem(label = "Margin Allocated", value = DateTimeUtils.formatCurrency(trade.marginAmount))
            ParameterItem(label = "Leverage", value = "${trade.leverage}x")
            ParameterItem(label = "Notional Size", value = DateTimeUtils.formatCurrency(trade.notionalSize))
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ParameterItem(label = "Risk / Trade", value = "${DateTimeUtils.formatNumber(trade.riskPercentage, 1)}% (${DateTimeUtils.formatCurrency(trade.riskAmount)})")
            ParameterItem(label = "Risk:Reward (R:R)", value = "1 : ${DateTimeUtils.formatNumber(trade.riskRewardRatio, 2)}", valueColor = IndigoPrimary)
            ParameterItem(label = "Setup Tag", value = trade.strategyTag)
          }
        }
      }

      // Strategy Notes
      if (trade.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "STRATEGY RATIONALE & NOTES",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceSubtle,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = trade.notes,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      // Pre-Trade Checklist Verification
      if (checklistItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(18.dp))
        val passedCount = checkedIds.size
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PRE-TRADE CHECKLIST",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted
          )
          Text(
            text = "$passedCount/${checklistItems.size} Verified",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (passedCount == checklistItems.size) EmeraldGreen else IndigoPrimary
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceSubtle,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            checklistItems.forEach { item ->
              val isChecked = checkedIds.contains(item.id)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Icon(
                  imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Check,
                  contentDescription = null,
                  tint = if (isChecked) EmeraldGreen else TextMuted.copy(alpha = 0.4f),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isChecked) TextPrimary else TextMuted
                )
              }
            }
          }
        }
      }

      // Chart Screenshot Attachment
      if (trade.imageUri != null) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "ATTACHED CHART SCREENSHOT",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceSubtle)
            .clickable { showFullScreenImage = true }
        ) {
          val context = LocalContext.current
          val imageFile = remember(trade.imageUri) { File(trade.imageUri) }
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(if (imageFile.exists()) imageFile else trade.imageUri)
              .crossfade(true)
              .build(),
            contentDescription = "Trade Chart",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }

  // Full Screen Image Dialog on tap
  if (showFullScreenImage && trade.imageUri != null) {
    Dialog(onDismissRequest = { showFullScreenImage = false }) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(SurfaceWhite)
          .clickable { showFullScreenImage = false }
          .padding(8.dp)
      ) {
        val context = LocalContext.current
        val imageFile = remember(trade.imageUri) { File(trade.imageUri) }
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(if (imageFile.exists()) imageFile else trade.imageUri)
            .crossfade(true)
            .build(),
          contentDescription = "Full chart",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
private fun ParameterItem(
  label: String,
  value: String,
  valueColor: Color = TextPrimary
) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      fontSize = 10.sp
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = valueColor,
      fontSize = 13.sp
    )
  }
}
