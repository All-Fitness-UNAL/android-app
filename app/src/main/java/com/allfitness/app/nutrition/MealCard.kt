package com.allfitness.app.nutrition

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.allfitness.app.R
import com.allfitness.app.core.ui.AppCard
import com.allfitness.app.core.ui.IconTile
import com.allfitness.app.core.ui.numberStyle
import com.allfitness.app.core.ui.theme.Spacing

@DrawableRes
fun MealType.iconRes(): Int = when (this) {
    MealType.BREAKFAST -> R.drawable.lc_coffee
    MealType.LUNCH -> R.drawable.lc_salad
    MealType.DINNER -> R.drawable.lc_moon
    MealType.SNACK -> R.drawable.lc_cookie
}

/**
 * Comida tocable (Figma `meal-card-*`): placa de 64 dp con el ícono del tipo, tipo y kcal, plato y tiempo
 * de preparación. Sin estado de consumo (spec §3). La tarjeta crece con la fuente: alto mínimo, no fijo.
 */
@Composable
fun MealCard(meal: Meal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    AppCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            IconTile(meal.type.iconRes(), Spacing.thumbMeal)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    Text(
                        stringResource(meal.type.labelRes()),
                        Modifier.weight(1f).alignByBaseline(),
                        style = MaterialTheme.typography.labelMedium,
                        // El verde queda para lo que se toca (spec §4.1).
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.kcal_value, meal.recipe.perServing.kcal),
                        Modifier.alignByBaseline(),
                        style = numberStyle(MaterialTheme.typography.titleSmall),
                    )
                }
                Text(meal.recipe.name, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Icon(painterResource(R.drawable.lc_clock), contentDescription = null, Modifier.size(Spacing.sm), tint = scheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.meal_prep_minutes, meal.recipe.prepMinutes),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
