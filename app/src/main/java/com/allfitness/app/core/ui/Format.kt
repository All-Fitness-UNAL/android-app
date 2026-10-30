package com.allfitness.app.core.ui

import com.allfitness.app.core.time.Today
import java.text.NumberFormat

/** Formatea con coma decimal (es-CO) y máximo 1 decimal: 77,5. */
fun formatNumber(value: Double): String =
    NumberFormat.getNumberInstance(Today.locale).apply { maximumFractionDigits = 1 }.format(value)

/** Acepta "77,5" o "77.5"; devuelve null si no es un número con máximo 1 decimal. */
fun parseOneDecimal(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    val number = normalized.toBigDecimalOrNull() ?: return null
    return if (number.stripTrailingZeros().scale() <= 1) number.toDouble() else null
}
