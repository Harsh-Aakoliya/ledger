package com.noviaralabs.ledger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noviaralabs.ledger.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

data class UiState(
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val askRegister: Boolean = false,
    val dashboard: Dashboard? = null,
    val lastUpdated: Long? = null,
    val banks: List<Bank> = emptyList(),
    val categories: List<Category> = emptyList()
)

class LedgerViewModel(app: Application) : AndroidViewModel(app) {
    private val api = Network.api
    private val session = Session(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (session.userId() != null) {
            _state.update { it.copy(loggedIn = true) }
            refresh()
        }
    }

    // ---------- auth ----------
    fun login(mobile: String) = launchCall(onError = { e ->
        if (e is HttpException && e.code() == 404) {
            _state.update { it.copy(askRegister = true) }
            true
        } else false
    }) {
        enter(api.login(LoginRequest(clean(mobile))))
    }

    fun register(mobile: String, name: String) = launchCall {
        enter(api.register(RegisterRequest(clean(mobile), name.trim())))
    }

    fun dismissRegister() = _state.update { it.copy(askRegister = false) }

    fun logout() {
        session.clear()
        _state.value = UiState()
    }

    fun clearError() = _state.update { it.copy(error = null) }

    // ---------- data ----------
    fun refresh() {
        val id = requireUser() ?: return
        launchCall { loadAll(id) }
    }

    fun addBank(bankId: Long, opening: Double, onDone: () -> Unit) = mutate(onDone) { uid ->
        api.addBank(uid, AddBankRequest(bankId, opening))
    }

    fun updateBank(bankUserId: Long, opening: Double, onDone: () -> Unit) = mutate(onDone) { uid ->
        api.updateBank(uid, bankUserId, UpdateBankRequest(opening))
    }

    fun removeBank(bankUserId: Long, onDone: () -> Unit) = mutate(onDone) { uid ->
        api.removeBank(uid, bankUserId)
    }

    fun addTxn(req: TxnRequest, onDone: () -> Unit) = mutate(onDone) { uid -> api.addTxn(uid, req) }

    fun updateTxn(id: Long, req: TxnRequest, onDone: () -> Unit) = mutate(onDone) { uid ->
        api.updateTxn(uid, id, req)
    }

    fun deleteTxn(id: Long, onDone: () -> Unit) = mutate(onDone) { uid -> api.deleteTxn(uid, id) }

    // ---------- helpers ----------
    private fun mutate(onDone: () -> Unit, call: suspend (Long) -> Dashboard) {
        val id = requireUser() ?: return
        launchCall(onOk = onDone) {
            val d = call(id)
            _state.update { it.copy(dashboard = d, lastUpdated = System.currentTimeMillis()) }
        }
    }

    private suspend fun enter(user: User) {
        session.save(user.userId)
        loadAll(user.userId)
        _state.update { it.copy(loggedIn = true, askRegister = false) }
    }

    private suspend fun loadAll(userId: Long) {
        val d = api.dashboard(userId)
        val banks = api.banks()
        val cats = api.categories()
        _state.update {
            it.copy(dashboard = d, banks = banks, categories = cats, lastUpdated = System.currentTimeMillis())
        }
    }

    /** Session expiry check (1 day). Logs out and returns null if expired. */
    private fun requireUser(): Long? {
        val id = session.userId()
        if (id == null) logout()
        return id
    }

    private fun launchCall(
        onError: (Exception) -> Boolean = { false },
        onOk: () -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                block()
                onOk()
            } catch (e: Exception) {
                if (!onError(e)) _state.update { it.copy(error = errText(e)) }
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    private fun clean(s: String) = s.filter { !it.isWhitespace() }

    private fun errText(e: Exception): String = when (e) {
        is HttpException -> runCatching {
            JSONObject(e.response()?.errorBody()?.string().orEmpty()).optString("message")
        }.getOrNull().takeUnless { it.isNullOrBlank() } ?: "Server error (${e.code()})"
        is IOException -> "Can't reach the server. Check your connection."
        else -> e.message ?: "Something went wrong"
    }
}
