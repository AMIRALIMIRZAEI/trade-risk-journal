package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class BentoTileStyle {
  WHITE,
  INDIGO_SOFT,
  DARK_SLATE
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  tileStyle: BentoTileStyle = BentoTileStyle.WHITE,
  valueColor: Color? = null,
  iconBgColor: Color = IndigoLightBg,
  iconTintColor: Color = IndigoPrimary,
  badgeText: String? = null,
  badgeColor: Color = IndigoLightBg,
  badgeTextColor: Color = IndigoDark,
  modifier: Modifier = Modifier
) {
  val containerColor = when (tileStyle) {
    BentoTileStyle.WHITE -> SurfaceWhite
    BentoTileStyle.INDIGO_SOFT -> IndigoLightBg
    BentoTileStyle.DARK_SLATE -> Slate900
  }

  val labelColor = when (tileStyle) {
    BentoTileStyle.WHITE -> Slate500
    BentoTileStyle.INDIGO_SOFT -> Color(0xFF818CF8) // Indigo 400
    BentoTileStyle.DARK_SLATE -> Slate400
  }

  val finalValueColor = valueColor ?: when (tileStyle) {
    BentoTileStyle.WHITE -> TextPrimary
    BentoTileStyle.INDIGO_SOFT -> IndigoDark
    BentoTileStyle.DARK_SLATE -> SurfaceWhite
  }

  val subtextColor = when (tileStyle) {
    BentoTileStyle.WHITE -> TextMuted
    BentoTileStyle.INDIGO_SOFT -> Color(0xFF6366F1)
    BentoTileStyle.DARK_SLATE -> Slate400
  }

  Card(
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = if (tileStyle == BentoTileStyle.WHITE) BorderStroke(1.dp, BorderSubtle) else null,
    elevation = CardDefaults.cardElevation(defaultElevation = if (tileStyle == BentoTileStyle.WHITE) 1.dp else 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.Center
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = labelColor,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp,
          fontSize = 10.sp
        )

        if (badgeText != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = badgeTextColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontSize = 10.sp
            )
          }
        } else if (icon != null) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTintColor,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.ExtraBold,
        color = finalValueColor,
        fontSize = 22.sp
      )

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = subtextColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
