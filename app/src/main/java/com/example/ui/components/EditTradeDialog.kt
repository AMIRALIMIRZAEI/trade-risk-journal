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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.util.DateTimeUtils
import com.example.util.RiskCalculator
import kotlin.math.abs

@Composable
fun EditTradeDialog(
  trade: TradeEntity,
  onDismiss: () -> Unit,
  onConfirmUpdate: (updatedTrade: TradeEntity) -> Unit
) {
  var symbol by remember { mutableStateOf(trade.symbol) }
  var tradeType by remember { mutableStateOf(trade.tradeType) }
  var entryPriceText by remember { mutableStateOf(trade.entryPrice.toString()) }
  var stopLossText by remember { mutableStateOf(trade.stopLoss.toString()) }
  var takeProfitText by remember { mutableStateOf(trade.takeProfit.toString()) }
  var marginText by remember { mutableStateOf(trade.marginAmount.toString()) }
  var leverageText by remember { mutableStateOf(trade.leverage.toInt().toString()) }
  var strategyTag by remember { mutableStateOf(trade.strategyTag) }
  var notes by remember { mutableStateOf(trade.notes) }
  var exitPriceText by remember { mutableStateOf(trade.exitPrice?.toString() ?: "") }

  val entryPrice = entryPriceText.toDoubleOrNull() ?: trade.entryPrice
  val stopLoss = stopLossText.toDoubleOrNull() ?: trade.stopLoss
  val takeProfit = takeProfitText.toDoubleOrNull() ?: trade.takeProfit
  val margin = marginText.toDoubleOrNull() ?: trade.marginAmount
  val leverage = leverageText.toDoubleOrNull() ?: trade.leverage
  val exitPrice = exitPriceText.toDoubleOrNull()

  val isLong = tradeType == TradeType.LONG

  val slPercent = remember(entryPrice, stopLoss) {
    RiskCalculator.calculateStopLossPercent(entryPrice, stopLoss)
  }

  val tpPercent = remember(entryPrice, takeProfit) {
    RiskCalculator.calculateTakeProfitPercent(entryPrice, takeProfit)
  }

  val riskRewardRatio = remember(tradeType, entryPrice, stopLoss, takeProfit) {
    RiskCalculator.calculateRiskReward(tradeType, entryPrice, stopLoss, takeProfit)
  }

  val riskAmount = remember(margin, leverage, slPercent) {
    margin * leverage * (slPercent / 100.0)
  }

  val potentialProfit = remember(margin, leverage, tpPercent) {
    margin * leverage * (tpPercent / 100.0)
  }

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
              .size(34.dp)
              .background(IndigoLightBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = IndigoPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Edit Trade #${trade.id}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Text(
              text = "Modify entry, targets or notes",
              style = MaterialTheme.typography.bodySmall,
              color = Slate400
            )
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Trade Direction Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = tradeType == TradeType.LONG,
            onClick = { tradeType = TradeType.LONG },
            label = { Text("LONG (BUY)", fontWeight = FontWeight.Bold) },
            leadingIcon = {
              Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldGreenBg,
              selectedLabelColor = EmeraldGreenDark,
              selectedLeadingIconColor = EmeraldGreenDark
            ),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )

          FilterChip(
            selected = tradeType == TradeType.SHORT,
            onClick = { tradeType = TradeType.SHORT },
            label = { Text("SHORT (SELL)", fontWeight = FontWeight.Bold) },
            leadingIcon = {
              Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CrimsonRedBg,
              selectedLabelColor = CrimsonRed,
              selectedLeadingIconColor = CrimsonRed
            ),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        // Symbol & Strategy Tag
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it.uppercase() },
            label = { Text("Symbol") },
            modifier = Modifier
              .weight(1f)
              .testTag("edit_symbol_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = strategyTag,
            onValueChange = { strategyTag = it },
            label = { Text("Strategy") },
            modifier = Modifier
              .weight(1f)
              .testTag("edit_strategy_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        // Entry, Stop Loss, Take Profit
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = entryPriceText,
            onValueChange = { entryPriceText = it },
            label = { Text("Entry ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("edit_entry_price_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = stopLossText,
            onValueChange = { stopLossText = it },
            label = { Text("Stop Loss ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("edit_stop_loss_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = takeProfitText,
            onValueChange = { takeProfitText = it },
            label = { Text("Take Profit ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("edit_take_profit_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        // Margin & Leverage
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = marginText,
            onValueChange = { marginText = it },
            label = { Text("Margin ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("edit_margin_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = leverageText,
            onValueChange = { leverageText = it },
            label = { Text("Leverage (x)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .weight(1f)
              .testTag("edit_leverage_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        // If trade is closed, allow editing exit price
        if (trade.status != TradeStatus.OPEN) {
          OutlinedTextField(
            value = exitPriceText,
            onValueChange = { exitPriceText = it },
            label = { Text("Exit Price ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_exit_price_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
        }

        // Live Risk/Reward Summary Box
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceSubtle,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("RISK (SL)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
              Text(
                "${DateTimeUtils.formatNumber(slPercent, 1)}% ($${DateTimeUtils.formatNumber(riskAmount, 2)})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonRed
              )
            }
            Column {
              Text("REWARD (TP)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
              Text(
                "${DateTimeUtils.formatNumber(tpPercent, 1)}% ($${DateTimeUtils.formatNumber(potentialProfit, 2)})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen
              )
            }
            Column {
              Text("R:R RATIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
              Text(
                "1 : ${DateTimeUtils.formatNumber(riskRewardRatio, 2)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IndigoPrimary
              )
            }
          }
        }

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes / Execution Log") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_notes_input"),
          maxLines = 3,
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          var updatedRealizedPnl = trade.realizedPnl
          var updatedRealizedPnlPercent = trade.realizedPnlPercent
          var updatedStatus = trade.status

          if (trade.status != TradeStatus.OPEN && exitPrice != null && exitPrice > 0) {
            val (pnlDollars, pnlPercent) = RiskCalculator.calculateRealizedPnl(
              tradeType = tradeType,
              entryPrice = entryPrice,
              exitPrice = exitPrice,
              margin = margin,
              leverage = leverage
            )
            updatedRealizedPnl = pnlDollars
            updatedRealizedPnlPercent = pnlPercent
            updatedStatus = when {
              abs(pnlDollars) < 0.01 -> TradeStatus.CLOSED_BE
              pnlDollars > 0 -> TradeStatus.CLOSED_WIN
              else -> TradeStatus.CLOSED_LOSS
            }
          }

          val updatedTrade = trade.copy(
            symbol = symbol.ifBlank { "BTC/USDT" },
            tradeType = tradeType,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            marginAmount = margin,
            leverage = leverage,
            strategyTag = strategyTag.ifBlank { "General" },
            notes = notes,
            exitPrice = if (trade.status != TradeStatus.OPEN) exitPrice else trade.exitPrice,
            realizedPnl = updatedRealizedPnl,
            realizedPnlPercent = updatedRealizedPnlPercent,
            status = updatedStatus
          )

          onConfirmUpdate(updatedTrade)
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
        modifier = Modifier.testTag("confirm_save_edit_trade_button")
      ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Changes", fontWeight = FontWeight.Bold)
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
    shape = RoundedCornerShape(22.dp)
  )
}
