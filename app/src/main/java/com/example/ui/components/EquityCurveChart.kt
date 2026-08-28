package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateTimeUtils
import com.example.viewmodel.EquityPoint
import kotlin.math.max
import kotlin.math.min

@Composable
fun EquityCurveChart(
  equityPoints: List<EquityPoint>,
  startingCapital: Double,
  modifier: Modifier = Modifier
) {
  var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(equityPoints) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
  }

  val activePoint = selectedPointIndex?.let { idx ->
    if (idx in equityPoints.indices) equityPoints[idx] else null
  } ?: equityPoints.lastOrNull()

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("equity_curve_card"),
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
      // Header with current equity & quick status
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
              imageVector = Icons.Default.ShowChart,
              contentDescription = "Equity Curve",
              tint = IndigoPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Equity Growth Curve",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = if (selectedPointIndex != null) "Scrubbing history" else "Cumulative performance over trades",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        }

        if (activePoint != null) {
          val totalPnl = activePoint.equity - startingCapital
          val totalPnlPercent = if (startingCapital > 0) (totalPnl / startingCapital) * 100.0 else 0.0
          val isPositive = totalPnl >= 0

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isPositive) EmeraldGreenBg else Color(0xFFFEE2E2),
            modifier = Modifier.padding(start = 6.dp)
          ) {
            Text(
              text = "${if (isPositive) "+" else ""}${DateTimeUtils.formatCurrency(totalPnl)} (${DateTimeUtils.formatPercent(totalPnlPercent)})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (isPositive) EmeraldGreen else CrimsonRed,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Active Inspection Box
      if (activePoint != null) {
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SurfaceSubtle,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "EQUITY ON ${activePoint.dateLabel.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = DateTimeUtils.formatCurrency(activePoint.equity),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IndigoDark
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = activePoint.symbol,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Drawdown: ${DateTimeUtils.formatNumber(activePoint.drawdownPercent, 1)}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (activePoint.drawdownPercent > 5) CrimsonRed else TextMuted
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Canvas Interactive Chart
      if (equityPoints.size < 2) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(SurfaceSubtle, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.TrendingUp,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Log trades to build your live equity curve",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .testTag("equity_curve_canvas")
              .pointerInput(equityPoints) {
                detectTapGestures(
                  onPress = { offset ->
                    val pointIndex = calculateNearestPointIndex(offset.x, size.width.toFloat(), equityPoints.size)
                    selectedPointIndex = pointIndex
                  },
                  onTap = { offset ->
                    val pointIndex = calculateNearestPointIndex(offset.x, size.width.toFloat(), equityPoints.size)
                    selectedPointIndex = pointIndex
                  }
                )
              }
              .pointerInput(equityPoints) {
                detectDragGestures(
                  onDragStart = { offset ->
                    selectedPointIndex = calculateNearestPointIndex(offset.x, size.width.toFloat(), equityPoints.size)
                  },
                  onDragEnd = {
                    // Keep the last selected or reset after short delay if desired
                  },
                  onDragCancel = {
                    selectedPointIndex = null
                  },
                  onDrag = { change, _ ->
                    change.consume()
                    selectedPointIndex = calculateNearestPointIndex(change.position.x, size.width.toFloat(), equityPoints.size)
                  }
                )
              }
          ) {
            val width = size.width
            val height = size.height
            val padding = 20f

            val minEquity = min(startingCapital * 0.95, equityPoints.minOf { it.equity } * 0.98)
            val maxEquity = max(startingCapital * 1.05, equityPoints.maxOf { it.equity } * 1.02)
            val equityRange = max(1.0, maxEquity - minEquity)

            fun getX(index: Int): Float {
              return padding + (index.toFloat() / (equityPoints.size - 1)) * (width - 2 * padding)
            }

            fun getY(equity: Double): Float {
              val normalized = (equity - minEquity) / equityRange
              val availableHeight = height - 2 * padding
              return height - padding - (normalized.toFloat() * availableHeight)
            }

            // Draw baseline for starting balance (Dashed line)
            val baselineY = getY(startingCapital)
            drawLine(
              color = BorderSubtle,
              start = Offset(padding, baselineY),
              end = Offset(width - padding, baselineY),
              strokeWidth = 2f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Build curved line path
            val path = Path()
            val fillPath = Path()

            val progress = animProgress.value
            val visibleCount = max(2, (equityPoints.size * progress).toInt())
            val pointsToDraw = equityPoints.take(visibleCount)

            pointsToDraw.forEachIndexed { index, pt ->
              val x = getX(index)
              val y = getY(pt.equity)
              if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - padding)
                fillPath.lineTo(x, y)
              } else {
                val prevX = getX(index - 1)
                val prevY = getY(pointsToDraw[index - 1].equity)
                val controlX1 = prevX + (x - prevX) / 2f
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) / 2f
                val controlY2 = y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
              }
            }

            if (pointsToDraw.isNotEmpty()) {
              val lastX = getX(pointsToDraw.lastIndex)
              fillPath.lineTo(lastX, height - padding)
              fillPath.close()

              // Draw Gradient Area Fill under curve
              drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                  colors = listOf(
                    EmeraldGreenLight.copy(alpha = 0.35f),
                    EmeraldGreenLight.copy(alpha = 0.05f),
                    Color.Transparent
                  ),
                  startY = padding,
                  endY = height - padding
                )
              )

              // Draw Primary Line
              drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                  colors = listOf(IndigoPrimary, EmeraldGreenLight, EmeraldGreen)
                ),
                style = Stroke(
                  width = 4.dp.toPx(),
                  cap = StrokeCap.Round,
                  join = StrokeJoin.Round
                )
              )

              // Draw point dots
              pointsToDraw.forEachIndexed { index, pt ->
                val x = getX(index)
                val y = getY(pt.equity)
                val isSelected = selectedPointIndex == index

                if (isSelected) {
                  // Draw scrubber vertical line
                  drawLine(
                    color = IndigoPrimary.copy(alpha = 0.5f),
                    start = Offset(x, padding),
                    end = Offset(x, height - padding),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                  )
                  // Highlight outer circle
                  drawCircle(
                    color = IndigoPrimary.copy(alpha = 0.2f),
                    radius = 12.dp.toPx(),
                    center = Offset(x, y)
                  )
                  drawCircle(
                    color = IndigoPrimary,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                  )
                  drawCircle(
                    color = SurfaceWhite,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                  )
                } else if (index == pointsToDraw.lastIndex) {
                  // Pulse endpoint
                  drawCircle(
                    color = EmeraldGreen.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                  )
                  drawCircle(
                    color = EmeraldGreen,
                    radius = 4.5.dp.toPx(),
                    center = Offset(x, y)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Baseline: ${DateTimeUtils.formatCurrency(startingCapital)}",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
        Text(
          text = "Touch or drag across chart to inspect trades",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
      }
    }
  }
}

private fun calculateNearestPointIndex(touchX: Float, width: Float, count: Int): Int {
  if (count <= 1 || width <= 0f) return 0
  val padding = 20f
  val effectiveWidth = max(1f, width - 2 * padding)
  val clampedX = (touchX - padding).coerceIn(0f, effectiveWidth)
  val fraction = clampedX / effectiveWidth
  val index = (fraction * (count - 1)).toInt()
  return index.coerceIn(0, count - 1)
}
