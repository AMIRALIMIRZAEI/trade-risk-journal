package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ChecklistManagerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TradeEntryScreen
import com.example.ui.screens.TradeLogScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.TradeViewModel
import kotlinx.coroutines.launch

enum class AppDestination(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val tag: String
) {
  DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
  CALCULATOR("Calculator", Icons.Filled.Calculate, Icons.Outlined.Calculate, "tab_calculator"),
  ENTRY("New Trade", Icons.Filled.PostAdd, Icons.Outlined.PostAdd, "tab_entry"),
  LOG("Journal", Icons.Filled.History, Icons.Outlined.History, "tab_log"),
  CHECKLIST("Discipline Rules", Icons.Filled.Rule, Icons.Filled.Rule, "tab_checklist")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val tradeViewModel: TradeViewModel = viewModel()
        val calculatorViewModel: CalculatorViewModel = viewModel()

        TradingJournalApp(
          tradeViewModel = tradeViewModel,
          calculatorViewModel = calculatorViewModel
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingJournalApp(
  tradeViewModel: TradeViewModel,
  calculatorViewModel: CalculatorViewModel
) {
  var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
  var previousDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(IndigoLightBg, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = currentDestination.selectedIcon,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = currentDestination.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Slate900
            )
          }
        },
        navigationIcon = {
          if (currentDestination == AppDestination.CHECKLIST) {
            IconButton(
              onClick = { currentDestination = previousDestination },
              modifier = Modifier.testTag("checklist_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Slate900
              )
            }
          }
        },
        actions = {
          if (currentDestination != AppDestination.CHECKLIST) {
            IconButton(
              onClick = {
                previousDestination = currentDestination
                currentDestination = AppDestination.CHECKLIST
              },
              modifier = Modifier.testTag("topbar_rules_button")
            ) {
              Icon(
                imageVector = Icons.Default.Checklist,
                contentDescription = "Rules",
                tint = IndigoPrimary
              )
            }
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = SurfaceWhite
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 2.dp,
        modifier = Modifier.testTag("bottom_navigation_bar")
      ) {
        val bottomTabs = listOf(
          AppDestination.DASHBOARD,
          AppDestination.CALCULATOR,
          AppDestination.ENTRY,
          AppDestination.LOG
        )

        bottomTabs.forEach { destination ->
          val selected = currentDestination == destination
          NavigationBarItem(
            selected = selected,
            onClick = {
              currentDestination = destination
            },
            icon = {
              Icon(
                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.title
              )
            },
            label = {
              Text(
                text = destination.title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = IndigoPrimary,
              selectedTextColor = IndigoDark,
              indicatorColor = IndigoLightBg,
              unselectedIconColor = Slate400,
              unselectedTextColor = Slate400
            ),
            modifier = Modifier.testTag(destination.tag)
          )
        }
      }
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(LightBg)
    ) {
      when (currentDestination) {
        AppDestination.DASHBOARD -> {
          DashboardScreen(
            viewModel = tradeViewModel,
            onNavigateToNewTrade = { currentDestination = AppDestination.ENTRY },
            onNavigateToCalculator = { currentDestination = AppDestination.CALCULATOR },
            onNavigateToTradeLog = { currentDestination = AppDestination.LOG }
          )
        }

        AppDestination.CALCULATOR -> {
          CalculatorScreen(
            viewModel = calculatorViewModel,
            onApplyToTradeEntry = {
              currentDestination = AppDestination.ENTRY
            }
          )
        }

        AppDestination.ENTRY -> {
          val prefilledState = calculatorViewModel.transferToTradeState
          TradeEntryScreen(
            viewModel = tradeViewModel,
            prefilledCalculatorState = prefilledState,
            onTradeSaved = {
              calculatorViewModel.clearTransferState()
              currentDestination = AppDestination.LOG
              coroutineScope.launch {
                snackbarHostState.showSnackbar(
                  message = "Trade successfully logged to Journal!",
                  duration = SnackbarDuration.Short
                )
              }
            },
            onNavigateToChecklistManager = {
              previousDestination = AppDestination.ENTRY
              currentDestination = AppDestination.CHECKLIST
            }
          )
        }

        AppDestination.LOG -> {
          TradeLogScreen(
            viewModel = tradeViewModel,
            onNavigateToNewTrade = { currentDestination = AppDestination.ENTRY }
          )
        }

        AppDestination.CHECKLIST -> {
          ChecklistManagerScreen(
            viewModel = tradeViewModel
          )
        }
      }
    }
  }
}
