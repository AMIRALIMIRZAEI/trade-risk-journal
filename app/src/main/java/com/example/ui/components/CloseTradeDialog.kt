package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradeEntity
import com.example.data.model.TradeType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateTimeUtils
import com.example.util.RiskCalculator

@Composable
fun CloseTradeDialog(
  trade: TradeEntity,
  onDismiss: () -> Unit,
  onConfirmClose: (exitPrice: Double, notes: String) -> Unit
) {
  var exitPriceText by remember { mutableStateOf(trade.takeProfit.toString()) }
  var exitNoteText by remember { mutableStateOf("") }

  val exitPrice = exitPriceText.toDoubleOrNull() ?: 0.0
  val isLong = trade.tradeType == TradeType.LONG

  val (pnlDollars, pnlPercent) = remember(exitPrice, trade) {
    if (exitPrice > 0) {
      RiskCalculator.calculateRealizedPnl(
        tradeType = trade.tradeType,
        entryPrice = trade.entryPrice,
        exitPrice = exitPrice,
        margin = trade.marginAmount,
        leverage = trade.leverage
      )
    } else {
      Pair(0.0, 0.0)
    }
  }

  val isWin = pnlDollars > 0
  val isLoss = pnlDollars < 0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(if (isLong) EmeraldGreenBg else CrimsonRedBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
              contentDescription = null,
              tint = if (isLong) EmeraldGreen else CrimsonRed,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Close ${trade.symbol}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "${trade.tradeType.name} @ ${trade.entryPrice}",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Quick Presets: TP or SL
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { exitPriceText = trade.takeProfit.toString() },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("TP (${trade.takeProfit})", fontSize = 11.sp, color = EmeraldGreen)
          }
          OutlinedButton(
            onClick = { exitPriceText = trade.stopLoss.toString() },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("SL (${trade.stopLoss})", fontSize = 11.sp, color = CrimsonRed)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = exitPriceText,
          onValueChange = { exitPriceText = it },
          label = { Text("Exit Price ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("exit_price_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Realized PnL Preview Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isWin) EmeraldGreenBg else if (isLoss) CrimsonRedBg else SurfaceSubtle,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Calculated Realized PnL:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWin) EmeraldGreenDark else if (isLoss) CrimsonRed else TextMuted
              )
              Text(
                text = "${if (isWin) "+" else ""}${DateTimeUtils.formatCurrency(pnlDollars)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWin) EmeraldGreen else if (isLoss) CrimsonRed else TextPrimary
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Return on Margin (${trade.leverage}x Lev):",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
              )
              Text(
                text = DateTimeUtils.formatPercent(pnlPercent),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isWin) EmeraldGreen else if (isLoss) CrimsonRed else TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = exitNoteText,
          onValueChange = { exitNoteText = it },
          label = { Text("Exit Note / Lesson (Optional)") },
          placeholder = { Text("e.g. Trailed stop hit, hit TP1 target") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("exit_note_input"),
          maxLines = 3,
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (exitPrice > 0) {
            onConfirmClose(exitPrice, exitNoteText)
          }
        },
        enabled = exitPrice > 0,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isWin) EmeraldGreen else IndigoPrimary
        ),
        modifier = Modifier.testTag("confirm_close_trade_button")
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm & Close Trade")
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
