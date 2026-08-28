package com.example.data.model

data class AccountSettings(
  val totalCapital: Double = 10000.0,
  val defaultRiskPercent: Double = 1.0, // 1%
  val defaultLeverage: Double = 1.0,
  val currencySymbol: String = "$"
)
