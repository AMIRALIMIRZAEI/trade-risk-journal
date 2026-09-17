package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AccountSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_account_preferences")

class UserPreferencesRepository(private val context: Context) {

  private object PreferenceKeys {
    val TOTAL_CAPITAL = doublePreferencesKey("total_capital")
    val LAST_SAVED_EQUITY = doublePreferencesKey("last_saved_equity")
    val DEFAULT_RISK_PERCENT = doublePreferencesKey("default_risk_percent")
    val DEFAULT_LEVERAGE = doublePreferencesKey("default_leverage")
    val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
  }

  val accountSettingsFlow: Flow<AccountSettings> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val totalCapital = preferences[PreferenceKeys.TOTAL_CAPITAL] ?: 10000.0
      val lastSavedEquity = preferences[PreferenceKeys.LAST_SAVED_EQUITY] ?: totalCapital
      val defaultRiskPercent = preferences[PreferenceKeys.DEFAULT_RISK_PERCENT] ?: 1.0
      val defaultLeverage = preferences[PreferenceKeys.DEFAULT_LEVERAGE] ?: 1.0
      val currencySymbol = preferences[PreferenceKeys.CURRENCY_SYMBOL] ?: "$"
      AccountSettings(
        totalCapital = totalCapital,
        lastSavedEquity = lastSavedEquity,
        defaultRiskPercent = defaultRiskPercent,
        defaultLeverage = defaultLeverage,
        currencySymbol = currencySymbol
      )
    }

  suspend fun saveTotalCapital(capital: Double) {
    context.dataStore.edit { preferences ->
      preferences[PreferenceKeys.TOTAL_CAPITAL] = capital
      if (preferences[PreferenceKeys.LAST_SAVED_EQUITY] == null) {
        preferences[PreferenceKeys.LAST_SAVED_EQUITY] = capital
      }
    }
  }

  suspend fun saveLastSavedEquity(equity: Double) {
    context.dataStore.edit { preferences ->
      preferences[PreferenceKeys.LAST_SAVED_EQUITY] = equity
    }
  }

  suspend fun saveSettings(settings: AccountSettings) {
    context.dataStore.edit { preferences ->
      preferences[PreferenceKeys.TOTAL_CAPITAL] = settings.totalCapital
      preferences[PreferenceKeys.LAST_SAVED_EQUITY] = settings.lastSavedEquity
      preferences[PreferenceKeys.DEFAULT_RISK_PERCENT] = settings.defaultRiskPercent
      preferences[PreferenceKeys.DEFAULT_LEVERAGE] = settings.defaultLeverage
      preferences[PreferenceKeys.CURRENCY_SYMBOL] = settings.currencySymbol
    }
  }
}
