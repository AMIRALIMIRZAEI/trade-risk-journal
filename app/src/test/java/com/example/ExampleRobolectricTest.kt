package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.RiskCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TradeJournal", appName)
  }

  @Test
  fun `verify exact risk management position sizing formula`() {
    // Formula: Margin = (Total Capital * Risk%) / (Leverage * StopLoss%)
    // Total Capital = $10,000, Risk = 1% (0.01), Leverage = 5x, Stop Loss = 2% (0.02)
    // Margin = (10,000 * 0.01) / (5 * 0.02) = 100 / 0.1 = $1000.00
    val margin = RiskCalculator.calculateMarginPositionSize(
      totalCapital = 10000.0,
      riskPercentDecimal = 0.01,
      leverage = 5.0,
      stopLossPercentDecimal = 0.02
    )
    assertEquals(1000.0, margin, 0.001)
  }
}
