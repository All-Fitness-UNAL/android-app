package com.allfitness.app.nutrition

import java.time.LocalDate

/** El día de la dieta que corresponde a [today]; null si no hay dieta o le falta ese día. */
fun dietDayFor(diet: Diet?, today: LocalDate): DietDay? = diet?.days?.firstOrNull { it.dayOfWeek == today.dayOfWeek }
