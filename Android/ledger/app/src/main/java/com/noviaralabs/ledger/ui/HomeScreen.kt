package com.noviaralabs.ledger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noviaralabs.ledger.data.Txn
import com.noviaralabs.ledger.data.User
import com.noviaralabs.ledger.data.UserBank
import com.noviaralabs.ledger.ui.theme.Charcoal
import com.noviaralabs.ledger.ui.theme.Cream
import com.noviaralabs.ledger.ui.theme.CreditGreen
import com.noviaralabs.ledger.ui.theme.DebitRed
import com.noviaralabs.ledger.ui.theme.Sand
import com.noviaralabs.ledger.ui.theme.WarmWhite

@Composable
fun HomeScreen(state: UiState, vm: LedgerViewModel) {
    val d = state.dashboard
    var showAdd by remember { mutableStateOf(false) }
    var txnId by remember { mutableStateOf<Long?>(null) }
    var bankId by remember { mutableStateOf<Long?>(null) }

    val selectedTxn = d?.transactions?.find { it.transactionId == txnId }
    val selectedBank = d?.banks?.find { it.bankUserId == bankId }

    Box(Modifier.fillMaxSize().background(Cream)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item { Header(d?.user, state.lastUpdated, state.loading) { vm.refresh() } }

            item { SectionTitle("Banks") }
            if (d == null || d.banks.isEmpty()) {
                item { EmptyText(if (d == null) "Nothing loaded yet. Tap refresh." else "No banks linked. Tap + to add one.") }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(d.banks, key = { it.bankUserId }) { b -> BankCard(b) { bankId = b.bankUserId } }
                    }
                }
            }

            item { SectionTitle("Transactions") }
            if (d != null && d.transactions.isEmpty()) {
                item { EmptyText("No transactions yet.") }
            }
            if (d != null) {
                items(d.transactions, key = { it.transactionId }) { t -> TxnRow(t) { txnId = t.transactionId } }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = Charcoal,
            contentColor = Cream,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Add") }
    }

    if (showAdd && d != null) AddSheet(state, vm) { showAdd = false }
    if (selectedTxn != null) TxnSheet(selectedTxn, state, vm) { txnId = null }
    if (selectedBank != null) BankDialog(selectedBank, state.loading, vm) { bankId = null }
}

@Composable
private fun Header(user: User?, lastUpdated: Long?, loading: Boolean, onRefresh: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(user?.name ?: "Ledger", style = MaterialTheme.typography.titleLarge, color = Cream, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(user?.mobileNumber ?: "", style = MaterialTheme.typography.bodyMedium, color = Sand)
        }
        Text(
            if (lastUpdated != null) "Updated\n${fmtMillis(lastUpdated)}" else "Not updated",
            style = MaterialTheme.typography.bodySmall,
            color = Sand,
            textAlign = TextAlign.End
        )
        IconButton(onClick = onRefresh, enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Sand)
            else Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Cream)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
private fun BankCard(bank: UserBank, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        modifier = Modifier.width(170.dp).clip(shape).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(bank.bankName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(rupees(bank.currentBalance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TxnRow(t: Txn, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    signedRupees(t.amount, t.isCredit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (t.isCredit) CreditGreen else DebitRed
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${fmtDate(t.transactionDate)} • ${t.bankName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(color = Sand.copy(alpha = 0.5f), shape = RoundedCornerShape(50)) {
                Text(
                    t.category,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
