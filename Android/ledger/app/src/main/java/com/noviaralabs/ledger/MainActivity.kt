package com.noviaralabs.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noviaralabs.ledger.ui.HomeScreen
import com.noviaralabs.ledger.ui.LedgerViewModel
import com.noviaralabs.ledger.ui.LoginScreen
import com.noviaralabs.ledger.ui.theme.Cream
import com.noviaralabs.ledger.ui.theme.LedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), // light icons on charcoal header
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent { LedgerTheme { LedgerApp() } }
    }
}

@Composable
fun LedgerApp(vm: LedgerViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        if (state.loggedIn) HomeScreen(state, vm) else LoginScreen(state, vm)
        SnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp)
        )
    }
}
