package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateTimeUtils
import com.example.viewmodel.DayPnlSummary
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

data class CalendarDayItem(
  val dayNumber: Int,
  val dateKey: String,
  val isCurrentMonth: Boolean,
  val pnlSummary: DayPnlSummary?
)

@Composable
fun PnlCalendarView(
  currentMonth: Calendar,
  dailyPnlMap: Map<String, DayPnlSummary>,
  selectedDayKey: String?,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onSelectDay: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val days = remember(currentMonth, dailyPnlMap) {
    generateCalendarDays(currentMonth, dailyPnlMap)
  }

  // Calculate monthly total realized PnL
  val monthlyPnl = remember(days) {
    days.filter { it.isCurrentMonth }
      .mapNotNull { it.pnlSummary?.netPnl }
      .sum()
  }

  val selectedDayTrades = selectedDayKey?.let { dailyPnlMap[it]?.trades } ?: emptyList()
  val selectedDaySummary = selectedDayKey?.let { dailyPnlMap[it] }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("pnl_calendar_card"),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Month Header & Navigation Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(IndigoLightBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Calendar",
              tint = IndigoPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = DateTimeUtils.formatMonthYear(currentMonth),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            val isMonthGreen = monthlyPnl >= 0
            Text(
              text = "Month PnL: ${if (isMonthGreen) "+" else ""}${DateTimeUtils.formatCurrency(monthlyPnl)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = if (monthlyPnl == 0.0) TextMuted else if (isMonthGreen) EmeraldGreen else CrimsonRed
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
              .size(36.dp)
              .testTag("prev_month_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Previous Month",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(
            onClick = onNextMonth,
            modifier = Modifier
              .size(36.dp)
              .testTag("next_month_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Next Month",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Day of Week Header
      val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        weekDays.forEach { dayName ->
          Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(42.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Calendar Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(days) { dayItem ->
          CalendarDayCell(
            day = dayItem,
            isSelected = dayItem.dateKey == selectedDayKey,
            onClick = {
              if (dayItem.isCurrentMonth && dayItem.pnlSummary != null) {
                onSelectDay(dayItem.dateKey)
              }
            }
          )
        }
      }

      // Expanded Day Inspector
      AnimatedVisibility(
        visible = selectedDayKey != null && selectedDayTrades.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .background(SurfaceSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TRADES ON $selectedDayKey",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted
              )
              if (selectedDaySummary != null) {
                val isDayGreen = selectedDaySummary.netPnl >= 0
                Text(
                  text = "Daily Total: ${if (isDayGreen) "+" else ""}${DateTimeUtils.formatCurrency(selectedDaySummary.netPnl)} (${selectedDayTrades.size} trades)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isDayGreen) EmeraldGreen else CrimsonRed
                )
              }
            }

            IconButton(
              onClick = { onSelectDay(null) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Day Detail",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(8.dp))

          selectedDayTrades.forEach { trade ->
            TradeDayItemRow(trade = trade)
            Spacer(modifier = Modifier.height(6.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun CalendarDayCell(
  day: CalendarDayItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val pnl = day.pnlSummary?.netPnl
  val hasTrades = day.pnlSummary != null && day.pnlSummary.trades.isNotEmpty()
  val isProfit = pnl != null && pnl > 0
  val isLoss = pnl != null && pnl < 0

  val bgColor = when {
    !day.isCurrentMonth -> Color.Transparent
    isSelected -> IndigoLightBg
    isProfit -> EmeraldGreenBg
    isLoss -> CrimsonRedBg
    hasTrades -> SurfaceSubtle
    else -> Color.Transparent
  }

  val textColor = when {
    !day.isCurrentMonth -> TextMuted.copy(alpha = 0.3f)
    isProfit -> EmeraldGreenDark
    isLoss -> CrimsonRed
    isSelected -> IndigoPrimary
    else -> TextPrimary
  }

  Box(
    modifier = Modifier
      .height(44.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .then(
        if (isSelected) Modifier.border(1.5.dp, IndigoPrimary, RoundedCornerShape(8.dp))
        else if (hasTrades) Modifier.border(0.5.dp, BorderSubtle, RoundedCornerShape(8.dp))
        else Modifier
      )
      .clickable(enabled = day.isCurrentMonth && hasTrades) { onClick() }
      .padding(2.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = if (day.dayNumber > 0) day.dayNumber.toString() else "",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (hasTrades || isSelected) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
        fontSize = 12.sp
      )

      if (day.isCurrentMonth && pnl != null) {
        val shortPnlText = if (abs(pnl) >= 1000) {
          String.format(Locale.US, "%s%.0fk", if (pnl > 0) "+" else "-", abs(pnl) / 1000)
        } else {
          String.format(Locale.US, "%s%.0f", if (pnl > 0) "+" else "-", abs(pnl))
        }
        Text(
          text = shortPnlText,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.ExtraBold,
          color = if (isProfit) EmeraldGreenDark else CrimsonRed,
          fontSize = 9.sp,
          lineHeight = 10.sp
        )
      } else if (hasTrades && day.isCurrentMonth) {
        // Open trade indicator
        Box(
          modifier = Modifier
            .size(4.dp)
            .background(IndigoPrimary, CircleShape)
        )
      }
    }
  }
}

@Composable
private fun TradeDayItemRow(trade: TradeEntity) {
  val isLong = trade.tradeType == TradeType.LONG
  val pnl = trade.realizedPnl ?: 0.0
  val isWin = pnl >= 0

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = SurfaceWhite,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .background(if (isLong) EmeraldGreenBg else CrimsonRedBg, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (isLong) EmeraldGreen else CrimsonRed,
            modifier = Modifier.size(14.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = trade.symbol,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isLong) EmeraldGreenBg else CrimsonRedBg
            ) {
              Text(
                text = trade.tradeType.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isLong) EmeraldGreenDark else CrimsonRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                fontSize = 9.sp
              )
            }
          }
          Text(
            text = "${DateTimeUtils.formatTime(trade.openTimestamp)} • ${trade.strategyTag}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 11.sp
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        if (trade.realizedPnl != null) {
          Text(
            text = "${if (isWin) "+" else ""}${DateTimeUtils.formatCurrency(pnl)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isWin) EmeraldGreen else CrimsonRed
          )
          if (trade.realizedPnlPercent != null) {
            Text(
              text = DateTimeUtils.formatPercent(trade.realizedPnlPercent),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = if (isWin) EmeraldGreen else CrimsonRed,
              fontSize = 11.sp
            )
          }
        } else {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = IndigoLightBg
          ) {
            Text(
              text = "OPEN",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = IndigoDark,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}

private fun generateCalendarDays(
  monthCalendar: Calendar,
  dailyPnlMap: Map<String, DayPnlSummary>
): List<CalendarDayItem> {
  val items = mutableListOf<CalendarDayItem>()
  val cal = Calendar.getInstance().apply {
    timeInMillis = monthCalendar.timeInMillis
    set(Calendar.DAY_OF_MONTH, 1)
  }

  val year = cal.get(Calendar.YEAR)
  val month = cal.get(Calendar.MONTH)
  val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 for Sunday
  val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

  // Empty padding cells for days before start of month
  for (i in 1 until firstDayOfWeek) {
    items.add(
      CalendarDayItem(
        dayNumber = 0,
        dateKey = "",
        isCurrentMonth = false,
        pnlSummary = null
      )
    )
  }

  // Days of current month
  for (day in 1..maxDaysInMonth) {
    cal.set(Calendar.DAY_OF_MONTH, day)
    val key = DateTimeUtils.getDayKey(cal)
    val summary = dailyPnlMap[key]

    items.add(
      CalendarDayItem(
        dayNumber = day,
        dateKey = key,
        isCurrentMonth = true,
        pnlSummary = summary
      )
    )
  }

  // Padding to complete trailing row (multiple of 7)
  while (items.size % 7 != 0) {
    items.add(
      CalendarDayItem(
        dayNumber = 0,
        dateKey = "",
        isCurrentMonth = false,
        pnlSummary = null
      )
    )
  }

  return items
}
