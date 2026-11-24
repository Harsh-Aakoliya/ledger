package com.noviaralabs.ledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noviaralabs.ledger.data.Bank
import com.noviaralabs.ledger.data.Category
import com.noviaralabs.ledger.data.Txn
import com.noviaralabs.ledger.data.TxnRequest
import com.noviaralabs.ledger.data.UserBank
import com.noviaralabs.ledger.ui.theme.Charcoal
import com.noviaralabs.ledger.ui.theme.CreditGreen
import com.noviaralabs.ledger.ui.theme.DebitRed
import com.noviaralabs.ledger.ui.theme.Sand
import com.noviaralabs.ledger.ui.theme.WarmWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// ---------------------------------------------------------------- add sheet (Transactions | Banks)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSheet(state: UiState, vm: LedgerViewModel, onDismiss: () -> Unit) {
    val d = state.dashboard ?: return
    var tab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WarmWhite,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        TabRow(selectedTabIndex = tab, containerColor = WarmWhite, contentColor = Charcoal) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Transactions") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Banks") })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (tab == 0) {
                if (d.banks.isEmpty()) {
                    Text("Link a bank in the Banks tab first.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    TxnForm(null, d.banks, state.categories, state.loading, "Add transaction") { req ->
                        vm.addTxn(req) { onDismiss() }
                    }
                }
            } else {
                val available = state.banks.filter { b -> d.banks.none { it.bankId == b.bankId } }
                BankForm(available, state.loading) { bankId, opening ->
                    vm.addBank(bankId, opening) { onDismiss() }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- transaction details / edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxnSheet(txn: Txn, state: UiState, vm: LedgerViewModel, onDismiss: () -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WarmWhite,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (editing) {
                Text("Edit transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TxnForm(txn, state.dashboard?.banks.orEmpty(), state.categories, state.loading, "Save changes") { req ->
                    vm.updateTxn(txn.transactionId, req) { editing = false }
                }
            } else {
                Text(
                    signedRupees(txn.amount, txn.isCredit),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (txn.isCredit) CreditGreen else DebitRed
                )
                DetailRow("Category", txn.category)
                DetailRow("Bank", txn.bankName)
                DetailRow("Date", fmtDate(txn.transactionDate))
                DetailRow("Note", txn.details ?: "—")
                DetailRow("Created at", fmtDateTime(txn.createdAt))
                DetailRow("Updated at", fmtDateTime(txn.updatedAt))
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedButton(onClick = { editing = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { confirmDelete = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DebitRed, contentColor = WarmWhite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = WarmWhite,
            title = { Text("Delete transaction?") },
            text = { Text("This permanently removes the transaction.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteTxn(txn.transactionId) { onDismiss() }
                }) { Text("Delete", color = DebitRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
    }
}

// ---------------------------------------------------------------- bank edit dialog

@Composable
fun BankDialog(bank: UserBank, loading: Boolean, vm: LedgerViewModel, onDismiss: () -> Unit) {
    var opening by remember { mutableStateOf(fmtPlain(bank.openingBalance)) }
    var confirmRemove by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmWhite,
        title = { Text(bank.bankName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current balance: ${rupees(bank.currentBalance)}")
                OutlinedTextField(
                    value = opening,
                    onValueChange = { opening = cleanAmount(it) },
                    label = { Text("Opening balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { confirmRemove = true }) { Text("Remove bank", color = DebitRed) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = opening.toDoubleOrNull() != null && !loading,
                onClick = { vm.updateBank(bank.bankUserId, opening.toDouble(), onDismiss) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            containerColor = WarmWhite,
            title = { Text("Remove ${bank.bankName}?") },
            text = { Text("Its transactions are kept in your list, but the bank and its balance will no longer be shown.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    vm.removeBank(bank.bankUserId, onDismiss)
                }) { Text("Remove", color = DebitRed) }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } }
        )
    }
}

// ---------------------------------------------------------------- forms

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Sand,
    selectedLabelColor = Charcoal
)

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Used for both "add" (initial = null) and "edit". */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TxnForm(
    initial: Txn?,
    banks: List<UserBank>,
    categories: List<Category>,
    loading: Boolean,
    submitLabel: String,
    onSubmit: (TxnRequest) -> Unit
) {
    // keep the transaction's own bank selectable even if the user has since removed it
    val options = remember(banks, initial) {
        val list = banks.map { it.bankUserId to it.bankName }
        if (initial != null && list.none { it.first == initial.bankUserId }) list + (initial.bankUserId to initial.bankName)
        else list
    }

    var type by remember { mutableStateOf(initial?.type ?: "DEBIT") }
    var amount by remember { mutableStateOf(initial?.amount?.let { fmtPlain(it) } ?: "") }
    var bankUserId by remember { mutableStateOf(initial?.bankUserId ?: options.firstOrNull()?.first) }
    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var details by remember { mutableStateOf(initial?.details ?: "") }
    var date by remember { mutableStateOf(initial?.let { LocalDate.parse(it.transactionDate) } ?: LocalDate.now()) }
    var showPicker by remember { mutableStateOf(false) }

    val valid = !loading && bankUserId != null && categoryId != null && (amount.toDoubleOrNull() ?: 0.0) > 0.0

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("DEBIT" to "Debit", "CREDIT" to "Credit").forEach { (key, label) ->
                FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label) }, colors = chipColors())
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = cleanAmount(it) },
            label = { Text("Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FieldLabel("Bank")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (id, name) ->
                FilterChip(selected = bankUserId == id, onClick = { bankUserId = id }, label = { Text(name) }, colors = chipColors())
            }
        }

        FieldLabel("Category")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { c ->
                FilterChip(
                    selected = categoryId == c.categoryId,
                    onClick = { categoryId = c.categoryId },
                    label = { Text(c.category) },
                    colors = chipColors()
                )
            }
        }

        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Date: ${date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
        }

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Note (optional)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            enabled = valid,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onSubmit(
                    TxnRequest(
                        bankUserId = bankUserId!!,
                        type = type,
                        amount = amount.toDouble(),
                        categoryId = categoryId!!,
                        details = details.trim().ifBlank { null },
                        transactionDate = date.toString()
                    )
                )
            }
        ) { Text(submitLabel) }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BankForm(available: List<Bank>, loading: Boolean, onSubmit: (Long, Double) -> Unit) {
    var bankId by remember { mutableStateOf<Long?>(null) }
    var opening by remember { mutableStateOf("") }

    if (available.isEmpty()) {
        Text("All available banks are already linked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FieldLabel("Select a bank")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            available.forEach { b ->
                FilterChip(
                    selected = bankId == b.bankId,
                    onClick = { bankId = b.bankId },
                    label = { Text(b.bankName) },
                    colors = chipColors()
                )
            }
        }
        OutlinedTextField(
            value = opening,
            onValueChange = { opening = cleanAmount(it) },
            label = { Text("Opening balance (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            enabled = bankId != null && !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSubmit(bankId!!, opening.toDoubleOrNull() ?: 0.0) }
        ) { Text("Link bank") }
    }
}
