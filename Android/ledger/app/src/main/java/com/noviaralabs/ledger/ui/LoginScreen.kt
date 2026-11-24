package com.noviaralabs.ledger.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.noviaralabs.ledger.ui.theme.Cream
import com.noviaralabs.ledger.ui.theme.Sand
import com.noviaralabs.ledger.ui.theme.Charcoal
import com.noviaralabs.ledger.ui.theme.WarmWhite

@Composable
fun LoginScreen(state: UiState, vm: LedgerViewModel) {
    val activity = LocalContext.current as Activity
    var number by rememberSaveable { mutableStateOf("") }
    var manual by rememberSaveable { mutableStateOf(false) } // true when the hint API failed / was dismissed
    var name by rememberSaveable { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        try {
            number = Identity.getSignInClient(activity).getPhoneNumberFromIntent(res.data)
            manual = false
        } catch (e: Exception) {
            manual = true
        }
    }

    fun pickNumber() {
        Identity.getSignInClient(activity)
            .getPhoneNumberHintIntent(GetPhoneNumberHintIntentRequest.builder().build())
            .addOnSuccessListener { pending ->
                launcher.launch(IntentSenderRequest.Builder(pending).build())
            }
            .addOnFailureListener { manual = true }
    }

    LaunchedEffect(Unit) { if (number.isBlank()) pickNumber() }

    Column(Modifier.fillMaxSize().background(Cream)) {
        Box(Modifier.fillMaxWidth().background(Charcoal).statusBarsPadding().padding(24.dp)) {
            Column {
                Text("Ledger", style = MaterialTheme.typography.headlineMedium, color = Cream, fontWeight = FontWeight.Bold)
                Text("Your banks and transactions, in one place", color = Sand, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Column(Modifier.padding(24.dp).imePadding()) {
            Text("Login", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = number,
                onValueChange = { if (manual) number = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } },
                readOnly = !manual,
                label = { Text("Mobile number") },
                placeholder = { Text("+91XXXXXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { pickNumber() }) { Text("Select number from device") }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.login(number) },
                enabled = number.isNotBlank() && !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Cream)
                else Text("Login")
            }
        }
    }

    if (state.askRegister) {
        AlertDialog(
            onDismissRequest = { vm.dismissRegister() },
            containerColor = WarmWhite,
            title = { Text("Create account?") },
            text = {
                Column {
                    Text("$number isn't registered yet. Would you like to create an account and log in?")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.register(number, name) }, enabled = name.isNotBlank() && !state.loading) {
                    Text("Create & login")
                }
            },
            dismissButton = { TextButton(onClick = { vm.dismissRegister() }) { Text("Cancel") } }
        )
    }
}
