package com.noviaralabs.ledger.data

data class User(val userId: Long, val name: String, val mobileNumber: String)
data class Bank(val bankId: Long, val bankName: String)
data class Category(val categoryId: Long, val category: String)

data class UserBank(
    val bankUserId: Long,
    val bankId: Long,
    val bankName: String,
    val openingBalance: Double,
    val currentBalance: Double
)

data class Txn(
    val transactionId: Long,
    val bankUserId: Long,
    val bankName: String,
    val type: String, // CREDIT | DEBIT
    val amount: Double,
    val categoryId: Long,
    val category: String,
    val details: String?,
    val transactionDate: String, // yyyy-MM-dd
    val createdAt: String,       // ISO instant (UTC)
    val updatedAt: String
) {
    val isCredit: Boolean get() = type == "CREDIT"
}

data class Dashboard(val user: User, val banks: List<UserBank>, val transactions: List<Txn>)

data class LoginRequest(val mobileNumber: String)
data class RegisterRequest(val mobileNumber: String, val name: String)
data class AddBankRequest(val bankId: Long, val openingBalance: Double)
data class UpdateBankRequest(val openingBalance: Double)
data class TxnRequest(
    val bankUserId: Long,
    val type: String,
    val amount: Double,
    val categoryId: Long,
    val details: String?,
    val transactionDate: String
)
