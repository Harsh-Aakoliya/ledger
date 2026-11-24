package com.noviaralabs.ledger.ui

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val inr = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

fun rupees(v: Double): String = if (v < 0) "-₹" + inr.format(-v) else "₹" + inr.format(v)

fun signedRupees(v: Double, credit: Boolean): String = (if (credit) "+" else "-") + rupees(v)

/** "yyyy-MM-dd" -> "23 Nov 2026" */
fun fmtDate(iso: String): String = LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

/** UTC instant string -> device local time */
fun fmtDateTime(iso: String): String =
    Instant.parse(iso).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))

fun fmtMillis(ms: Long): String =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))

fun fmtPlain(v: Double): String = BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()

/** Digits and at most one dot, max 2 decimals. */
fun cleanAmount(s: String): String {
    val f = s.filter { it.isDigit() || it == '.' }
    val i = f.indexOf('.')
    if (i == -1) return f
    return f.substring(0, i + 1) + f.substring(i + 1).replace(".", "").take(2)
}
