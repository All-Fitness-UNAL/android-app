package com.allfitness.app.core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** "Hoy" es la fecha local del dispositivo; la semana va de lunes a domingo (research R10). */
object Today {
    val locale: Locale = Locale.forLanguageTag("es-CO")

    fun date(): LocalDate = LocalDate.now()

    fun weekStart(date: LocalDate = date()): LocalDate = date.with(DayOfWeek.MONDAY)

    fun weekDates(date: LocalDate = date()): List<LocalDate> =
        weekStart(date).let { start -> (0L..6L).map(start::plusDays) }
}

fun DayOfWeek.displayName(style: TextStyle = TextStyle.FULL): String =
    getDisplayName(style, Today.locale).replaceFirstChar { it.titlecase(Today.locale) }

fun LocalDate.shortLabel(): String = format(DateTimeFormatter.ofPattern("d MMM", Today.locale))

fun LocalDate.longLabel(): String = format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Today.locale))
